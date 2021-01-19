package cn.iocoder.dashboard.framework.apollox.spring.annotation;

import cn.hutool.core.lang.Singleton;
import cn.iocoder.dashboard.framework.apollox.Config;
import cn.iocoder.dashboard.framework.apollox.ConfigChangeListener;
import cn.iocoder.dashboard.framework.apollox.DefaultConfig;
import cn.iocoder.dashboard.framework.apollox.model.ConfigChangeEvent;
import com.google.common.base.Preconditions;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Apollo Annotation Processor for Spring Application
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloAnnotationProcessor extends ApolloProcessor {

    @Override
    protected void processField(Object bean, String beanName, Field field) {
        ApolloConfig annotation = AnnotationUtils.getAnnotation(field, ApolloConfig.class);
        if (annotation == null) {
            return;
        }

        Preconditions.checkArgument(Config.class.isAssignableFrom(field.getType()), "Invalid type: %s for field: %s, should be Config", field.getType(), field);

        // 创建 Config 对象
        Config config = Singleton.get(DefaultConfig.class);

        // 设置 Config 对象，到对应的 Field
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, config);
    }

    @Override
    protected void processMethod(final Object bean, String beanName, final Method method) {
        ApolloConfigChangeListener annotation = AnnotationUtils.findAnnotation(method, ApolloConfigChangeListener.class);
        if (annotation == null) {
            return;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Preconditions.checkArgument(parameterTypes.length == 1, "Invalid number of parameters: %s for method: %s, should be 1", parameterTypes.length, method);
        Preconditions.checkArgument(ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0]), "Invalid parameter type: %s for method: %s, should be ConfigChangeEvent", parameterTypes[0], method);

        // 创建 ConfigChangeListener 监听器。该监听器会调用被注解的方法。
        ReflectionUtils.makeAccessible(method);
        ConfigChangeListener configChangeListener = changeEvent -> {
            // 反射调用
            ReflectionUtils.invokeMethod(method, bean, changeEvent);
        };

        // 向指定 Namespace 的 Config 对象们，注册该监听器
        Config config = Singleton.get(DefaultConfig.class);
        config.addChangeListener(configChangeListener);
    }

}
