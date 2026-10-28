package cn.iocoder.yudao.module.iot.framework.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * IoT 时序存储的配置类
 *
 * 由 yudao.iot.storage.type 选择存储后端，对应的 DAO 实现通过 @ConditionalOnProperty 装配
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(IotStorageProperties.class)
public class IotStorageConfiguration {
}
