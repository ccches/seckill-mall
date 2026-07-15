package com.seckill.mall.scheduler;

import com.seckill.mall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀活动预热——每30秒扫描一次即将开始的活动，把库存加载到 Redis。
 */
@Component
@RequiredArgsConstructor
public class ActivityPreheatTask {

    private final SeckillService seckillService;

    @Scheduled(fixedDelay = 30_000)  // 每30秒执行一次
    public void preheat() {
        seckillService.preheatUpcoming();
    }
}
