package cn.iocoder.yudao.framework.swagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.providers.JavadocProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * springdoc 自动配置类
 * 使用 knife4j.enable=false 禁用 Swagger
 *
 * @author 芋道源码
 */
@AutoConfiguration
@ConditionalOnClass({OpenAPI.class})
@EnableConfigurationProperties(SwaggerProperties.class)
public class YudaoSwaggerAutoConfiguration {

    @Bean
    public OpenAPI createApi(SwaggerProperties properties) {
        Map<String, SecurityScheme> maps = securityScheme();
        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo(properties))
                .components(new Components().securitySchemes(maps))
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
        maps.keySet().forEach(key -> openAPI.addSecurityItem(new SecurityRequirement().addList(key)));
        return openAPI;
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

    /**
     * API 摘要信息
     */
    private Info buildInfo(SwaggerProperties properties) {
        return new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion())
                .contact(new Contact().name(properties.getAuthor()).url(properties.getUrl()).email(properties.getEmail()))
                .license(new License().name(properties.getLicense()).url(properties.getLicenseUrl()));
    }

    /**
     * 安全模式，这里配置通过请求头 Authorization 传递 token 参数
     */
    private Map<String, SecurityScheme> securityScheme() {
        Map<String, SecurityScheme> map = new HashMap<>();
        SecurityScheme securityScheme = new SecurityScheme()
                //类型
                .type(SecurityScheme.Type.APIKEY)
                //请求头的name
                .name(HttpHeaders.AUTHORIZATION)
                //token所在未知
                .in(SecurityScheme.In.HEADER);
        map.put(HttpHeaders.AUTHORIZATION, securityScheme);
        return map;
    }

    /**
     * globalHeaderParameter
     * @return 多租户参数
     */
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

