package cn.iocoder.yudao.module.iot.framework.plugin;

import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class UnifiedConfiguration {

    private static final String SERVICE_REGISTRY_INITIALIZED_MARKER = "serviceRegistryInitializedMarker";

    @Resource
    private DeviceDataApi deviceDataApi;

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
        return new SpringPluginManager();
    }

}