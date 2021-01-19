package cn.iocoder.dashboard.framework.apollox.spring.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Objects;

/**
 * Bean Registration 工具类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class BeanRegistrationUtil {

    // 注册 `beanClass` 到 BeanDefinitionRegistry 中，当且仅当 `beanName` 和 `beanClass` 都不存在对应的 BeanDefinition 时
    public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass) {
        // 不存在 `beanName` 对应的 BeanDefinition
        if (registry.containsBeanDefinition(beanName)) {
            return false;
        }

        // 不存在 `beanClass` 对应的 BeanDefinition
        String[] candidates = registry.getBeanDefinitionNames();
        for (String candidate : candidates) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
            if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
                return false;
            }
        }

        // 注册 `beanClass` 到 BeanDefinitionRegistry 中
        BeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition();
        registry.registerBeanDefinition(beanName, annotationProcessor);
        return true;
    }

}
