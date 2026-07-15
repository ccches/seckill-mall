package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.ErrorCode;
import com.seckill.mall.mapper.OrderMapper;
import com.seckill.mall.mapper.ProductMapper;
import com.seckill.mall.model.dto.OrderCreateDTO;
import com.seckill.mall.model.entity.Order;
import com.seckill.mall.model.entity.Product;
import com.seckill.mall.model.vo.OrderVO;
import com.seckill.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public List<OrderVO> create(Long userId, OrderCreateDTO dto) {
        List<OrderVO> result = new ArrayList<>();

        for (OrderCreateDTO.Item item : dto.getItems()) {
            // ① 查商品（用乐观锁 version 防止并发超卖）
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }

            // ② 乐观锁扣库存
            int rows = productMapper.updateStock(
                    product.getId(), item.getQuantity(), product.getVersion());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }

            // ③ 生成订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setProductId(product.getId());
            order.setProductName(product.getName());
            order.setPrice(product.getPrice());
            order.setQuantity(item.getQuantity());
            order.setTotalAmount(product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            order.setOrderType(1);  // 普通订单
            order.setStatus(0);     // 待支付
            orderMapper.insert(order);

            result.add(OrderVO.from(order));
        }
        return result;
    }

    @Override
    public List<OrderVO> list(Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        return orderMapper.selectList(wrapper).stream()
                .map(OrderVO::from)
                .toList();
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORD" + timestamp + uuid;
    }
}
