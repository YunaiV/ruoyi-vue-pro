package cn.iocoder.yudao.framework.websocket.config;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * WebSocket 配置项
 *
 * @author xingyu4j
 */
@ConfigurationProperties("yudao.websocket")
@Data
@Validated
public class WebSocketProperties {

    /**
     * WebSocket 的连接路径
     */
    @NotEmpty(message = "WebSocket 的连接路径不能为空")
    private String path = "/ws";

    /**
     * 消息发送器的类型
     *
     * 可选值：local、redis、rocketmq、kafka、rabbitmq。与原版一致使用配置字符串，
     * 便于 @ConditionalOnProperty 直接匹配，与 yudao.sse.sender-type 各自独立
     */
    @NotNull(message = "WebSocket 的消息发送者不能为空")
    @Pattern(regexp = "(?i)local|redis|rocketmq|kafka|rabbitmq", message = "WebSocket 的消息发送者必须是 local、redis、rocketmq、kafka、rabbitmq")
    private String senderType = "local";

    /**
     * RocketMQ 消息发送器的配置，sender-type 为 rocketmq 时使用
     */
    private SenderRocketMQ senderRocketmq = new SenderRocketMQ();
    /**
     * RabbitMQ 消息发送器的配置，sender-type 为 rabbitmq 时使用
     */
    private SenderRabbitMQ senderRabbitmq = new SenderRabbitMQ();
    /**
     * Kafka 消息发送器的配置，sender-type 为 kafka 时使用
     */
    private SenderKafka senderKafka = new SenderKafka();

    // ==================== MQ 消息发送器的配置：SseProperties 复用该结构 ====================

    /**
     * RocketMQ 消息发送器的配置
     */
    @Data
    public static class SenderRocketMQ {

        /**
         * 消息发送的 Topic
         */
        private String topic;
        /**
         * 消息消费的 Consumer Group，使用广播消费
         */
        private String consumerGroup;

    }

    /**
     * RabbitMQ 消息发送器的配置
     */
    @Data
    public static class SenderRabbitMQ {

        /**
         * 消息发送的 Exchange
         */
        private String exchange;
        /**
         * 消息消费的 Queue 名字前缀，每个节点会追加 UUID 后缀，以达到广播消费的目的
         */
        private String queue;

    }

    /**
     * Kafka 消息发送器的配置
     */
    @Data
    public static class SenderKafka {

        /**
         * 消息发送的 Topic
         */
        private String topic;
        /**
         * 消息消费的 Consumer Group 前缀，每个节点会追加 UUID 后缀，以达到广播消费的目的
         */
        private String consumerGroup;

    }

}
