package com.seckill.mall.model.vo;

import com.seckill.mall.model.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品返回对象——从 Entity 转换而来，不直接暴露数据库 Entity 给前端。
 */
@Data
@Builder
public class ProductVO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String images;
    private Long categoryId;
    private Integer status;

    // ===== 秒杀信息（如果该商品有秒杀活动） =====
    private SeckillInfo seckillInfo;

    @Data
    @Builder
    public static class SeckillInfo {
        private Long activityId;
        private BigDecimal seckillPrice;
        private Integer seckillStock;
        private String startTime;
        private String endTime;
        private Integer status;       // 0=未开始, 1=进行中, 2=已结束
    }

    /**
     * Entity → VO 转换
     */
    public static ProductVO from(Product product) {
        return ProductVO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .images(product.getImages())
                .categoryId(product.getCategoryId())
                .status(product.getStatus())
                .build();
    }
}
