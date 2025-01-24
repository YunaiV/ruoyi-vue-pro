package cn.iocoder.yudao.module.iot.plugin.http.config;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpVertxPluginConfiguration {

    @Bean(initMethod = "start")
    public HttpVertxPlugin httpVertxPlugin() {
        PluginWrapper pluginWrapper = new PluginWrapper(new DefaultPluginManager(), null, null, null);
        return new HttpVertxPlugin(pluginWrapper);
    }
} 