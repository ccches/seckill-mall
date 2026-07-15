package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.model.entity.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    /**
     * 乐观锁扣减秒杀库存
     */
    @Update("UPDATE t_seckill_activity SET seckill_stock = seckill_stock - 1, " +
            "version = version + 1 WHERE id = #{id} AND seckill_stock >= 1 " +
            "AND version = #{version}")
    int updateStock(@Param("id") Long id, @Param("version") Integer version);
}
