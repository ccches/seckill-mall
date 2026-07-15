package com.seckill.mall.service;

import com.seckill.mall.model.dto.PageDTO;
import com.seckill.mall.model.vo.ProductVO;

public interface ProductService {

    /**
     * 分页查商品列表（缓存优先）
     */
    PageDTO<ProductVO> listProducts(int page, int size, Long categoryId);

    /**
     * 查商品详情（缓存优先）
     */
    ProductVO getDetail(Long productId);
}
