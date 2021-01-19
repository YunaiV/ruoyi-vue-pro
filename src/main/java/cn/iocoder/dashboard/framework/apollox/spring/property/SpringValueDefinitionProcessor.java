package cn.iocoder.dashboard.framework.apollox.spring.property;

import cn.hutool.core.lang.Singleton;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * To process xml config placeholders, e.g.
 *
 * <pre>
 *  &lt;bean class=&quot;com.ctrip.framework.apollo.demo.spring.xmlConfigDemo.bean.XmlBean&quot;&gt;
 *    &lt;property name=&quot;timeout&quot; value=&quot;${timeout:200}&quot;/&gt;
 *    &lt;property name=&quot;batch&quot; value=&quot;${batch:100}&quot;/&gt;
 *  &lt;/bean&gt;
 * </pre>
 */
public class SpringValueDefinitionProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * SpringValueDefinition 集合
     * <p>
     * KEY：beanName
     * VALUE：SpringValueDefinition 集合
     */
    private static final Multimap<String, SpringValueDefinition> beanName2SpringValueDefinitions = LinkedListMultimap.create();
    /**
     * 是否初始化的标识
     */
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private final PlaceholderHelper placeholderHelper = Singleton.get(PlaceholderHelper.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        processPropertyValues(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    public static Multimap<String, SpringValueDefinition> getBeanName2SpringValueDefinitions() {
        return beanName2SpringValueDefinitions;
    }

    private void processPropertyValues(BeanDefinitionRegistry beanRegistry) {
        // 若已经初始化，直接返回
        if (!initialized.compareAndSet(false, true)) {
            // already initialized
            return;
        }
        // 循环 BeanDefinition 集合
        String[] beanNames = beanRegistry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanRegistry.getBeanDefinition(beanName);
            // 循环 BeanDefinition 的 PropertyValue 数组
            MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
            List<PropertyValue> propertyValues = mutablePropertyValues.getPropertyValueList();
            for (PropertyValue propertyValue : propertyValues) {
                // 获得 `value` 属性。
                Object value = propertyValue.getValue();
                // 忽略非 Spring PlaceHolder 的 `value` 属性。
                if (!(value instanceof TypedStringValue)) {
                    continue;
                }
                // 获得 `placeholder` 属性。
                String placeholder = ((TypedStringValue) value).getValue();
                // 提取 `keys` 属性们。
                Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);
                if (keys.isEmpty()) {
                    continue;
                }
                // 循环 `keys` ，创建对应的 SpringValueDefinition 对象，并添加到 `beanName2SpringValueDefinitions` 中。
                for (String key : keys) {
                    beanName2SpringValueDefinitions.put(beanName,
                            new SpringValueDefinition(key, placeholder, propertyValue.getName()));
                }
            }
        }
    }

}
