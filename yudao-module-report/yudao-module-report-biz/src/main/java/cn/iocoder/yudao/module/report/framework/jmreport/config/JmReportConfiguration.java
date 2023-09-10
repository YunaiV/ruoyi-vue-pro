package cn.iocoder.yudao.module.report.framework.jmreport.config;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.tenant.core.TenantLineInnerCustomizer;
import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmReportTokenServiceImpl;
import cn.iocoder.yudao.module.system.api.oauth2.OAuth2TokenApi;
import com.google.common.collect.Sets;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

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
    public JmReportTokenServiceI jmReportTokenService(OAuth2TokenApi oAuth2TokenApi,
        SecurityProperties securityProperties) {
        return new JmReportTokenServiceImpl(oAuth2TokenApi, securityProperties);
    }

    @Bean
    public TenantLineInnerCustomizer jmTenantLineInnerCustomizer() {
        return interceptor -> {
            // 积木报表，不需要多租户
            Set<String> ignoreTable =
                SetUtils.asSet("jimu_dict", "jimu_dict_item", "jimu_report", "jimu_report_data_source",
                    "jimu_report_db", "jimu_report_db_field", "jimu_report_db_param", "jimu_report_link",
                    "jimu_report_map", "jimu_report_share");
            interceptor.addIgnoreTables(ignoreTable);
        };
    }
}