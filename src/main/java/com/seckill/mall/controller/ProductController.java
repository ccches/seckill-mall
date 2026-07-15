package com.seckill.mall.controller;

import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.ErrorCode;
import com.seckill.mall.common.Result;
import com.seckill.mall.model.dto.PageDTO;
import com.seckill.mall.model.vo.ProductVO;
import com.seckill.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public Result<PageDTO<ProductVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(productService.listProducts(page, size, categoryId));
    }

    /**
     * 商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        ProductVO vo = productService.getDetail(id);
        if (vo == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return Result.success(vo);
    }
}
