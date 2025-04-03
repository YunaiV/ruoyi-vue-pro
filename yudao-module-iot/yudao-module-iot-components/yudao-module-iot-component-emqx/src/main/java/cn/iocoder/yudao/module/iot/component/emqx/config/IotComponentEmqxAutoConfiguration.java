package cn.iocoder.yudao.module.iot.component.emqx.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.component.core.config.IotComponentCommonProperties;
import cn.iocoder.yudao.module.iot.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.component.core.heartbeat.IotComponentRegistry;
import cn.iocoder.yudao.module.iot.component.emqx.downstream.IotDeviceDownstreamHandlerImpl;
import cn.iocoder.yudao.module.iot.component.emqx.upstream.IotDeviceUpstreamServer;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import java.lang.management.ManagementFactory;

/**
 * IoT 组件 EMQX 的自动配置类
 *
 * @author haohao
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(IotComponentEmqxProperties.class)
@ConditionalOnProperty(prefix = "yudao.iot.component.emqx", name = "enabled", havingValue = "true", matchIfMissing = false)
// TODO @haohao：是不是不用扫 cn.iocoder.yudao.module.iot.component.core 拉，它尽量靠自动配置
@ComponentScan(basePackages = {
        "cn.iocoder.yudao.module.iot.component.core", // 核心包
        "cn.iocoder.yudao.module.iot.component.emqx" // EMQX 组件包
})
public class IotComponentEmqxAutoConfiguration {

    /**
     * 组件 key
     */
    private static final String PLUGIN_KEY = "emqx";

    public IotComponentEmqxAutoConfiguration() {
        // TODO @haohao：这个日志，融合到 initialize ？
        log.info("[IotComponentEmqxAutoConfiguration][已启动]");
    }

    @EventListener(ApplicationStartedEvent.class)
    public void initialize(ApplicationStartedEvent event) {
        // 从应用上下文中获取需要的 Bean
        IotComponentRegistry componentRegistry = event.getApplicationContext().getBean(IotComponentRegistry.class);
        IotComponentCommonProperties commonProperties = event.getApplicationContext().getBean(IotComponentCommonProperties.class);

        // 设置当前组件的核心标识
        // TODO @haohao：如果多个组件，都去设置，会不会冲突哈？
        commonProperties.setPluginKey(PLUGIN_KEY);

        // 将 EMQX 组件注册到组件注册表
        componentRegistry.registerComponent(
                PLUGIN_KEY,
                SystemUtil.getHostInfo().getAddress(),
                0, // 内嵌模式固定为 0
                getProcessId()
        );

        log.info("[initialize][IoT EMQX 组件初始化完成]");
    }

    // TODO @haohao：这个可能要注意，可能会有多个？冲突？
    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public MqttClient mqttClient(Vertx vertx, IotComponentEmqxProperties emqxProperties) {
        // TODO @haohao：这个日志，要不要去掉，避免过多哈
        log.info("MQTT配置: host={}, port={}, username={}, ssl={}",
                emqxProperties.getMqttHost(), emqxProperties.getMqttPort(),
                emqxProperties.getMqttUsername(), emqxProperties.getMqttSsl());

        MqttClientOptions options = new MqttClientOptions()
                .setClientId("yudao-iot-downstream-" + IdUtil.fastSimpleUUID())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword());
        // TODO @haohao：可以用 ObjUtil.default
        if (emqxProperties.getMqttSsl() != null) {
            options.setSsl(emqxProperties.getMqttSsl());
        } else {
            options.setSsl(false);
        }
        return MqttClient.create(vertx, options);
    }

    @Bean(name = "emqxDeviceUpstreamServer", initMethod = "start", destroyMethod = "stop")
    public IotDeviceUpstreamServer deviceUpstreamServer(IotDeviceUpstreamApi deviceUpstreamApi,
                                                        IotComponentEmqxProperties emqxProperties,
                                                        Vertx vertx,
                                                        MqttClient mqttClient,
                                                        IotComponentRegistry componentRegistry) {
        return new IotDeviceUpstreamServer(emqxProperties, deviceUpstreamApi, vertx, mqttClient, componentRegistry);
    }

    @Bean(name = "emqxDeviceDownstreamHandler")
    public IotDeviceDownstreamHandler deviceDownstreamHandler(MqttClient mqttClient) {
        return new IotDeviceDownstreamHandlerImpl(mqttClient);
    }

    // TODO @haohao：这个通用下一下哈。
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