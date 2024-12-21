package cn.iocoder.yudao.module.iot.framework.plugin;

import cn.iocoder.yudao.module.iot.api.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Configuration
public class ServiceRegistryConfiguration {

    @Resource
    private DeviceDataApi deviceDataApi;

    @PostConstruct
    public void init() {
        // 将主程序中的 DeviceDataApi 实例注册到 ServiceRegistry
        ServiceRegistry.registerService(DeviceDataApi.class, deviceDataApi);
        log.info("[init][将 DeviceDataApi 实例注册到 ServiceRegistry 中]");
    }

    /**
     * 定义一个标记用的 Bean，用于表示 ServiceRegistry 已初始化完成
     */
    @Bean("serviceRegistryInitializedMarker")
    public Object serviceRegistryInitializedMarker() {
        // 返回任意对象即可，这里返回null都可以，但最好返回个实际对象
        return new Object();
    }
}