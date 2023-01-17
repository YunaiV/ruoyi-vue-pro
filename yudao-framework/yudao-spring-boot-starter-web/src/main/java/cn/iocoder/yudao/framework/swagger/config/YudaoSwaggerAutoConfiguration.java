package cn.iocoder.yudao.framework.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.providers.JavadocProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

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
        //信息
        Info info = new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion())
                .contact(new Contact().name(properties.getAuthor()).url(properties.getUrl()).email(properties.getEmail()))
                .license(new License().name(properties.getLicense()).url(properties.getLicenseUrl()));
        return new OpenAPI()
                .info(info)
                .schemaRequirement(HttpHeaders.AUTHORIZATION, securityScheme())
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
    }

    private SecurityScheme securityScheme() {
        SecurityScheme securityScheme = new SecurityScheme();
        //类型
        securityScheme.setType(SecurityScheme.Type.APIKEY);
        //请求头的name
        securityScheme.setName(HttpHeaders.AUTHORIZATION);
        //token所在未知
        securityScheme.setIn(SecurityScheme.In.HEADER);
        return securityScheme;
    }

    /**
     * 自定义 openapi 处理器
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openAPI,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties,
                                         PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomisers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomisers,
                                         Optional<JavadocProvider> javadocProvider) {
        return new OpenAPIService(openAPI, securityParser, springDocConfigProperties, propertyResolverUtils, openApiBuilderCustomisers, serverBaseUrlCustomisers, javadocProvider);
    }

    @Bean
    public GroupedOpenApi appApi() {
        return GroupedOpenApi.builder()
                .group("app")
                .addOperationCustomizer((operation, handlerMethod) ->
                        operation.addParametersItem(globalHeaderParameter())
                )
                .pathsToMatch("/app-api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .addOperationCustomizer((operation, handlerMethod) ->
                        operation.addParametersItem(globalHeaderParameter())
                )
                .pathsToMatch("/admin-api/**")
                .build();
    }

    private static Parameter globalHeaderParameter() {
        return new Parameter()
                .name(HEADER_TENANT_ID)
                .description("租户编号")
                .in(String.valueOf(SecurityScheme.In.HEADER))
                .schema(new IntegerSchema()
                        ._default(1L)
                        .name(HEADER_TENANT_ID)
                        .description("租户编号")
                );
    }

}

