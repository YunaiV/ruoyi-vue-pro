package cn.iocoder.yudao.framework.tenant.config;

import cn.iocoder.yudao.framework.tenant.core.mq.TenantRedisMessageInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多租户针对 MQ 的自动配置
 *
 * @author 芋道源码
 */
@Configuration
// 允许使用 yudao.tenant.enable=false 禁用多租户
@ConditionalOnProperty(prefix = "yudao.tenant", value = "enable", matchIfMissing = true)
public class YudaoTenantMQAutoConfiguration {

    @Bean
    public TenantRedisMessageInterceptor tenantRedisMessageInterceptor() {
        return new TenantRedisMessageInterceptor();
    }

}
