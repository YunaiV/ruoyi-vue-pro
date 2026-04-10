package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Matcher 测试基类
 * 提供通用的 Spring 测试配置
 *
 * @author HUIHUI
 */
@SpringJUnitConfig
public abstract class IotBaseConditionMatcherTest {

    /**
     * 注入一下 SpringUtil，解析 EL 表达式时需要
     * {@link SpringExpressionUtils#parseExpression}
     */
    @Configuration
    static class TestConfig {

        @Bean
        public SpringUtil springUtil() {
            return new SpringUtil();
        }

    }

}
