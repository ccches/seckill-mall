package com.seckill.mall.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.seckill.mall.model.entity.Product;
import com.seckill.mall.model.entity.SeckillActivity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置——L1 热点数据隔离。
 * 秒杀商品详情和活动信息存在 JVM 堆内存，避免每次请求打到 Redis。
 */
@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<Long, Product> productCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<Long, SeckillActivity> activityCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
