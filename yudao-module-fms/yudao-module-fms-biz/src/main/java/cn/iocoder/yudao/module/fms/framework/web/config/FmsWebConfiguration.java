package cn.iocoder.yudao.module.fms.framework.web.config;

import cn.iocoder.yudao.framework.swagger.config.YudaoSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * fms 模块的 web 组件的 Configuration
 */
@Configuration(proxyBeanMethods = false)
public class FmsWebConfiguration {

    /**
     * fms 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi fmsGroupedOpenApi() {
        return YudaoSwaggerAutoConfiguration.buildGroupedOpenApi("fms");
    }

}
