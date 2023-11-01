package cn.iocoder.yudao.framework.tenant.core.mq.kafka;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 多租户的 Kafka 的 {@link EnvironmentPostProcessor} 实现类
 *
 * Kafka Producer 发送消息时，增加 {@link TenantKafkaProducerInterceptor} 拦截器
 *
 * @author 芋道源码
 */
@Slf4j
public class TenantKafkaEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_KEY_INTERCEPTOR_CLASSES = "spring.kafka.producer.properties.interceptor.classes";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 添加 TenantKafkaProducerInterceptor 拦截器
        try {
            String value = environment.getProperty(PROPERTY_KEY_INTERCEPTOR_CLASSES);
            if (StrUtil.isEmpty(value)) {
                value = TenantKafkaProducerInterceptor.class.getName();
            } else {
                value += "," + TenantKafkaProducerInterceptor.class.getName();
            }
            environment.getSystemProperties().put(PROPERTY_KEY_INTERCEPTOR_CLASSES, value);
        } catch (NoClassDefFoundError ignore) {
            // 如果触发 NoClassDefFoundError 异常，说明 TenantKafkaProducerInterceptor 类不存在，即没引入 kafka-spring 依赖
        }
    }

}
