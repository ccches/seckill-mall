package com.seckill.mall.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.mall.mapper.OrderMapper;
import com.seckill.mall.model.entity.Order;
import com.seckill.mall.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单超时取消消费者——收到 RocketMQ 延迟消息后，检查订单是否已支付。
 * 未支付 → 取消订单 + 恢复 Redis 库存（INCR）
 * 已支付 → 不做任何操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "SECKILL_TIMEOUT", consumerGroup = "timeout-consumer")
public class OrderTimeoutConsumer implements RocketMQListener<String> {

    private final OrderMapper orderMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String orderNo) {
        log.info("[超时检查] 开始检查: orderNo={}", orderNo);

        // 查订单
        Order order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo));

        if (order == null) {
            log.warn("[超时检查] 订单不存在: orderNo={}", orderNo);
            return;
        }

        // 只有待支付(0)才取消
        if (order.getStatus() != 0) {
            log.info("[超时检查] 订单已处理，跳过: orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        // 取消订单
        orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
                        .set(Order::getStatus, 2)         // 已取消
                        .set(Order::getCancelTime, LocalDateTime.now()));

        // 恢复 Redis 秒杀库存（INCR）
        if (order.getActivityId() != null) {
            String stockKey = RedisKeyUtil.seckillStock(order.getActivityId());
            redisTemplate.opsForValue().increment(stockKey);
            log.info("[超时取消] 库存已恢复: activityId={}", order.getActivityId());
        }

        log.info("[超时取消] 订单已取消: orderNo={}", orderNo);
    }
}
