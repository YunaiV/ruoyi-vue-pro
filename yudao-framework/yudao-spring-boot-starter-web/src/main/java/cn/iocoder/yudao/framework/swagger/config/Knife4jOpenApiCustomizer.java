package cn.iocoder.yudao.framework.swagger.config;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.github.xiaoymin.knife4j.core.conf.ExtensionsConstants;
import com.github.xiaoymin.knife4j.core.conf.GlobalConstants;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jSetting;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 增强扩展属性支持
 *
 * 参考 <a href="https://github.com/xiaoymin/knife4j/issues/913">Spring Boot 3.4 以上版本 /v3/api-docs 解决接口报错，依赖修复</a>
 *
 * @since 4.1.0
 * @author <a href="xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2022/12/11 22:40
 */
@Primary
@Configuration
@Slf4j
public class Knife4jOpenApiCustomizer extends com.github.xiaoymin.knife4j.spring.extension.Knife4jOpenApiCustomizer
        implements GlobalOpenApiCustomizer {

    final Knife4jProperties knife4jProperties;
    final SpringDocConfigProperties properties;

    public Knife4jOpenApiCustomizer(Knife4jProperties knife4jProperties, SpringDocConfigProperties properties) {
        super(knife4jProperties,properties);
        this.knife4jProperties = knife4jProperties;
        this.properties = properties;
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (knife4jProperties.isEnable()) {
            Knife4jSetting setting = knife4jProperties.getSetting();
            OpenApiExtensionResolver openApiExtensionResolver = new OpenApiExtensionResolver(setting, knife4jProperties.getDocuments());
            // 解析初始化
            openApiExtensionResolver.start();
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put(GlobalConstants.EXTENSION_OPEN_SETTING_NAME, setting);
            objectMap.put(GlobalConstants.EXTENSION_OPEN_MARKDOWN_NAME, openApiExtensionResolver.getMarkdownFiles());
            openApi.addExtension(GlobalConstants.EXTENSION_OPEN_API_NAME, objectMap);
            addOrderExtension(openApi);
        }
    }

    /**
     * 往 OpenAPI 内 tags 字段添加 x-order 属性
     *
     * @param openApi openApi
     */
    private void addOrderExtension(OpenAPI openApi) {
        if (CollectionUtils.isEmpty(properties.getGroupConfigs())) {
            return;
        }
        // 获取包扫描路径
        Set<String> packagesToScan =
                properties.getGroupConfigs().stream()
                        .map(SpringDocConfigProperties.GroupConfig::getPackagesToScan)
                        .filter(toScan -> !CollectionUtils.isEmpty(toScan))
                        .flatMap(List::stream)
                        .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(packagesToScan)) {
            return;
        }
        // 扫描包下被 ApiSupport 注解的 RestController Class
        Set<Class<?>> classes = packagesToScan.stream()
                .map(packageToScan -> scanPackageByAnnotation(packageToScan, RestController.class))
                .flatMap(Set::stream)
                .filter(clazz -> clazz.isAnnotationPresent(ApiSupport.class))
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(classes)) {
            // ApiSupport oder 值存入 tagSortMap<Tag.name,ApiSupport.order>
            Map<String, Integer> tagOrderMap = new HashMap<>();
            classes.forEach(clazz -> {
                Tag tag = getTag(clazz);
                if (Objects.nonNull(tag)) {
                    ApiSupport apiSupport = clazz.getAnnotation(ApiSupport.class);
                    tagOrderMap.putIfAbsent(tag.name(), apiSupport.order());
                }
            });
            // 往 openApi tags 字段添加 x-order 增强属性
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    if (tagOrderMap.containsKey(tag.getName())) {
                        tag.addExtension(ExtensionsConstants.EXTENSION_ORDER, tagOrderMap.get(tag.getName()));
                    }
                });
            }
        }
    }

    private Tag getTag(Class<?> clazz) {
        // 从类上获取
        Tag tag = clazz.getAnnotation(Tag.class);
        if (Objects.isNull(tag)) {
            // 从接口上获取
            Class<?>[] interfaces = clazz.getInterfaces();
            if (ArrayUtils.isNotEmpty(interfaces)) {
                for (Class<?> interfaceClazz : interfaces) {
                    Tag anno = interfaceClazz.getAnnotation(Tag.class);
                    if (Objects.nonNull(anno)) {
                        tag = anno;
                        break;
                    }
                }
            }
        }
        return tag;
    }

    private Set<Class<?>> scanPackageByAnnotation(String packageName, final Class<? extends Annotation> annotationClass) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        Set<Class<?>> classes = new HashSet<>();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageName)) {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException ignore) {
            }
        }
        return classes;
    }

}