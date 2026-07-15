package com.seckill.mall.service;

import com.seckill.mall.model.dto.LoginRequestDTO;
import com.seckill.mall.model.entity.User;

public interface UserService {

    /**
     * 注册，返回 JWT Token 对
     */
    TokenPair register(LoginRequestDTO dto);

    /**
     * 登录，返回 JWT Token 对
     */
    TokenPair login(LoginRequestDTO dto);

    /**
     * 根据 ID 查用户
     */
    User getById(Long userId);

    /**
     * Token 对
     */
    record TokenPair(String accessToken, String refreshToken) {}
}
