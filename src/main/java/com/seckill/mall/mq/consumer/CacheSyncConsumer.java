package com.seckill.mall.mq.consumer;

import com.seckill.mall.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 缓存同步消费者——收到 Binlog 变更通知后，删除 Redis 缓存。
 *
 * 完整链路：
 *   MySQL 变更 → BinlogListener 监听到 → 发 MQ(CACHE_SYNC)
 *   → 本 Consumer 消费 → DEL Redis 缓存
 *   → 后续：PUBLISH Redis 广播 → 所有节点清 Caffeine 本地缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "CACHE_SYNC", consumerGroup = "cache-sync-consumer")
public class CacheSyncConsumer implements RocketMQListener<Map<String, Object>> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductServiceImpl productService;

    @Override
    public void onMessage(Map<String, Object> msg) {
        String type = msg.get("type").toString();
        log.info("[缓存同步] 收到变更通知: type={}", type);

        if ("product".equals(type)) {
            // 清产品列表缓存（所有分页的 key）
            var keys = redisTemplate.keys("product:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[缓存同步] 已清除 {} 个商品列表缓存", keys.size());
            }
            // 通过 Pub/Sub 广播通知所有节点清 Caffeine
            redisTemplate.convertAndSend("cache:invalidate:product", "all");

        } else if ("seckill".equals(type)) {
            // 清秒杀相关缓存
            var keys = redisTemplate.keys("seckill:stock:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[缓存同步] 已清除秒杀库存缓存");
            }
        }
    }
}
