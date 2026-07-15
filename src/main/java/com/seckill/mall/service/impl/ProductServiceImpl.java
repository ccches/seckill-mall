package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import com.seckill.mall.mapper.ProductMapper;
import com.seckill.mall.model.dto.PageDTO;
import com.seckill.mall.model.entity.Product;
import com.seckill.mall.model.vo.ProductVO;
import com.seckill.mall.service.ProductService;
import com.seckill.mall.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Cache<Long, Product> productCache;  // Caffeine L1

    private static final long DETAIL_TTL = 30;
    private static final long LIST_TTL = 5;

    public ProductServiceImpl(ProductMapper productMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              ObjectMapper objectMapper,
                              Cache<Long, Product> productCache) {
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productCache = productCache;
    }

    @Override
    public PageDTO<ProductVO> listProducts(int page, int size, Long categoryId) {
        String cacheKey = RedisKeyUtil.productList(page, size, categoryId);

        // ① 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return objectMapper.convertValue(cached, new TypeReference<PageDTO<ProductVO>>() {});
        }

        // ② 查数据库
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getCreateTime);

        // 总数
        long total = productMapper.selectCount(wrapper);

        // 分页查询：手动算 offset
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + "," + size);

        List<ProductVO> records = productMapper.selectList(wrapper)
                .stream()
                .map(ProductVO::from)
                .toList();

        PageDTO<ProductVO> result = PageDTO.of(total, page, size, records);

        // ③ 写缓存
        long ttl = LIST_TTL + (long) (Math.random() * 120);
        redisTemplate.opsForValue().set(cacheKey, result, ttl, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public ProductVO getDetail(Long productId) {
        // ===== L1: Caffeine 本地缓存 =====
        Product cachedProduct = productCache.getIfPresent(productId);
        if (cachedProduct != null) {
            return ProductVO.from(cachedProduct);
        }

        // ===== L2: Redis 远程缓存 =====
        String cacheKey = RedisKeyUtil.productDetail(productId);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            Product p = objectMapper.convertValue(cached, Product.class);
            productCache.put(productId, p);  // 回填 L1
            return ProductVO.from(p);
        }

        // ===== L3: MySQL =====
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            return null;
        }

        // 回填 L1 + L2
        productCache.put(productId, product);
        long ttl = DETAIL_TTL + (long) (Math.random() * 300);
        redisTemplate.opsForValue().set(cacheKey, product, ttl, TimeUnit.MINUTES);

        return ProductVO.from(product);
    }

    /**
     * 清除所有层级缓存——数据更新后调用。
     *
     * 完整链路：
     *   ① 清 Caffeine L1（本节点）
     *   ② 删 Redis L2
     *   ③ PUBLISH 广播 → 其他节点 CacheInvalidateListener 收到 → 清各自 Caffeine
     */
    public void evictCache(Long productId) {
        productCache.invalidate(productId);                          // 清 L1（本节点）
        redisTemplate.delete(RedisKeyUtil.productDetail(productId)); // 清 L2
        redisTemplate.convertAndSend("cache:invalidate:product",
                productId.toString());                               // 广播清其他节点 L1
        log.info("缓存清除: productId={}", productId);
    }

    /**
     * 内部调用——直接查数据库获取 Product Entity（不走缓存）
     */
    public Product getProductById(Long productId) {
        return productMapper.selectById(productId);
    }
}
