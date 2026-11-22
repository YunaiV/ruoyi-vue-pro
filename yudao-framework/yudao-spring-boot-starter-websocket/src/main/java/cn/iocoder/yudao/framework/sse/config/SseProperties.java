package cn.iocoder.yudao.framework.sse.config;

import cn.iocoder.yudao.framework.websocket.config.WebSocketProperties;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * SSE 配置项
 *
 * @author 芋道源码
 */
@ConfigurationProperties("yudao.sse")
@Data
@Validated
public class SseProperties {

    /**
     * 是否开启 SSE，默认关闭
     */
    @NotNull(message = "SSE 是否开启不能为空")
    private Boolean enable = false;

    /**
     * SSE 的接口路径前缀，提供 {path}/connect、{path}/close 两个接口
     */
    @NotEmpty(message = "SSE 的接口路径不能为空")
    private String path = "/infra/sse";

    /**
     * 消息发送器的类型
     *
     * 可选值：local、redis、rocketmq、kafka、rabbitmq。与 yudao.websocket.sender-type 一致使用配置字符串，
     * 便于 @ConditionalOnProperty 直接匹配，两套配置各自独立
     */
    @NotNull(message = "SSE 的消息发送者不能为空")
    @Pattern(regexp = "(?i)local|redis|rocketmq|kafka|rabbitmq", message = "SSE 的消息发送者必须是 local、redis、rocketmq、kafka、rabbitmq")
    private String senderType = "local";

    /**
     * 心跳间隔，需小于代理（例如说 Nginx）的空闲超时
     */
    @NotNull(message = "SSE 的心跳间隔不能为空")
    @DurationMin(millis = 1, message = "SSE 的心跳间隔必须大于 0")
    private Duration heartbeatInterval = Duration.ofSeconds(25);

    /**
     * SseEmitter 的超时时间，到期后由客户端按 retry 重连
     */
    @NotNull(message = "SSE 的连接超时时间不能为空")
    @DurationMin(millis = 1, message = "SSE 的连接超时时间必须大于 0")
    private Duration emitterTimeout = Duration.ofMinutes(30);

    /**
     * 客户端断线后的重连间隔，通过 retry 字段下发给客户端
     */
    @NotNull(message = "SSE 的重连间隔不能为空")
    @DurationMin(millis = 1, message = "SSE 的重连间隔必须大于 0")
    private Duration retryInterval = Duration.ofSeconds(5);

    /**
     * 单次发送等待连接锁的超时时间，避免慢客户端阻塞 MQ 消费线程
     */
    @NotNull(message = "SSE 的发送超时时间不能为空")
    @DurationMin(millis = 1, message = "SSE 的发送超时时间必须大于 0")
    private Duration sendTimeout = Duration.ofSeconds(3);

    /**
     * 连续发送失败（含等待锁超时）多少次后断开连接
     */
    @NotNull(message = "SSE 的最大连续失败次数不能为空")
    @Min(value = 1, message = "SSE 的最大连续失败次数不能小于 1")
    private Integer maxSendFailures = 3;

    /**
     * RocketMQ 消息发送器的配置，sender-type 为 rocketmq 时使用
     */
    private WebSocketProperties.SenderRocketMQ senderRocketmq = new WebSocketProperties.SenderRocketMQ();
    /**
     * RabbitMQ 消息发送器的配置，sender-type 为 rabbitmq 时使用
     */
    private WebSocketProperties.SenderRabbitMQ senderRabbitmq = new WebSocketProperties.SenderRabbitMQ();
    /**
     * Kafka 消息发送器的配置，sender-type 为 kafka 时使用
     */
    private WebSocketProperties.SenderKafka senderKafka = new WebSocketProperties.SenderKafka();

}
