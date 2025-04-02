package cn.iocoder.yudao.module.iot.component.core.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.component.core.downstream.IotDeviceDownstreamServer;
import cn.iocoder.yudao.module.iot.component.core.heartbeat.IotComponentInstanceHeartbeatJob;
import cn.iocoder.yudao.module.iot.component.core.heartbeat.IotComponentRegistry;
import cn.iocoder.yudao.module.iot.component.core.upstream.IotDeviceUpstreamClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IoT 组件的通用自动配置类
 *
 * @author haohao
 */
@AutoConfiguration
@EnableConfigurationProperties(IotComponentCommonProperties.class)
@EnableScheduling // 开启定时任务，因为 IotComponentInstanceHeartbeatJob 是一个定时任务
public class IotComponentCommonAutoConfiguration {

    /**
     * 创建EMQX设备下行服务器
     * 当yudao.iot.component.emqx.enabled=true时，使用emqxDeviceDownstreamHandler
     */
    @Bean
    @ConditionalOnProperty(prefix = "yudao.iot.component.emqx", name = "enabled", havingValue = "true")
    public IotDeviceDownstreamServer emqxDeviceDownstreamServer(IotComponentCommonProperties properties,
                                                                @Qualifier("emqxDeviceDownstreamHandler") IotDeviceDownstreamHandler deviceDownstreamHandler) {
        return new IotDeviceDownstreamServer(properties, deviceDownstreamHandler);
    }

    @Bean(initMethod = "init", destroyMethod = "stop")
    public IotComponentInstanceHeartbeatJob pluginInstanceHeartbeatJob(IotDeviceUpstreamApi deviceUpstreamApi,
                                                                       IotDeviceDownstreamServer deviceDownstreamServer,
                                                                       IotComponentCommonProperties commonProperties,
                                                                       IotComponentRegistry componentRegistry) {
        return new IotComponentInstanceHeartbeatJob(deviceUpstreamApi, deviceDownstreamServer, commonProperties,
                componentRegistry);
    }

    @Bean
    public IotDeviceUpstreamClient deviceUpstreamClient() {
        return new IotDeviceUpstreamClient();
    }
}