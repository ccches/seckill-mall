package com.seckill.mall.service;

import com.seckill.mall.model.dto.OrderCreateDTO;
import com.seckill.mall.model.vo.OrderVO;

import java.util.List;

public interface OrderService {

    /**
     * 下单
     */
    List<OrderVO> create(Long userId, OrderCreateDTO dto);

    /**
     * 订单列表
     */
    List<OrderVO> list(Long userId, Integer status);
}
