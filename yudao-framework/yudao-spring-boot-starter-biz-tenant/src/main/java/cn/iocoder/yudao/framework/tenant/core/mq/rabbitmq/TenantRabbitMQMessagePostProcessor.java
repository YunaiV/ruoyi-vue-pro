package cn.iocoder.yudao.framework.tenant.core.mq.rabbitmq;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * RabbitMQ 消息队列的多租户 {@link ProducerInterceptor} 实现类
 *
 * 1. Producer 发送消息时，将 {@link TenantContextHolder} 租户编号，添加到消息的 Header 中
 * 2. Consumer 消费消息时，将消息的 Header 的租户编号，添加到 {@link TenantContextHolder} 中，通过 {@link InvocableHandlerMethod} 实现
 *
 * @author 芋道源码
 */
public class TenantRabbitMQMessagePostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            message.getMessageProperties().getHeaders().put(HEADER_TENANT_ID, tenantId);
        }
        return message;
    }

}
