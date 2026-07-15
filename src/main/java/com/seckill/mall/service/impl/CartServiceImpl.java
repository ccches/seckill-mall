package com.seckill.mall.service.impl;

import com.seckill.mall.model.dto.CartAddDTO;
import com.seckill.mall.model.entity.Product;
import com.seckill.mall.model.vo.CartItemVO;
import com.seckill.mall.service.CartService;
import com.seckill.mall.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductServiceImpl productService;

    @Override
    public void add(Long userId, CartAddDTO dto) {
        String key = RedisKeyUtil.cart(userId);
        HashOperations<String, String, Integer> ops = redisTemplate.opsForHash();

        // 获取已有数量，累加
        Integer current = ops.get(key, dto.getProductId().toString());
        int newQty = (current == null ? 0 : current) + dto.getQuantity();

        ops.put(key, dto.getProductId().toString(), newQty);
    }

    @Override
    public List<CartItemVO> list(Long userId) {
        String key = RedisKeyUtil.cart(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        List<CartItemVO> items = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = Long.valueOf(entry.getKey().toString());
            Integer quantity = (Integer) entry.getValue();

            Product product = productService.getProductById(productId);
            if (product == null) continue;

            items.add(CartItemVO.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .build());
        }
        return items;
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String key = RedisKeyUtil.cart(userId);
        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, productId.toString());
        } else {
            redisTemplate.opsForHash().put(key, productId.toString(), quantity);
        }
    }

    @Override
    public void remove(Long userId, Long productId) {
        String key = RedisKeyUtil.cart(userId);
        redisTemplate.opsForHash().delete(key, productId.toString());
    }
}
