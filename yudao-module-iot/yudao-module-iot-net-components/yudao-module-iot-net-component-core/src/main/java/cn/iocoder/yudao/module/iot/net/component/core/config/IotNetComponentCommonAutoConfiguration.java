package cn.iocoder.yudao.module.iot.net.component.core.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamServer;
import cn.iocoder.yudao.module.iot.net.component.core.heartbeat.IotNetComponentInstanceHeartbeatJob;
import cn.iocoder.yudao.module.iot.net.component.core.heartbeat.IotNetComponentRegistry;
import cn.iocoder.yudao.module.iot.net.component.core.upstream.IotDeviceUpstreamClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IoT 网络组件的通用自动配置类
 *
 * @author haohao
 */
@AutoConfiguration
@EnableConfigurationProperties(IotNetComponentCommonProperties.class)
@EnableScheduling // 开启定时任务，因为 IotNetComponentInstanceHeartbeatJob 是一个定时任务
public class IotNetComponentCommonAutoConfiguration {

    /**
     * 创建 EMQX 设备下行服务器
     * <p>
     * 当 yudao.iot.component.emqx.enabled = true 时，优先使用 emqxDeviceDownstreamHandler
     */
    @Bean
    @ConditionalOnProperty(prefix = "yudao.iot.component.emqx", name = "enabled", havingValue = "true")
    public IotDeviceDownstreamServer emqxDeviceDownstreamServer(
            IotNetComponentCommonProperties properties,
            @Qualifier("emqxDeviceDownstreamHandler") IotDeviceDownstreamHandler deviceDownstreamHandler) {
        return new IotDeviceDownstreamServer(properties, deviceDownstreamHandler);
    }

    /**
     * 创建网络组件实例心跳任务
     */
    @Bean(initMethod = "init", destroyMethod = "stop")
    public IotNetComponentInstanceHeartbeatJob pluginInstanceHeartbeatJob(
            IotDeviceUpstreamApi deviceUpstreamApi,
            IotNetComponentCommonProperties commonProperties,
            IotNetComponentRegistry componentRegistry) {
        return new IotNetComponentInstanceHeartbeatJob(
                deviceUpstreamApi,
                commonProperties,
                componentRegistry);
    }

    /**
     * 创建设备上行客户端
     */
    @Bean
    public IotDeviceUpstreamClient deviceUpstreamClient() {
        return new IotDeviceUpstreamClient();
    }
}