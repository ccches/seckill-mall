package com.seckill.mall.config;

import com.seckill.mall.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器——从 Authorization Header 提取 Token，校验后把 userId 注入 request。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validate(token)) {
            response.setStatus(401);
            return false;
        }

        Long userId = JwtUtil.getUserId(token);
        request.setAttribute("userId", userId);
        return true;
    }
}
