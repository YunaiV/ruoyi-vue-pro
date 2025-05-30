package cn.iocoder.yudao.module.iot.net.component.http.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBusSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.net.component.http.downstream.IotDeviceDownstreamHandlerImpl;
import cn.iocoder.yudao.module.iot.net.component.http.upstream.IotDeviceUpstreamServer;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
     * 初始化 HTTP 组件
     *
     * @param event 应用启动事件
     */
    @EventListener(ApplicationStartedEvent.class)
    public void initialize(ApplicationStartedEvent event) {
        log.info("[IotNetComponentHttpAutoConfiguration][开始初始化]");

        // TODO @芋艿：临时处理
        IotMessageBus messageBus = event.getApplicationContext()
                .getBean(IotMessageBus.class);
        messageBus.register(new IotMessageBusSubscriber<IotDeviceMessage>() {

            @Override
            public String getTopic() {
                return IotDeviceMessage.buildMessageBusGatewayDeviceMessageTopic("yy");
            }

            @Override
            public String getGroup() {
                return "test";
            }

            @Override
            public void onMessage(IotDeviceMessage message) {
                System.out.println(message);
            }

        });
    }

    // TODO @芋艿：貌似这里不用注册 bean？
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
     */
    @Bean(name = "httpDeviceUpstreamServer", initMethod = "start", destroyMethod = "stop")
    public IotDeviceUpstreamServer deviceUpstreamServer(
            @Lazy @Qualifier("httpVertx") Vertx vertx,
            IotDeviceUpstreamApi deviceUpstreamApi,
            IotNetComponentHttpProperties properties,
            IotDeviceMessageProducer deviceMessageProducer) {
        return new IotDeviceUpstreamServer(vertx, properties, deviceUpstreamApi, deviceMessageProducer);
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
