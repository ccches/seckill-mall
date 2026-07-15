package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.model.dto.CartAddDTO;
import com.seckill.mall.model.vo.CartItemVO;
import com.seckill.mall.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 查看购物车
     */
    @GetMapping
    public Result<List<CartItemVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cartService.list(userId));
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody CartAddDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.add(userId, dto);
        return Result.success();
    }

    /**
     * 修改数量
     */
    @PutMapping("/{productId}")
    public Result<?> update(@PathVariable Long productId,
                            @RequestParam Integer quantity,
                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.updateQuantity(userId, productId, quantity);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{productId}")
    public Result<?> remove(@PathVariable Long productId,
                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.remove(userId, productId);
        return Result.success();
    }
}
