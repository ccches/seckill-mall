package com.seckill.mall.model.vo;

import com.seckill.mall.model.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderVO {
    private String orderNo;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer orderType;
    private Integer status;
    private LocalDateTime createTime;

    public static OrderVO from(Order order) {
        return OrderVO.builder()
                .orderNo(order.getOrderNo())
                .productName(order.getProductName())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .createTime(order.getCreateTime())
                .build();
    }
}
