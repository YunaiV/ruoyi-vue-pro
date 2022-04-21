package cn.iocoder.yudao.module.system.framework.sms;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsCodeProperties.class)
public class SmsCodeConfiguration {
}
