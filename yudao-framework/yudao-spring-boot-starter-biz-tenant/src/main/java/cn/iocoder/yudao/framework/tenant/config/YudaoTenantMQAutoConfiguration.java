package cn.iocoder.yudao.framework.tenant.config;

import cn.iocoder.yudao.framework.tenant.core.mq.TenantRedisMessageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多租户针对 MQ 的自动配置
 *
 * @author 芋道源码
 */
@Configuration
public class YudaoTenantMQAutoConfiguration {

    @Bean
    public TenantRedisMessageInterceptor tenantRedisMessageInterceptor() {
        return new TenantRedisMessageInterceptor();
    }

}
