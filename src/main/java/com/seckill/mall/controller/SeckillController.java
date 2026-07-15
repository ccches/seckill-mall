package com.seckill.mall.controller;

import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.ErrorCode;
import com.seckill.mall.common.Result;
import com.seckill.mall.model.entity.SeckillActivity;
import com.seckill.mall.service.SeckillService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    // ===== 管理后台接口 =====

    @PostMapping("/activity")
    public Result<SeckillActivity> createActivity(@RequestBody SeckillActivity activity) {
        return Result.success(seckillService.createActivity(activity));
    }

    @GetMapping("/activities")
    public Result<List<SeckillActivity>> listActivities() {
        return Result.success(seckillService.listActivities());
    }

    // ===== 秒杀用户接口（TODO：后续实现） =====

    @PostMapping("/{activityId}/path")
    public Result<String> getPath(@PathVariable Long activityId,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(seckillService.getSeckillPath(activityId, userId));
    }

    private final CaptchaController captchaController;

    @PostMapping("/{activityId}/execute")
    public Result<?> execute(@PathVariable Long activityId,
                              @RequestParam String md5Key,
                              @RequestParam String captcha,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 验证码校验
        if (!captchaController.validate(userId, captcha)) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }

        String orderNo = seckillService.executeSeckill(activityId, userId, md5Key);
        return Result.success(java.util.Map.of("orderNo", orderNo, "status", "QUEUING"));
    }

    @GetMapping("/{activityId}/result")
    public Result<?> result(@PathVariable Long activityId,
                             @RequestParam String orderNo) {
        String status = seckillService.getSeckillResult(activityId, orderNo);
        return Result.success(java.util.Map.of("orderNo", orderNo, "status", status));
    }
}
