package com.seckill.mall.config;

import com.seckill.mall.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
/**
 * Redis Pub/Sub 缓存失效监听——处理多节点缓存一致性。
 *
 * 流程：
 *  节点A 更新商品 → evictCache() → 删 Caffeine + 删 Redis + PUBLISH 广播
 *  节点B、C 收到广播 → 清除自己 JVM 里的 Caffeine 缓存
 *
 * 后续和 Canal 整合：
 *  Canal 监听到 Binlog → MQ → Consumer 除了删 Redis，也发 Pub/Sub 广播。
 */
@Component
@RequiredArgsConstructor
public class CacheInvalidateListener implements MessageListener {

    private final ProductServiceImpl productService;
    private final RedisMessageListenerContainer listenerContainer;

    private static final String CHANNEL = "cache:invalidate:product";

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(this, new ChannelTopic(CHANNEL));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Long productId = Long.valueOf(new String(message.getBody()));
            productService.evictCache(productId);
            log.info("收到失效广播，已清除本地缓存: productId={}", productId);
        } catch (Exception e) {
            log.error("Pub/Sub 解析失败", e);
        }
    }
}
