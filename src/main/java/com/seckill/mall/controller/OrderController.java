package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.model.dto.OrderCreateDTO;
import com.seckill.mall.model.vo.OrderVO;
import com.seckill.mall.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 下单
     */
    @PostMapping("/create")
    public Result<List<OrderVO>> create(@RequestBody OrderCreateDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.create(userId, dto));
    }

    /**
     * 订单列表
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> list(@RequestParam(required = false) Integer status,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.list(userId, status));
    }
}
