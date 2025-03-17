package cn.iocoder.yudao.module.iot.plugin.common.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamServer;
import cn.iocoder.yudao.module.iot.plugin.common.heartbeat.IotPluginInstanceHeartbeatJob;
import cn.iocoder.yudao.module.iot.plugin.common.upstream.IotDeviceUpstreamClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * IoT 插件的通用自动配置类
 *
 * @author haohao
 */
@AutoConfiguration
@EnableConfigurationProperties(IotPluginCommonProperties.class)
@EnableScheduling // 开启定时任务，因为 IotPluginInstanceHeartbeatJob 是一个定时任务
public class IotPluginCommonAutoConfiguration {

    @Bean
    public RestTemplate restTemplate(IotPluginCommonProperties properties) {
        return new RestTemplateBuilder()
                .setConnectTimeout(properties.getUpstreamConnectTimeout())
                .setReadTimeout(properties.getUpstreamReadTimeout())
                .build();
    }

    @Bean
    public IotDeviceUpstreamApi deviceUpstreamApi(IotPluginCommonProperties properties,
                                                  RestTemplate restTemplate) {
        return new IotDeviceUpstreamClient(properties, restTemplate);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public IotDeviceDownstreamServer deviceDownstreamServer(IotPluginCommonProperties properties,
                                                            IotDeviceDownstreamHandler deviceDownstreamHandler) {
        return new IotDeviceDownstreamServer(properties, deviceDownstreamHandler);
    }

    @Bean(initMethod = "init", destroyMethod = "stop")
    public IotPluginInstanceHeartbeatJob pluginInstanceHeartbeatJob(IotDeviceUpstreamApi deviceDataApi,
                                                                    IotDeviceDownstreamServer deviceDownstreamServer,
                                                                    IotPluginCommonProperties commonProperties) {
        return new IotPluginInstanceHeartbeatJob(deviceDataApi, deviceDownstreamServer, commonProperties);
    }

}