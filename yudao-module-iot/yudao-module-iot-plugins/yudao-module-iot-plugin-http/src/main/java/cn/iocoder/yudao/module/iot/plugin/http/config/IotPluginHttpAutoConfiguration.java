package cn.iocoder.yudao.module.iot.plugin.http.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.http.downstream.IotDeviceDownstreamHandlerImpl;
import cn.iocoder.yudao.module.iot.plugin.http.upstream.IotDeviceUpstreamServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IoT 插件 HTTP 的专用自动配置类
 *
 * @author haohao
 */
@Configuration
@EnableConfigurationProperties(IotPluginHttpProperties.class)
public class IotPluginHttpAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public IotDeviceUpstreamServer deviceUpstreamServer(IotDeviceUpstreamApi deviceUpstreamApi,
                                                        IotPluginHttpProperties properties) {
        return new IotDeviceUpstreamServer(properties, deviceUpstreamApi);
    }

    @Bean
    public IotDeviceDownstreamHandler deviceDownstreamHandler() {
        return new IotDeviceDownstreamHandlerImpl();
    }

}
