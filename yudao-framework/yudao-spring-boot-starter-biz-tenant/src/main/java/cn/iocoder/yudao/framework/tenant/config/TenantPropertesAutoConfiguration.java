package cn.iocoder.yudao.framework.tenant.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class YudaoTenantAutoConfigurationOnDisable {

}
