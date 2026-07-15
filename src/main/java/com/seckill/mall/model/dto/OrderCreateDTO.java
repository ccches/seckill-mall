package com.seckill.mall.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}
