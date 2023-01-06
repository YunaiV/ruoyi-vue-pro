package cn.iocoder.yudao.module.visualization.framework.jmreport.config;

import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import cn.iocoder.yudao.module.system.api.oauth2.OAuth2TokenApi;
import cn.iocoder.yudao.module.visualization.framework.jmreport.core.service.JmReportTokenServiceImpl;
import cn.iocoder.yudao.module.visualization.framework.jmreport.core.web.JmReportTokenFilter;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 积木报表的配置类
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "org.jeecg.modules.jmreport") // 扫描积木报表的包
public class JmReportConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JmReportTokenServiceI jmReportTokenService(OAuth2TokenApi oAuth2TokenApi) {
        return new JmReportTokenServiceImpl(oAuth2TokenApi);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public FilterRegistrationBean<JmReportTokenFilter> registerMyAnotherFilter(OAuth2TokenApi oAuth2TokenApi){
        FilterRegistrationBean<JmReportTokenFilter> bean = new FilterRegistrationBean<>();
        bean.setOrder(WebFilterOrderEnum.JM_TOKEN_FILTER);
        bean.setFilter(new JmReportTokenFilter(oAuth2TokenApi));
        return bean;
    }
}
