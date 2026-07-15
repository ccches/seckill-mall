package com.seckill.mall.model.dto;

import lombok.Data;

@Data
public class CartAddDTO {
    private Long productId;
    private Integer quantity;
}
