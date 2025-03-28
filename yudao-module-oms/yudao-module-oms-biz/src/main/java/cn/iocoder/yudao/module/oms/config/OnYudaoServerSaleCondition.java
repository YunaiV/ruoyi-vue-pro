package cn.iocoder.yudao.module.oms.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @description:
 * @author: LaoSan
 * @create: 2025-03-18 19:10
 **/
public class OnYudaoServerSaleCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取 spring.application.name 的值
        String applicationName = context.getEnvironment().getProperty("spring.application.name");
        // 检查是否为 "yudao-server-sale"
        return "yudao-server-sale".equals(applicationName);
    }
}
