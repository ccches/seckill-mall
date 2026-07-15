package com.seckill.mall.service;

import com.seckill.mall.model.entity.SeckillActivity;

import java.util.List;

public interface SeckillService {

    /**
     * 创建秒杀活动（管理后台）
     */
    SeckillActivity createActivity(SeckillActivity activity);

    /**
     * 查询秒杀活动列表
     */
    List<SeckillActivity> listActivities();

    /**
     * 预热秒杀活动——把库存加载到 Redis（定时任务调用）
     */
    void preheatUpcoming();

    /**
     * 获取秒杀动态地址 md5Key
     */
    String getSeckillPath(Long activityId, Long userId);

    /**
     * 执行秒杀——返回订单号、"排队中"
     */
    String executeSeckill(Long activityId, Long userId, String md5Key);

    /**
     * 查询秒杀结果
     */
    String getSeckillResult(Long activityId, String orderNo);
}
