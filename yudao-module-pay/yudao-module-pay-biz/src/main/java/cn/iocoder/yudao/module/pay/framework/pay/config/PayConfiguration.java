package cn.iocoder.yudao.module.pay.framework.pay.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PayProperties.class)
public class PayConfiguration {
}
