package cn.iocoder.yudao.module.iot.framework.plugin;

import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.framework.plugin.listener.CustomPluginStateListener;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class UnifiedConfiguration {

    private static final String SERVICE_REGISTRY_INITIALIZED_MARKER = "serviceRegistryInitializedMarker";

    @Resource
    private DeviceDataApi deviceDataApi;
    @Value("${pf4j.pluginsDir:pluginsDir}")
    private String pluginsDir;

    @Bean(SERVICE_REGISTRY_INITIALIZED_MARKER)
    public Object serviceRegistryInitializedMarker() {
        ServiceRegistry.registerService(DeviceDataApi.class, deviceDataApi);
        log.info("[init][将 DeviceDataApi 实例注册到 ServiceRegistry 中]");
        return new Object();
    }

    @Bean
    @DependsOn(SERVICE_REGISTRY_INITIALIZED_MARKER)
    public SpringPluginManager pluginManager() {
        log.info("[init][实例化 SpringPluginManager]");
        SpringPluginManager springPluginManager = new SpringPluginManager(Paths.get(pluginsDir)) {
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