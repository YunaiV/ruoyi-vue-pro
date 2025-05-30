package cn.iocoder.yudao.module.iot.net.component.core.config;

import cn.iocoder.yudao.module.iot.net.component.core.upstream.IotDeviceUpstreamClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
     * 创建设备上行客户端
     */
    @Bean
    public IotDeviceUpstreamClient deviceUpstreamClient() {
        return new IotDeviceUpstreamClient();
    }

}