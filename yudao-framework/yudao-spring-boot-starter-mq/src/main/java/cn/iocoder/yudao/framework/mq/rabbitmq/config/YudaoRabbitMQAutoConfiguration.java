package cn.iocoder.yudao.framework.mq.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * RabbitMQ 消息队列配置类
 *
 * @author 芋道源码
 */
@AutoConfiguration
@Slf4j
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
public class YudaoRabbitMQAutoConfiguration {

    /**
     * Jackson2JsonMessageConverter Bean：使用 jackson 序列化消息
     */
    @Bean
    public MessageConverter createMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
