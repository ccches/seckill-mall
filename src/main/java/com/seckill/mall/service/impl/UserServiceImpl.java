package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.ErrorCode;
import com.seckill.mall.mapper.UserMapper;
import com.seckill.mall.model.dto.LoginRequestDTO;
import com.seckill.mall.model.entity.User;
import com.seckill.mall.service.UserService;
import com.seckill.mall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 解决 Lombok 在 IDE 中可能不识别的问题，手写构造器
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public TokenPair register(LoginRequestDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getUsername());
        user.setStatus(1);
        userMapper.insert(user);

        String accessToken = JwtUtil.generateAccessToken(user.getId());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId());
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public TokenPair login(LoginRequestDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = JwtUtil.generateAccessToken(user.getId());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId());
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}
