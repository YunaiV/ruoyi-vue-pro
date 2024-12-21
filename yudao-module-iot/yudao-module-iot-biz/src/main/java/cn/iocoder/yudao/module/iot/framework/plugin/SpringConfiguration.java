package cn.iocoder.yudao.module.iot.framework.plugin;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringConfiguration {

    @Bean
    @DependsOn("serviceRegistryInitializedMarker")
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }

}