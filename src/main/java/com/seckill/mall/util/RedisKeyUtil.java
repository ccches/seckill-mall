package com.seckill.mall.util;

/**
 * Redis Key 统一生成工具——所有 Key 必须通过此类生成，禁止硬编码字符串。
 */
public class RedisKeyUtil {

    private static final String PREFIX = "seckill:";
    private static final String PREFIX_PRODUCT = "product:";
    private static final String PREFIX_USER = "user:";
    private static final String PREFIX_CART = "cart:";
    private static final String PREFIX_ORDER = "order:";

    // ===== 秒杀相关 =====

    public static String seckillStock(Long activityId) {
        return PREFIX + "stock:" + activityId;
    }

    public static String seckillUser(Long activityId, Long userId) {
        return PREFIX + "user:" + activityId + ":" + userId;
    }

    public static String seckillResult(String orderNo) {
        return PREFIX + "result:" + orderNo;
    }

    public static String seckillPath(Long activityId, Long userId) {
        return PREFIX + "path:" + activityId + ":" + userId;
    }

    // ===== 商品相关 =====

    public static String productDetail(Long productId) {
        return PREFIX_PRODUCT + "detail:" + productId;
    }

    public static String productList(int page, int size, Long categoryId) {
        return PREFIX_PRODUCT + "list:page:" + page + ":size:" + size + ":" + categoryId;
    }

    // ===== 用户相关 =====

    public static String userToken(Long userId) {
        return PREFIX_USER + "token:" + userId;
    }

    // ===== 购物车相关 =====

    public static String cart(Long userId) {
        return PREFIX_CART + userId;
    }

    // ===== 订单相关 =====

    public static String orderDetail(String orderNo) {
        return PREFIX_ORDER + "detail:" + orderNo;
    }

    private RedisKeyUtil() {}
}
