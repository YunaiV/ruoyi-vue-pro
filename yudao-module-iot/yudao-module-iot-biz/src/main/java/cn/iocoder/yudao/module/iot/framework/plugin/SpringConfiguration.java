package cn.iocoder.yudao.module.iot.framework.plugin;

import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.Greetings;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringConfiguration {

    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }

    @Bean
    @DependsOn("pluginManager")
    public Greetings greetings() {
        return new Greetings();
    }

}