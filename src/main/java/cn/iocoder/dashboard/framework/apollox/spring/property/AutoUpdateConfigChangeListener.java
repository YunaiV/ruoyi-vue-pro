package cn.iocoder.dashboard.framework.apollox.spring.property;

import cn.hutool.core.lang.Singleton;
import cn.iocoder.dashboard.framework.apollox.ConfigChangeListener;
import cn.iocoder.dashboard.framework.apollox.enums.PropertyChangeType;
import cn.iocoder.dashboard.framework.apollox.model.ConfigChange;
import cn.iocoder.dashboard.framework.apollox.model.ConfigChangeEvent;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * 自动更新配置监听器
 *
 * Create by zhangzheng on 2018/3/6
 */
public class AutoUpdateConfigChangeListener implements ConfigChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(AutoUpdateConfigChangeListener.class);

    /**
     * {@link TypeConverter#convertIfNecessary(Object, Class, Field)} 是否带上 Field 参数，因为 Spring 3.2.0+ 才有该方法
     */
    private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;
    private final Environment environment;
    private final ConfigurableBeanFactory beanFactory;
    /**
     * TypeConverter 对象，参见 https://blog.csdn.net/rulerp2014/article/details/51100857
     */
    private final TypeConverter typeConverter;
    private final PlaceholderHelper placeholderHelper;
    private final SpringValueRegistry springValueRegistry;

    public AutoUpdateConfigChangeListener(Environment environment, ConfigurableListableBeanFactory beanFactory) {
        this.typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
        this.beanFactory = beanFactory;
        this.typeConverter = this.beanFactory.getTypeConverter();
        this.environment = environment;
        this.placeholderHelper = Singleton.get(PlaceholderHelper.class);
        this.springValueRegistry = Singleton.get(SpringValueRegistry.class);
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        // 获得更新的 KEY 集合
        Set<String> keys = changeEvent.changedKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        // 循环 KEY 集合，更新 StringValue
        for (String key : keys) {
            // 忽略，若不在 SpringValueRegistry 中
            // 1. check whether the changed key is relevant
            Collection<SpringValue> targetValues = springValueRegistry.get(key);
            if (targetValues == null || targetValues.isEmpty()) {
                continue;
            }
            // 校验是否需要更新
            // 2. check whether the value is really changed or not (since spring property sources have hierarchies)
            if (!shouldTriggerAutoUpdate(changeEvent, key)) {
                continue;
            }
            // 循环，更新 SpringValue
            // 3. update the value
            for (SpringValue val : targetValues) {
                updateSpringValue(val);
            }
        }
    }

    /**
     * Check whether we should trigger the auto update or not.
     * <br />
     * For added or modified keys, we should trigger auto update if the current value in Spring equals to the new value.
     * <br />
     * For deleted keys, we will trigger auto update anyway.
     */
    private boolean shouldTriggerAutoUpdate(ConfigChangeEvent changeEvent, String changedKey) {
        ConfigChange configChange = changeEvent.getChange(changedKey);
        // 若变更类型为删除，需要触发更新
        if (configChange.getChangeType() == PropertyChangeType.DELETED) {
            return true;
        }
        // 若变更类型为新增或修改，判断 environment 的值是否和最新值相等。
        // 【高能】！！！
        return Objects.equals(environment.getProperty(changedKey), configChange.getNewValue());
    }

    private void updateSpringValue(SpringValue springValue) {
        try {
            // 解析值
            Object value = resolvePropertyValue(springValue);
            // 更新 StringValue
            springValue.update(value);
            logger.info("Auto update apollo changed value successfully, new value: {}, {}", value, springValue);
        } catch (Throwable ex) {
            logger.error("Auto update apollo changed value failed, {}", springValue.toString(), ex);
        }
    }

    /**
     * Logic transplanted from DefaultListableBeanFactory
     *
     * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, String, Set, TypeConverter)
     */
    private Object resolvePropertyValue(SpringValue springValue) {
        // value will never be null, as @Value and @ApolloJsonValue will not allow that
        Object value = placeholderHelper.resolvePropertyValue(beanFactory, springValue.getBeanName(), springValue.getPlaceholder());
        // 如果值数据结构是 JSON 类型，则使用 Gson 解析成对应值的类型
        if (springValue.isJson()) {
            value = parseJsonValue((String) value, springValue.getGenericType());
        } else {
            // 如果类型为 Field
            if (springValue.isField()) {
                // org.springframework.beans.TypeConverter#convertIfNecessary(java.lang.Object, java.lang.Class, java.lang.reflect.Field) is available from Spring 3.2.0+
                if (typeConverterHasConvertIfNecessaryWithFieldParameter) {
                    value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(), springValue.getField());
                } else {
                    value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType());
                }
            // 如果类型为 Method
            } else {
                value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(), springValue.getMethodParameter());
            }
        }

        return value;
    }

    private Object parseJsonValue(String json, Type targetType) {
        try {
            return JSON.parseObject(json, targetType);
        } catch (Throwable ex) {
            logger.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
            throw ex;
        }
    }

    private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
        try {
            TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
        } catch (Throwable ex) {
            return false;
        }
        return true;
    }

}
