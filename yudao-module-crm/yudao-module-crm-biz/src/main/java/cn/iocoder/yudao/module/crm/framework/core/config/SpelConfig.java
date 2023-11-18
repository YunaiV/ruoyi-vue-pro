package cn.iocoder.yudao.module.crm.framework.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 注册 Spel 所需 Bean
 *
 * @author HUIHUI
 */
@Configuration
public class SpelConfig {

    @Bean
    public SpelExpressionParser spelExpressionParser() {
        return new SpelExpressionParser();
    }

}
