package cn.iocoder.dashboard.framework.apollox.spring.boot;


import cn.hutool.core.lang.Singleton;
import cn.iocoder.dashboard.framework.apollox.Config;
import cn.iocoder.dashboard.framework.apollox.DBConfig;
import cn.iocoder.dashboard.framework.apollox.spring.config.ConfigPropertySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static cn.iocoder.dashboard.framework.apollox.spring.config.PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME;

@Slf4j
public class ApolloApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        // 忽略，若已经有 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 的 PropertySource
        if (environment.getPropertySources().contains(APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            // already initialized
            return;
        }

        // 创建自定义的 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 的 PropertySource
        Config config = Singleton.get(DBConfig.class);
        ConfigPropertySource configPropertySource = new ConfigPropertySource(APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, config);
        // 添加到 `environment` 中，且优先级最高
        environment.getPropertySources().addFirst(configPropertySource);
    }

}
