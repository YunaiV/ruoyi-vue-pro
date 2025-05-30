package cn.iocoder.yudao.module.iot.net.component.emqx.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.net.component.emqx.upstream.IotDeviceUpstreamServer;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

/**
 * IoT 网络组件 EMQX 的自动配置类
 *
 * @author haohao
 */
@AutoConfiguration
@EnableConfigurationProperties(IotNetComponentEmqxProperties.class)
@ConditionalOnProperty(prefix = "yudao.iot.component.emqx", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = {
        "cn.iocoder.yudao.module.iot.net.component.emqx" // 只扫描 EMQX 组件包
}) // TODO @haohao：自动配置后，不需要这个哈。
@Slf4j
public class IotNetComponentEmqxAutoConfiguration {

    /**
     * 初始化 EMQX 组件
     *
     * @param event 应用启动事件
     */
    @EventListener(ApplicationStartedEvent.class)
    public void initialize(ApplicationStartedEvent event) {
        log.info("[IotNetComponentEmqxAutoConfiguration][开始初始化]");

        // 从应用上下文中获取需要的 Bean
        // TODO @芋艿：看看要不要监听下

        log.info("[initialize][IoT EMQX 组件初始化完成]");
    }

    /**
     * 创建 Vert.x 实例
     */
    @Bean(name = "emqxVertx")
    public Vertx vertx() {
        return Vertx.vertx();
    }

    /**
     * 创建 MQTT 客户端
     */
    @Bean
    public MqttClient mqttClient(@Qualifier("emqxVertx") Vertx vertx, IotNetComponentEmqxProperties emqxProperties) {
        MqttClientOptions options = new MqttClientOptions()
                .setClientId("yudao-iot-downstream-" + IdUtil.fastSimpleUUID())
                .setUsername(emqxProperties.getMqttUsername())
                .setPassword(emqxProperties.getMqttPassword());
        // 设置 SSL 选项
        options.setSsl(ObjUtil.defaultIfNull(emqxProperties.getMqttSsl(), false));
        return MqttClient.create(vertx, options);
    }

    /**
     * 创建设备上行服务器
     */
    @Bean(name = "emqxDeviceUpstreamServer", initMethod = "start", destroyMethod = "stop")
    public IotDeviceUpstreamServer deviceUpstreamServer(
            IotDeviceUpstreamApi deviceUpstreamApi,
            IotNetComponentEmqxProperties emqxProperties,
            @Qualifier("emqxVertx") Vertx vertx,
            MqttClient mqttClient) {
        return new IotDeviceUpstreamServer(emqxProperties, deviceUpstreamApi, vertx, mqttClient);
    }

}