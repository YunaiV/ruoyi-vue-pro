package cn.iocoder.yudao.module.iot.net.component.http.config;

import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.net.component.http.downstream.IotDeviceDownstreamHandlerImpl;
import cn.iocoder.yudao.module.iot.net.component.http.upstream.IotDeviceUpstreamServer;
import cn.iocoder.yudao.module.iot.net.component.core.config.IotNetComponentCommonProperties;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.net.component.core.heartbeat.IotNetComponentRegistry;
import cn.iocoder.yudao.module.iot.net.component.core.util.IotNetComponentCommonUtils;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;

/**
 * IoT 网络组件 HTTP 的自动配置类
 *
 * @author haohao
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(IotNetComponentHttpProperties.class)
@ConditionalOnProperty(prefix = "yudao.iot.component.http", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = {
        "cn.iocoder.yudao.module.iot.net.component.http" // 只扫描 HTTP 组件包
})
public class IotNetComponentHttpAutoConfiguration {

    /**
     * 组件 key
     */
    private static final String PLUGIN_KEY = "http";

    public IotNetComponentHttpAutoConfiguration() {
        // 构造函数中不输出日志，移到 initialize 方法中
    }

    /**
     * 初始化 HTTP 组件
     *
     * @param event 应用启动事件
     */
    @EventListener(ApplicationStartedEvent.class)
    public void initialize(ApplicationStartedEvent event) {
        log.info("[IotNetComponentHttpAutoConfiguration][开始初始化]");

        // 从应用上下文中获取需要的 Bean
        IotNetComponentRegistry componentRegistry = event.getApplicationContext()
                .getBean(IotNetComponentRegistry.class);
        IotNetComponentCommonProperties commonProperties = event.getApplicationContext()
                .getBean(IotNetComponentCommonProperties.class);

        // 设置当前组件的核心标识
        // 注意：这里只为当前 HTTP 组件设置 pluginKey，不影响其他组件
        // TODO @haohao：多个会存在冲突的问题哇？
        commonProperties.setPluginKey(PLUGIN_KEY);

        // 将 HTTP 组件注册到组件注册表
        componentRegistry.registerComponent(
                PLUGIN_KEY,
                SystemUtil.getHostInfo().getAddress(),
                0, // 内嵌模式固定为 0：自动生成对应的 port 端口号
                IotNetComponentCommonUtils.getProcessId());

        log.info("[initialize][IoT HTTP 组件初始化完成]");
    }

    /**
     * 创建 Vert.x 实例
     *
     * @return Vert.x 实例
     */
    @Bean(name = "httpVertx")
    public Vertx vertx() {
        return Vertx.vertx();
    }

    /**
     * 创建设备上行服务器
     *
     * @param vertx              Vert.x 实例
     * @param deviceUpstreamApi  设备上行 API
     * @param properties         HTTP 组件配置属性
     * @param applicationContext 应用上下文
     * @return 设备上行服务器
     */
    @Bean(name = "httpDeviceUpstreamServer", initMethod = "start", destroyMethod = "stop")
    public IotDeviceUpstreamServer deviceUpstreamServer(
            @Lazy @Qualifier("httpVertx") Vertx vertx,
            IotDeviceUpstreamApi deviceUpstreamApi,
            IotNetComponentHttpProperties properties,
            ApplicationContext applicationContext) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP 服务器配置: port={}", properties.getServerPort());
        } else {
            log.info("HTTP 服务器将监听端口: {}", properties.getServerPort());
        }
        return new IotDeviceUpstreamServer(vertx, properties, deviceUpstreamApi, applicationContext);
    }

    /**
     * 创建设备下行处理器
     *
     * @return 设备下行处理器
     */
    @Bean(name = "httpDeviceDownstreamHandler")
    public IotDeviceDownstreamHandler deviceDownstreamHandler() {
        return new IotDeviceDownstreamHandlerImpl();
    }

}
