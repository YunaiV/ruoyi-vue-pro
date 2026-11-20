package cn.iocoder.yudao.module.iot.core.messagebus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

/**
 * IoT 消息总线配置属性
 *
 * @author 芋道源码
 */
@ConfigurationProperties("yudao.iot.message-bus")
@Data
@Validated
public class IotMessageBusProperties {

    /**
     * 消息总线类型
     *
     * 可选值：local、redis、rocketmq、kafka、rabbitmq
     */
    @NotNull(message = "IoT 消息总线类型不能为空")
    private String type = "local";

    /**
     * 确认发送（{@link cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus#postConfirmed}）等待 broker 确认的超时时间
     *
     * 目前用于 RabbitMQ 的 publisher confirm；其它消息总线由各自客户端的超时参数限定（Redis 的命令超时、
     * Kafka 的 max.block.ms 与 delivery.timeout.ms、RocketMQ 的 send-message-timeout）
     */
    @NotNull(message = "IoT 消息总线确认发送的超时时间不能为空")
    private Duration confirmTimeout = Duration.ofSeconds(10);

}
