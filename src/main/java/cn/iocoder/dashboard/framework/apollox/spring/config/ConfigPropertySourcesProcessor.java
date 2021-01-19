package cn.iocoder.dashboard.framework.apollox.spring.config;

import cn.iocoder.dashboard.framework.apollox.spring.annotation.ApolloAnnotationProcessor;
import cn.iocoder.dashboard.framework.apollox.spring.annotation.SpringValueProcessor;
import cn.iocoder.dashboard.framework.apollox.spring.property.PropertySourcesProcessor;
import cn.iocoder.dashboard.framework.apollox.spring.property.SpringValueDefinitionProcessor;
import cn.iocoder.dashboard.framework.apollox.spring.util.BeanRegistrationUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Apollo Property Sources processor for Spring XML Based Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigPropertySourcesProcessor extends PropertySourcesProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 注册 PropertySourcesPlaceholderConfigurer 到 BeanDefinitionRegistry 中，替换 PlaceHolder 为对应的属性值，参考文章 https://leokongwq.github.io/2016/12/28/spring-PropertyPlaceholderConfigurer.html
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, PropertySourcesPlaceholderConfigurer.class.getName(), PropertySourcesPlaceholderConfigurer.class);
        // 注册 ApolloAnnotationProcessor 到 BeanDefinitionRegistry 中，因为 XML 配置的 Bean 对象，也可能存在 @ApolloConfig 和 @ApolloConfigChangeListener 注解。
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloAnnotationProcessor.class.getName(), ApolloAnnotationProcessor.class);
        // 注册 SpringValueProcessor 到 BeanDefinitionRegistry 中，用于 PlaceHolder 自动更新机制
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, SpringValueProcessor.class.getName(), SpringValueProcessor.class);
        // 注册 ApolloJsonValueProcessor 到 BeanDefinitionRegistry 中，因为 XML 配置的 Bean 对象，也可能存在 @ApolloJsonValue 注解。
//        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, ApolloJsonValueProcessor.class.getName(), ApolloJsonValueProcessor.class); TODO 芋艿：暂时不需要迁移

        // 处理 XML 配置的 Spring PlaceHolder
        processSpringValueDefinition(registry);
    }

    /**
     * For Spring 3.x versions, the BeanDefinitionRegistryPostProcessor would not be
     * instantiated if it is added in postProcessBeanDefinitionRegistry phase, so we have to manually
     * call the postProcessBeanDefinitionRegistry method of SpringValueDefinitionProcessor here...
     */
    private void processSpringValueDefinition(BeanDefinitionRegistry registry) {
        // 创建 SpringValueDefinitionProcessor 对象
        SpringValueDefinitionProcessor springValueDefinitionProcessor = new SpringValueDefinitionProcessor();
        // 处理 XML 配置的 Spring PlaceHolder
        springValueDefinitionProcessor.postProcessBeanDefinitionRegistry(registry);
    }

}
