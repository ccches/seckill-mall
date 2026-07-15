package com.seckill.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.mall.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 乐观锁扣库存——返回受影响行数，0 表示库存不足或版本冲突
     */
    @Update("UPDATE t_product SET stock = stock - #{quantity}, version = version + 1 " +
            "WHERE id = #{id} AND stock >= #{quantity} AND version = #{version}")
    int updateStock(@Param("id") Long id,
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);
}
