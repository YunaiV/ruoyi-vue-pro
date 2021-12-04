package cn.iocoder.yudao.framework.tenant.config;

import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import cn.iocoder.yudao.framework.tenant.core.web.TenantWebFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 多租户针对 Web 的自动配置
 *
 * @author 芋道源码
 */
public class YudaoTenantWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<TenantWebFilter> tenantWebFilter() {
        FilterRegistrationBean<TenantWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantWebFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_FILTER);
        return registrationBean;
    }

}
