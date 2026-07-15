package com.seckill.mall.mq.consumer;

import com.seckill.mall.mapper.OrderMapper;
import com.seckill.mall.mapper.ProductMapper;
import com.seckill.mall.mapper.SeckillActivityMapper;
import com.seckill.mall.model.entity.Order;
import com.seckill.mall.model.entity.Product;
import com.seckill.mall.mq.producer.SeckillOrderProducer;
import com.seckill.mall.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单消费者——从 MQ 拉消息，异步创建订单（DB 乐观锁兜底）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "SECKILL_ORDER", consumerGroup = "seckill-consumer")
public class SeckillOrderConsumer implements RocketMQListener<Map<String, Object>> {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final SeckillActivityMapper activityMapper;
    private final SeckillOrderProducer orderProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void onMessage(Map<String, Object> msg) {
        Long userId = Long.valueOf(msg.get("userId").toString());
        Long activityId = Long.valueOf(msg.get("activityId").toString());
        Long productId = Long.valueOf(msg.get("productId").toString());
        String orderNo = msg.get("orderNo").toString();
        Long seckillPrice = Long.valueOf(msg.get("seckillPrice").toString());

        log.info("[MQ消费] 开始处理: orderNo={}", orderNo);

        // 幂等检查：订单已存在则跳过
        if (orderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)) > 0) {
            log.info("[MQ消费] 订单已存在，跳过: orderNo={}", orderNo);
            return;
        }

        // DB 乐观锁扣减秒杀库存
        int rows = activityMapper.updateStock(activityId,
                activityMapper.selectById(activityId).getVersion());
        if (rows == 0) {
            log.warn("[MQ消费] 乐观锁冲突，库存扣减失败: orderNo={}", orderNo);
            redisTemplate.opsForValue().set(
                    RedisKeyUtil.seckillResult(orderNo), "FAILED", 10, TimeUnit.MINUTES);
            return;
        }

        // 查商品信息
        Product product = productMapper.selectById(productId);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setPrice(BigDecimal.valueOf(seckillPrice));
        order.setQuantity(1);
        order.setTotalAmount(BigDecimal.valueOf(seckillPrice));
        order.setOrderType(2);   // 秒杀订单
        order.setStatus(0);      // 待支付
        order.setActivityId(activityId);
        orderMapper.insert(order);

        // 写秒杀结果
        redisTemplate.opsForValue().set(
                RedisKeyUtil.seckillResult(orderNo), "SUCCESS", 10, TimeUnit.MINUTES);

        // 发送延迟消息，10秒后检查是否超时未支付（生产环境改为30分钟）
        orderProducer.sendDelay(orderNo);

        log.info("[MQ消费] 订单创建成功: orderNo={}", orderNo);
    }
}
