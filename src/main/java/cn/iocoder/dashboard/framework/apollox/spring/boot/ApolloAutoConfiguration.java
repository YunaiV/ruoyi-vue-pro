package cn.iocoder.dashboard.framework.apollox.spring.boot;

import cn.iocoder.dashboard.framework.apollox.spring.config.ConfigPropertySourcesProcessor;
import cn.iocoder.dashboard.framework.apollox.spring.property.PropertySourcesProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(PropertySourcesProcessor.class) // 缺失 PropertySourcesProcessor 时
public class ApolloAutoConfiguration {

    @Bean
    public ConfigPropertySourcesProcessor configPropertySourcesProcessor() {
        return new ConfigPropertySourcesProcessor(); // 注入 ConfigPropertySourcesProcessor bean 对象
    }

}
