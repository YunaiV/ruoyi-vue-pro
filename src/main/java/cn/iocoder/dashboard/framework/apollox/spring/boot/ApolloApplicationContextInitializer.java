package cn.iocoder.dashboard.framework.apollox.spring.boot;


import cn.iocoder.dashboard.framework.apollox.Config;
import cn.iocoder.dashboard.framework.apollox.ConfigService;
import cn.iocoder.dashboard.framework.apollox.spring.config.ConfigPropertySourceFactory;
import cn.iocoder.dashboard.framework.apollox.spring.config.PropertySourcesConstants;
import cn.iocoder.dashboard.framework.apollox.spring.util.SpringInjector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.dashboard.framework.apollox.spring.config.PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME;

@Slf4j
public class ApolloApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        // 忽略，若已经有 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 的 PropertySource
        if (environment.getPropertySources().contains(APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            // already initialized
            return;
        }

        // 忽略，若已经有 APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME 的 PropertySource
        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            // already initialized
            return;
        }

        // 获得 "apollo.bootstrap.namespaces" 配置项
        List<String> namespaceList = Collections.singletonList("default");

        // 按照优先级，顺序遍历 Namespace
        CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        for (String namespace : namespaceList) {
            // 创建 Apollo Config 对象
            Config config = ConfigService.getConfig(namespace);
            // 创建 Namespace 对应的 ConfigPropertySource 对象
            // 添加到 `composite` 中。
            composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
        }

        // 添加到 `environment` 中，且优先级最高
        environment.getPropertySources().addFirst(composite);
    }

}
