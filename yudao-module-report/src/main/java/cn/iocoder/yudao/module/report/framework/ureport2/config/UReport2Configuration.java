package cn.iocoder.yudao.module.report.framework.ureport2.config;

import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.module.report.dal.redis.ureport2.UReport2FileVersionRedisDAO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import cn.iocoder.yudao.module.report.framework.ureport2.core.provider.UReport2DatabaseReportProvider;
import cn.iocoder.yudao.module.report.framework.ureport2.core.web.UReport2SecurityFilter;
import cn.iocoder.yudao.module.report.service.ureport2.UReport2FileService;
import com.bstek.ureport.console.UReportServlet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UReport2 报表的配置类
 *
 * UReport2 本身由 ureport2-spring-boot-starter 自动配置，这里只补充芋道相关的扩展：
 * 1. 基于数据库、按租户隔离的模板存储
 * 2. 按租户隔离、支持多实例的模板缓存
 * 3. /ureport/** 请求的租户与权限校验
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "ureport", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UReport2Configuration {

    @Bean
    public UReport2ReportDefinitionCache ureport2ReportDefinitionCache(UReport2FileVersionRedisDAO fileVersionRedisDAO) {
        return new UReport2ReportDefinitionCache(fileVersionRedisDAO);
    }

    @Bean
    public UReport2DatabaseReportProvider ureport2DatabaseReportProvider(UReport2FileService ureport2FileService,
                                                                         UReport2ReportDefinitionCache reportDefinitionCache) {
        return new UReport2DatabaseReportProvider(ureport2FileService, reportDefinitionCache);
    }

    @Bean
    public FilterRegistrationBean<UReport2SecurityFilter> ureport2SecurityFilter(SecurityFrameworkService securityFrameworkService,
                                                                                 ObjectProvider<TenantFrameworkService> tenantFrameworkService,
                                                                                 GlobalExceptionHandler globalExceptionHandler) {
        FilterRegistrationBean<UReport2SecurityFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UReport2SecurityFilter(securityFrameworkService,
                tenantFrameworkService.getIfAvailable(), globalExceptionHandler));
        registrationBean.addUrlPatterns(UReportServlet.URL + "/*");
        // 需要在 Spring Security 过滤器之后，保证已经完成 token 认证；与 TenantSecurityWebFilter 同序，二者处理的 URL 不重叠
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_SECURITY_FILTER);
        return registrationBean;
    }

}
