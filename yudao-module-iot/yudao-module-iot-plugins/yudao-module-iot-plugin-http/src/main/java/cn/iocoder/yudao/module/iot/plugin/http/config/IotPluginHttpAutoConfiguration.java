package cn.iocoder.yudao.module.iot.plugin.http.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.http.upstream.IotDeviceUpstreamServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 插件与独立运行都可复用的配置类
 */
@Configuration
public class IotPluginHttpAutoConfiguration {

    // TODO @haohao：这个要不要搞个配置类，更容易维护；
    /**
     * 可在 application.yml 中配置，默认端口 8092
     */
    @Value("${plugin.http.server.port:8092}")
    private Integer port;

    @Bean
    public IotDeviceUpstreamServer vertxService(IotDeviceUpstreamApi deviceUpstreamApi) {
        return new IotDeviceUpstreamServer(port, deviceUpstreamApi);
    }

}
