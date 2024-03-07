package cn.iocoder.yudao.framework.tenant.core.mq.kafka;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * Kafka 消息队列的多租户 {@link ProducerInterceptor} 实现类
 *
 * 1. Producer 发送消息时，将 {@link TenantContextHolder} 租户编号，添加到消息的 Header 中
 * 2. Consumer 消费消息时，将消息的 Header 的租户编号，添加到 {@link TenantContextHolder} 中，通过 {@link InvocableHandlerMethod} 实现
 *
 * @author 芋道源码
 */
public class TenantKafkaProducerInterceptor implements ProducerInterceptor<Object, Object> {

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            Headers headers = (Headers) ReflectUtil.getFieldValue(record, "headers"); // private 属性，没有 get 方法，智能反射
            headers.add(HEADER_TENANT_ID, tenantId.toString().getBytes());
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

}
