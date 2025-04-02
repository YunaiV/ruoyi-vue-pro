package cn.iocoder.yudao.module.iot.component.http.config;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.component.core.config.IotComponentCommonProperties;
import cn.iocoder.yudao.module.iot.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.component.core.heartbeat.IotComponentRegistry;
import cn.iocoder.yudao.module.iot.component.http.downstream.IotDeviceDownstreamHandlerImpl;
import cn.iocoder.yudao.module.iot.component.http.upstream.IotDeviceUpstreamServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import java.lang.management.ManagementFactory;

/**
 * IoT 组件 HTTP 的自动配置类
 *
 * @author haohao
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(IotComponentHttpProperties.class)
@ConditionalOnProperty(prefix = "yudao.iot.component.http", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = {
        "cn.iocoder.yudao.module.iot.component.core", // 核心包
        "cn.iocoder.yudao.module.iot.component.http" // HTTP组件包
})
public class IotComponentHttpAutoConfiguration {

    /**
     * 组件key
     */
    private static final String PLUGIN_KEY = "http";

    public IotComponentHttpAutoConfiguration() {
        log.info("[IotComponentHttpAutoConfiguration][已启动]");
    }

    @EventListener(ApplicationStartedEvent.class)
    public void initialize(ApplicationStartedEvent event) {
        // 从应用上下文中获取需要的Bean
        IotComponentRegistry componentRegistry = event.getApplicationContext().getBean(IotComponentRegistry.class);
        IotComponentCommonProperties commonProperties = event.getApplicationContext()
                .getBean(IotComponentCommonProperties.class);

        // 设置当前组件的核心标识
        commonProperties.setPluginKey(PLUGIN_KEY);

        // 将HTTP组件注册到组件注册表
        componentRegistry.registerComponent(
                PLUGIN_KEY,
                SystemUtil.getHostInfo().getAddress(),
                0, // 内嵌模式固定为0
                getProcessId());

        log.info("[initialize][IoT HTTP 组件初始化完成]");
    }

    @Bean(name = "httpDeviceUpstreamServer", initMethod = "start", destroyMethod = "stop")
    public IotDeviceUpstreamServer deviceUpstreamServer(IotDeviceUpstreamApi deviceUpstreamApi,
                                                        IotComponentHttpProperties properties,
                                                        ApplicationContext applicationContext,
                                                        IotComponentRegistry componentRegistry) {
        return new IotDeviceUpstreamServer(properties, deviceUpstreamApi, applicationContext, componentRegistry);
    }

    @Bean(name = "httpDeviceDownstreamHandler")
    public IotDeviceDownstreamHandler deviceDownstreamHandler() {
        return new IotDeviceDownstreamHandlerImpl();
    }

    /**
     * 获取当前进程ID
     *
     * @return 进程ID
     */
    private String getProcessId() {
        // 获取进程的 name
        String name = ManagementFactory.getRuntimeMXBean().getName();
        // 分割名称，格式为 pid@hostname
        String pid = name.split("@")[0];
        return pid;
    }
}
