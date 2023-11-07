package cn.iocoder.yudao.framework.tenant.core.mq.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 多租户的 RocketMQ 初始化器
 *
 * @author 芋道源码
 */
public class TenantRocketMQInitializer implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultRocketMQListenerContainer) {
            DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) bean;
            initTenantConsumer(container.getConsumer());
        } else if (bean instanceof RocketMQTemplate) {
            RocketMQTemplate template = (RocketMQTemplate) bean;
            initTenantProducer(template.getProducer());
        }
        return bean;
    }

    private void initTenantProducer(DefaultMQProducer producer) {
        if (producer == null) {
            return;
        }
        DefaultMQProducerImpl producerImpl = producer.getDefaultMQProducerImpl();
        if (producerImpl == null) {
            return;
        }
        producerImpl.registerSendMessageHook(new TenantRocketMQSendMessageHook());
    }

    private void initTenantConsumer(DefaultMQPushConsumer consumer) {
        if (consumer == null) {
            return;
        }
        DefaultMQPushConsumerImpl consumerImpl = consumer.getDefaultMQPushConsumerImpl();
        if (consumerImpl == null) {
            return;
        }
        consumerImpl.registerConsumeMessageHook(new TenantRocketMQConsumeMessageHook());
    }

}