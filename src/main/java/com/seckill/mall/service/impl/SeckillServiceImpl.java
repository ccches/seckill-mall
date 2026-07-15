package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.ErrorCode;
import com.seckill.mall.mapper.OrderMapper;
import com.seckill.mall.mapper.ProductMapper;
import com.seckill.mall.mapper.SeckillActivityMapper;
import com.seckill.mall.model.entity.Order;
import com.seckill.mall.model.entity.Product;
import com.seckill.mall.model.entity.SeckillActivity;
import com.seckill.mall.service.SeckillService;
import com.seckill.mall.util.RedisKeyUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.seckill.mall.mq.producer.SeckillOrderProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillOrderProducer orderProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    // 预加载 Lua 脚本
    private final DefaultRedisScript<Long> stockDeductScript = new DefaultRedisScript<>();

    {
        stockDeductScript.setLocation(new ClassPathResource("lua/seckill_stock_deduct.lua"));
        stockDeductScript.setResultType(Long.class);
    }

    @Override
    public SeckillActivity createActivity(SeckillActivity activity) {
        activity.setStatus(0);
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    public List<SeckillActivity> listActivities() {
        return activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreateTime));
    }

    @Override
    public void preheatUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime window = now.plusMinutes(5);

        List<SeckillActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 0)
                        .ge(SeckillActivity::getStartTime, now)
                        .le(SeckillActivity::getStartTime, window));

        for (SeckillActivity act : activities) {
            redisTemplate.opsForValue().set(
                    RedisKeyUtil.seckillStock(act.getId()),
                    act.getSeckillStock(), 2, TimeUnit.HOURS);
            log.info("[预热] 活动" + act.getId() + " 库存" + act.getSeckillStock());
        }
    }

    @Override
    public String getSeckillPath(Long activityId, Long userId) {
        SeckillActivity act = getActivity(activityId);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(act.getStartTime().minusSeconds(2))) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_STARTED);
        }
        if (now.isAfter(act.getEndTime())) {
            throw new BusinessException(ErrorCode.SECKILL_ENDED);
        }

        // 生成动态 md5
        String md5 = md5(activityId + UUID.randomUUID().toString());
        redisTemplate.opsForValue().set(
                RedisKeyUtil.seckillPath(activityId, userId), md5, 60, TimeUnit.SECONDS);
        return md5;
    }

    @Override
    public String executeSeckill(Long activityId, Long userId, String md5Key) {
        SeckillActivity act = getActivity(activityId);
        LocalDateTime now = LocalDateTime.now();

        // ① 校验活动时间
        if (now.isBefore(act.getStartTime())) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_STARTED);
        }
        if (now.isAfter(act.getEndTime())) {
            throw new BusinessException(ErrorCode.SECKILL_ENDED);
        }

        // ② 校验 md5
        String cachedMd5 = (String) redisTemplate.opsForValue()
                .get(RedisKeyUtil.seckillPath(activityId, userId));
        if (cachedMd5 == null || !cachedMd5.equals(md5Key)) {
            throw new BusinessException(ErrorCode.SECKILL_PATH_INVALID);
        }

        // ③ Sentinel 限流检查——被限流则抛 BusinessException
        try (Entry ignored = SphU.entry("seckill-execute")) {
            return doExecute(act, userId, activityId);
        } catch (BlockException e) {
            throw new BusinessException(ErrorCode.RATE_LIMITED);
        }
    }

    /**
     * 真正执行秒杀（限流通过后才进入）
     */
    private String doExecute(SeckillActivity act, Long userId, Long activityId) {
        // 生成订单号
        String orderNo = "SEC" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        // ④ 兜底：如果库存还没预热，立刻预热
        String stockKey = RedisKeyUtil.seckillStock(activityId);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().set(stockKey, act.getSeckillStock(), 2, TimeUnit.HOURS);
            log.info("[兜底预热] 活动" + activityId + " 库存" + act.getSeckillStock());
        }

        String userKey = RedisKeyUtil.seckillUser(activityId, userId);

        Long result = redisTemplate.execute(
                stockDeductScript,
                Arrays.asList(stockKey, userKey),
                orderNo
        );

        if (result == null || result == -2) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        if (result == -3) {
            throw new BusinessException(ErrorCode.SECKILL_REPEATED);
        }

        // ⑤ Lua 扣减成功 → 发送 MQ 消息（异步下单削峰）
        orderProducer.send(userId, activityId, act.getProductId(), orderNo,
                act.getSeckillPrice().longValue());

        // ⑥ 更新活动状态
        if (act.getStatus() == 0) {
            act.setStatus(1);
            activityMapper.updateById(act);
        }

        return orderNo;
    }

    @Override
    public String getSeckillResult(Long activityId, String orderNo) {
        Object result = redisTemplate.opsForValue()
                .get(RedisKeyUtil.seckillResult(orderNo));
        return result == null ? "QUEUING" : result.toString();
    }

    // ===== 工具方法 =====

    private SeckillActivity getActivity(Long activityId) {
        SeckillActivity act = activityMapper.selectById(activityId);
        if (act == null) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_STARTED);
        }
        return act;
    }

    private String md5(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
