package com.seckill.mall.common;

import lombok.Getter;

/**
 * 自定义业务异常——Service层遇到预期异常时抛出，由 GlobalExceptionHandler 统一处理。
 *
 * 用法:
 *   throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
