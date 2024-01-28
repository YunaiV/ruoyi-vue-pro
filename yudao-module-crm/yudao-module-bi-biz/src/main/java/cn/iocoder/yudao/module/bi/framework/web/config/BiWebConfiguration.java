package cn.iocoder.yudao.module.bi.framework.web.config;

import cn.iocoder.yudao.framework.swagger.config.YudaoSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * bi 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class BiWebConfiguration {

    /**
     * bi 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi biGroupedOpenApi() {
        return YudaoSwaggerAutoConfiguration.buildGroupedOpenApi("bi");
    }

}
