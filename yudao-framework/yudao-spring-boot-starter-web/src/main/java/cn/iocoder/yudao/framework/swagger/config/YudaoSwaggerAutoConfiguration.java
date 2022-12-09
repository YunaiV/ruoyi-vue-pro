package cn.iocoder.yudao.framework.swagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/**
 * Swagger3 自动配置类
 *
 * @author 芋道源码
 */
@AutoConfiguration
@ConditionalOnClass({OpenAPI.class})
// 允许使用 swagger.enable=false 禁用 Swagger
@ConditionalOnProperty(prefix = "yudao.swagger", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(SwaggerProperties.class)
public class YudaoSwaggerAutoConfiguration {

    @Bean
    public OpenAPI createRestApi(SwaggerProperties properties) {
        return new OpenAPI()
                .info(new Info().title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .license(new License().name("MIT").url("https://gitee.com/zhijiantianya/ruoyi-vue-pro/blob/master/LICENSE")))
                .externalDocs(new ExternalDocumentation()
                        .description(properties.getDescription())
                        .url("https://gitee.com/zhijiantianya/ruoyi-vue-pro"));

    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("app")
                .pathsToMatch("/app/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/admin/**")
                .build();
    }


}
