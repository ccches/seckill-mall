package com.seckill.mall.common;

import lombok.Getter;

/**
 * 错误码枚举——所有业务错误统一在这里管理，不允许在代码里写魔法数字。
 */
@Getter
public enum ErrorCode {

    // ===== 通用 =====
    SUCCESS(0, "success"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ===== 用户 =====
    UNAUTHORIZED(401, "未登录或Token已过期"),
    USERNAME_EXISTS(402, "用户名已存在"),
    LOGIN_FAILED(403, "用户名或密码错误"),

    // ===== 秒杀 =====
    SECKILL_NOT_STARTED(1001, "秒杀活动尚未开始"),
    SECKILL_ENDED(1002, "秒杀活动已结束"),
    STOCK_NOT_ENOUGH(1003, "库存不足"),
    SECKILL_REPEATED(1004, "您已参与过本次秒杀"),
    RATE_LIMITED(1005, "请求过于频繁，请稍后重试"),
    SECKILL_PATH_INVALID(1006, "秒杀地址无效"),
    CAPTCHA_ERROR(1007, "验证码错误"),

    // ===== 订单 =====
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_CANNOT_CANCEL(2002, "该订单不可取消"),

    // ===== 商品 =====
    PRODUCT_NOT_FOUND(3001, "商品不存在");

    // ==========================================

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
