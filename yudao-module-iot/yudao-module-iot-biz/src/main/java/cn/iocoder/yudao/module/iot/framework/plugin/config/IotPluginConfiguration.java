package cn.iocoder.yudao.module.iot.framework.plugin.config;

import cn.iocoder.yudao.module.iot.framework.plugin.core.IotPluginStartRunner;
import cn.iocoder.yudao.module.iot.framework.plugin.core.IotPluginStateListener;
import cn.iocoder.yudao.module.iot.service.plugin.IotPluginConfigService;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

/**
 * IoT 插件配置类
 *
 * @author haohao
 */
@Configuration
@Slf4j
public class IotPluginConfiguration {

    @Bean
    public IotPluginStartRunner pluginStartRunner(SpringPluginManager pluginManager,
                                                  IotPluginConfigService pluginConfigService) {
        return new IotPluginStartRunner(pluginManager, pluginConfigService);
    }

    // TODO @芋艿：需要 review 下
    @Bean
    public SpringPluginManager pluginManager(@Value("${pf4j.pluginsDir:pluginsDir}") String pluginsDir) {
        log.info("[init][实例化 SpringPluginManager]");
        SpringPluginManager springPluginManager = new SpringPluginManager(Paths.get(pluginsDir)) {

            @Override
            public void startPlugins() {
                // 禁用插件启动，避免插件启动时，启动所有插件
                log.info("[init][禁用默认启动所有插件]");
            }

        };
        springPluginManager.addPluginStateListener(new IotPluginStateListener());
        return springPluginManager;
    }

}