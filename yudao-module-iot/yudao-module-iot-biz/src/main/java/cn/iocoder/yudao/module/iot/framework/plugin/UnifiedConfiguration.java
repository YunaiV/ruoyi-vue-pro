package cn.iocoder.yudao.module.iot.framework.plugin;

import cn.iocoder.yudao.module.iot.framework.plugin.listener.CustomPluginStateListener;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO @芋艿：需要 review 下
@Slf4j
@Configuration
public class UnifiedConfiguration {

    @Value("${pf4j.pluginsDir:pluginsDir}")
    private String pluginsDir;

    @Bean
//    @DependsOn("deviceDataApiImpl")
    public SpringPluginManager pluginManager() {
        log.info("[init][实例化 SpringPluginManager]");
//        SpringPluginManager springPluginManager = new SpringPluginManager(Paths.get(pluginsDir)) {
        SpringPluginManager springPluginManager = new SpringPluginManager() {

            @Override
            public void startPlugins() {
                // 禁用插件启动，避免插件启动时，启动所有插件
                log.info("[init][禁用默认启动所有插件]");
            }

        };
        springPluginManager.addPluginStateListener(new CustomPluginStateListener());
        return springPluginManager;
    }

}