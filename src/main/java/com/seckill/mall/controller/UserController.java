package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.model.dto.LoginRequestDTO;
import com.seckill.mall.model.entity.User;
import com.seckill.mall.service.UserService;
import com.seckill.mall.service.UserService.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody LoginRequestDTO dto) {
        TokenPair tokens = userService.register(dto);
        return Result.success(Map.of(
                "accessToken", tokens.accessToken(),
                "refreshToken", tokens.refreshToken()
        ));
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequestDTO dto) {
        TokenPair tokens = userService.login(dto);
        return Result.success(Map.of(
                "accessToken", tokens.accessToken(),
                "refreshToken", tokens.refreshToken()
        ));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        return Result.success(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "phone", user.getPhone(),
                "email", user.getEmail()
        ));
    }
}
