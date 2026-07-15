package com.seckill.mall.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流配置——直接在代码里设规则，不依赖 Dashboard。
 * 令牌桶模式：每秒最多放行 2000 个请求。超出则直接拒绝。
 */
@Configuration
public class SentinelConfig {

    private static final String SECKILL_RESOURCE = "seckill-execute";
    private static final int QPS_LIMIT = 2000;

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule rule = new FlowRule();
        rule.setResource(SECKILL_RESOURCE);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);   // QPS 限流模式
        rule.setCount(QPS_LIMIT);                      // 每秒 2000 个请求
        rules.add(rule);

        FlowRuleManager.loadRules(rules);
        System.out.println("[Sentinel] 限流规则已加载: " + SECKILL_RESOURCE + " QPS=" + QPS_LIMIT);
    }
}
