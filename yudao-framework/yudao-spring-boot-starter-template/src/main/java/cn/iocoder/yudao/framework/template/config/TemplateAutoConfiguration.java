package cn.iocoder.yudao.framework.template.config;

import cn.iocoder.yudao.framework.template.core.TemplateManager;
import cn.iocoder.yudao.framework.template.core.TemplateService;
import cn.iocoder.yudao.framework.template.core.impl.TemplateServiceRedisImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;

@AutoConfiguration
@EnableConfigurationProperties(TemplateRegistar.class)
@Slf4j
public class TemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TemplateService templateService(ResourcePatternResolver resolver, RedisTemplate<String, byte[]> byteArrayRedisTemplate) {
        TemplateServiceRedisImpl service = new TemplateServiceRedisImpl();
        service.setResourcePatternResolver(resolver);
        //        service.setSelf(service); // 注入代理对象
        service.setRedisTemplate(byteArrayRedisTemplate);
        return service;
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateConfigureFactory templateConfigureFactory() {
        return new TemplateConfigureFactory(); // 如果需要注入策略 registrar，可在此处扩展
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateManager templateManager(TemplateService templateService, ResourcePatternResolver resolver, TemplateConfigureFactory factory) {
        TemplateManager manager = new TemplateManager();
        manager.setTemplateService(templateService);
        manager.setConfigureFactory(factory);
        return manager;
    }
}
