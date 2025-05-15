package cn.iocoder.yudao.module.report.framework.jmreport.config;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmOnlDragExternalServiceImpl;
import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmReportTokenServiceImpl;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 积木报表的配置类
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "org.jeecg.modules.jmreport") // 扫描积木报表的包
public class JmReportConfiguration {

    @Bean
    public JmReportTokenServiceI jmReportTokenService(OAuth2TokenCommonApi oAuth2TokenApi,
                                                      PermissionCommonApi permissionApi,
                                                      SecurityProperties securityProperties) {
        return new JmReportTokenServiceImpl(oAuth2TokenApi, permissionApi, securityProperties);
    }

    @Bean // 暂时注释：可以按需实现后打开
    @Primary
    public JmOnlDragExternalServiceImpl jmOnlDragExternalService2() {
        return new JmOnlDragExternalServiceImpl();
    }

}
