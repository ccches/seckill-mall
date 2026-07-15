package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 数学验证码——防脚本刷单。
 * 流程：获取验证码 → 算答案 → 秒杀时校验。
 */
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    private static final String PREFIX = "seckill:captcha:";

    /**
     * 获取验证码——返回数学题 + 用户标识，答案存 Redis
     */
    @GetMapping
    public Result<Map<String, Object>> getCaptcha(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;
        int answer = a + b;

        // 用 userId + 时间戳做 key
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, answer, 2, TimeUnit.MINUTES);

        return Result.success(Map.of(
                "question", a + " + " + b + " = ?",
                "key", key
        ));
    }

    /**
     * 校验验证码（给 SeckillService 调用）
     */
    public boolean validate(Long userId, String userAnswer) {
        String key = PREFIX + userId;
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) return false;

        boolean correct = stored.toString().equals(userAnswer);
        if (correct) {
            redisTemplate.delete(key);  // 一次性使用
        }
        return correct;
    }
}
