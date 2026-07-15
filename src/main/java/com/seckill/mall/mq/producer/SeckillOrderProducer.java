package com.seckill.mall.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 秒杀订单消息生产者——Lua 扣减成功后将订单信息发到 MQ，由 Consumer 异步创建订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderProducer {

    private final RocketMQTemplate rocketMQTemplate;

    private static final String TOPIC = "SECKILL_ORDER";

    public void send(Long userId, Long activityId, Long productId,
                     String orderNo, Long seckillPrice) {
        Map<String, Object> msg = Map.of(
                "userId", userId,
                "activityId", activityId,
                "productId", productId,
                "orderNo", orderNo,
                "seckillPrice", seckillPrice
        );

        rocketMQTemplate.send(TOPIC, new GenericMessage<>(msg));
        log.info("[MQ] 秒杀消息已发送: orderNo={}", orderNo);
    }

    /**
     * 发送延迟消息——用于订单超时取消。
     * 延迟级别 3 = 10秒（测试用），生产环境用 16 = 30分钟。
     */
    public void sendDelay(String orderNo) {
        org.springframework.messaging.Message<String> msg =
                org.springframework.messaging.support.MessageBuilder
                        .withPayload(orderNo).build();
        rocketMQTemplate.syncSend("SECKILL_TIMEOUT", msg, 3000, 3);
        log.info("[MQ] 延迟取消消息已发送: orderNo={}", orderNo);
    }
}
