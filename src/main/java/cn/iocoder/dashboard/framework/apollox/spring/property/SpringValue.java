package cn.iocoder.dashboard.framework.apollox.spring.property;

import org.springframework.core.MethodParameter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Spring @Value method info
 *
 * @author github.com/zhegexiaohuozi  seimimaster@gmail.com
 * @since 2018/2/6.
 */
public class SpringValue {

    /**
     * Bean 对象
     */
    private Object bean;
    /**
     * Bean 名字
     */
    private String beanName;
    /**
     * Spring 方法参数封装
     */
    private MethodParameter methodParameter;
    /**
     * Field
     */
    private Field field;
    /**
     * KEY
     *
     * 即在 Config 中的属性 KEY 。
     */
    private String key;
    /**
     * 占位符
     */
    private String placeholder;
    /**
     * 值类型
     */
    private Class<?> targetType;
    /**
     * 是否 JSON
     */
    private boolean isJson;
    /**
     * 泛型。当是 JSON 类型时，使用
     */
    private Type genericType;

    // Field
    public SpringValue(String key, String placeholder, Object bean, String beanName, Field field, boolean isJson) {
        this.bean = bean;
        this.beanName = beanName;
        // Field
        this.field = field;
        this.key = key;
        this.placeholder = placeholder;
        // Field 差异
        this.targetType = field.getType();
        this.isJson = isJson;
        if (isJson) {
            this.genericType = field.getGenericType();
        }
    }

    // Method
    public SpringValue(String key, String placeholder, Object bean, String beanName, Method method, boolean isJson) {
        this.bean = bean;
        this.beanName = beanName;
        // Method
        this.methodParameter = new MethodParameter(method, 0);
        this.key = key;
        this.placeholder = placeholder;
        // Method 差异
        Class<?>[] paramTps = method.getParameterTypes();
        this.targetType = paramTps[0];
        this.isJson = isJson;
        if (isJson) {
            this.genericType = method.getGenericParameterTypes()[0];
        }
    }

    public void update(Object newVal) throws IllegalAccessException, InvocationTargetException {
        // Field
        if (isField()) {
            injectField(newVal);
        // Method
        } else {
            injectMethod(newVal);
        }
    }

    private void injectField(Object newVal) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(bean, newVal);
        field.setAccessible(accessible);
    }

    private void injectMethod(Object newVal) throws InvocationTargetException, IllegalAccessException {
        methodParameter.getMethod().invoke(bean, newVal);
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public MethodParameter getMethodParameter() {
        return methodParameter;
    }

    public boolean isField() {
        return this.field != null;
    }

    public Field getField() {
        return field;
    }

    public Type getGenericType() {
        return genericType;
    }

    public boolean isJson() {
        return isJson;
    }

    @Override
    public String toString() {
        if (isField()) {
            return String.format("key: %s, beanName: %s, field: %s.%s", key, beanName, bean.getClass().getName(), field.getName());
        }
        return String.format("key: %s, beanName: %s, method: %s.%s", key, beanName, bean.getClass().getName(),
                methodParameter.getMethod().getName());
    }

}
