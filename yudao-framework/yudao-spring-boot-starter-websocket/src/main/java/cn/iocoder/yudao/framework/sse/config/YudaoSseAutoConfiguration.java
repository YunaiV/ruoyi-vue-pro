package cn.iocoder.yudao.framework.sse.config;

import cn.iocoder.yudao.framework.sse.core.sender.kafka.KafkaSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.kafka.KafkaSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.local.LocalSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.rabbitmq.RabbitMQSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.rabbitmq.RabbitMQSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.redis.RedisSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.redis.RedisSseMessageSender;
import cn.iocoder.yudao.framework.sse.core.sender.rocketmq.RocketMQSseMessageConsumer;
import cn.iocoder.yudao.framework.sse.core.sender.rocketmq.RocketMQSseMessageSender;
import cn.iocoder.yudao.framework.mq.redis.config.YudaoRedisMQConsumerAutoConfiguration;
import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.sse.core.controller.SseController;
import cn.iocoder.yudao.framework.sse.core.session.SseEmitterSessionManager;
import cn.iocoder.yudao.framework.sse.core.scheduler.SseHeartbeatScheduler;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

/**
 * SSE 自动配置，默认关闭，通过 yudao.sse.enable=true 开启
 *
 * 1. 消息发送器按 yudao.sse.sender-type 创建，MQ 使用 yudao.sse.sender-xxx 配置，与 WebSocket 的发送器、消息、Consumer 相互独立
 * 2. Bean 名称统一使用 yudaoSse 前缀，与 AI 等模块自有的 SSE 生成流隔离
 *
 * @author 芋道源码
 */
@AutoConfiguration(before = YudaoRedisMQConsumerAutoConfiguration.class) // before YudaoRedisMQConsumerAutoConfiguration 的原因是，需要保证 RedisSseMessageConsumer 先创建，才能创建 RedisMessageListenerContainer
@ConditionalOnProperty(prefix = "yudao.sse", name = "enable", havingValue = "true")
@EnableConfigurationProperties(SseProperties.class)
public class YudaoSseAutoConfiguration {

    @Bean
    public SseEmitterSessionManager yudaoSseEmitterSessionManager(SseProperties sseProperties) {
        return new SseEmitterSessionManager(sseProperties.getSendTimeout().toMillis());
    }

    @Bean
    public SseHeartbeatScheduler yudaoSseHeartbeatScheduler(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                                            SseProperties sseProperties) {
        return new SseHeartbeatScheduler(yudaoSseEmitterSessionManager,
                sseProperties.getHeartbeatInterval().toMillis(), sseProperties.getSendTimeout().toMillis());
    }

    @Bean
    public SseController yudaoSseController(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                            SseProperties sseProperties) {
        return new SseController(yudaoSseEmitterSessionManager, sseProperties);
    }

    // ==================== Sender 相关 ====================

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "yudao.sse", name = "sender-type", havingValue = "local", matchIfMissing = true)
    public static class LocalSseMessageSenderConfiguration {

        @Bean
        public LocalSseMessageSender yudaoSseMessageSender(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                                           SseProperties sseProperties) {
            return new LocalSseMessageSender(yudaoSseEmitterSessionManager,
                    sseProperties.getSendTimeout().toMillis(), sseProperties.getMaxSendFailures());
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "yudao.sse", name = "sender-type", havingValue = "redis")
    public static class RedisSseMessageSenderConfiguration {

        @Bean
        public RedisSseMessageSender yudaoSseMessageSender(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                                           SseProperties sseProperties, RedisMQTemplate redisMQTemplate) {
            return new RedisSseMessageSender(yudaoSseEmitterSessionManager,
                    sseProperties.getSendTimeout().toMillis(), sseProperties.getMaxSendFailures(), redisMQTemplate);
        }

        @Bean
        public RedisSseMessageConsumer yudaoSseRedisMessageConsumer(RedisSseMessageSender yudaoSseMessageSender) {
            return new RedisSseMessageConsumer(yudaoSseMessageSender);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "yudao.sse", name = "sender-type", havingValue = "rocketmq")
    public static class RocketMQSseMessageSenderConfiguration {

        @Bean
        public RocketMQSseMessageSender yudaoSseMessageSender(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                                              SseProperties sseProperties, RocketMQTemplate rocketMQTemplate) {
            String topic = sseProperties.getSenderRocketmq().getTopic();
            Assert.hasText(topic, "yudao.sse.sender-rocketmq.topic 不能为空");
            return new RocketMQSseMessageSender(yudaoSseEmitterSessionManager,
                    sseProperties.getSendTimeout().toMillis(), sseProperties.getMaxSendFailures(), rocketMQTemplate, topic);
        }

        @Bean
        public RocketMQSseMessageConsumer yudaoSseRocketMQMessageConsumer(RocketMQSseMessageSender yudaoSseMessageSender) {
            return new RocketMQSseMessageConsumer(yudaoSseMessageSender);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "yudao.sse", name = "sender-type", havingValue = "rabbitmq")
    public static class RabbitMQSseMessageSenderConfiguration {

        @Bean
        public RabbitMQSseMessageSender yudaoSseMessageSender(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                                              SseProperties sseProperties, RabbitTemplate rabbitTemplate,
                                                              @Qualifier("yudaoSseTopicExchange") TopicExchange yudaoSseTopicExchange) {
            return new RabbitMQSseMessageSender(yudaoSseEmitterSessionManager,
                    sseProperties.getSendTimeout().toMillis(), sseProperties.getMaxSendFailures(), rabbitTemplate, yudaoSseTopicExchange);
        }

        @Bean
        public RabbitMQSseMessageConsumer yudaoSseRabbitMQMessageConsumer(RabbitMQSseMessageSender yudaoSseMessageSender) {
            return new RabbitMQSseMessageConsumer(yudaoSseMessageSender);
        }

        /**
         * 创建 SSE 使用的 Topic Exchange
         */
        @Bean
        public TopicExchange yudaoSseTopicExchange(SseProperties sseProperties) {
            String exchange = sseProperties.getSenderRabbitmq().getExchange();
            Assert.hasText(exchange, "yudao.sse.sender-rabbitmq.exchange 不能为空");
            return new TopicExchange(exchange,
                    true,  // durable: 是否持久化
                    false);  // exclusive: 是否排它
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "yudao.sse", name = "sender-type", havingValue = "kafka")
    public static class KafkaSseMessageSenderConfiguration {

        @Bean
        public KafkaSseMessageSender yudaoSseMessageSender(SseEmitterSessionManager yudaoSseEmitterSessionManager,
                                                           SseProperties sseProperties, KafkaTemplate<Object, Object> kafkaTemplate) {
            String topic = sseProperties.getSenderKafka().getTopic();
            Assert.hasText(topic, "yudao.sse.sender-kafka.topic 不能为空");
            return new KafkaSseMessageSender(yudaoSseEmitterSessionManager,
                    sseProperties.getSendTimeout().toMillis(), sseProperties.getMaxSendFailures(), kafkaTemplate, topic);
        }

        @Bean
        public KafkaSseMessageConsumer yudaoSseKafkaMessageConsumer(KafkaSseMessageSender yudaoSseMessageSender) {
            return new KafkaSseMessageConsumer(yudaoSseMessageSender);
        }

    }

}
