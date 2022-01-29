package cn.iocoder.yudao.module.member.framework.sms;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsCodeProperties.class)
public class SmsCodeConfiguration {
}
