package com.seckill.mall.service;

import com.seckill.mall.model.dto.CartAddDTO;
import com.seckill.mall.model.vo.CartItemVO;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     */
    void add(Long userId, CartAddDTO dto);

    /**
     * 查看购物车
     */
    List<CartItemVO> list(Long userId);

    /**
     * 修改数量
     */
    void updateQuantity(Long userId, Long productId, Integer quantity);

    /**
     * 删除
     */
    void remove(Long userId, Long productId);
}
