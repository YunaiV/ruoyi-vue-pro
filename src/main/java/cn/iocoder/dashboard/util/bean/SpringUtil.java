package cn.iocoder.dashboard.util.bean;

import org.springframework.beans.BeansException;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import org.springframework.context.ApplicationContext;

import org.springframework.context.ApplicationContextAware;

import org.springframework.stereotype.Component;


@Component
public class SpringUtil implements ApplicationContextAware {

    /**
     * Spring context
     */
    private static ApplicationContext applicationContext = null;

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setAppCtx(ApplicationContext webAppCtx) {
        if (webAppCtx != null) {
            applicationContext = webAppCtx;
        }
    }


    /**
     * 拿到ApplicationContext对象实例后就可以手动获取Bean的注入实例对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext() == null ? null : getApplicationContext().getBean(clazz);
    }


    public static <T> T getBean(String name, Class<T> clazz) throws ClassNotFoundException {
        return getApplicationContext() == null ? null : getApplicationContext() .getBean(name, clazz);
    }


    public static final Object getBean(String beanName) {
        return getApplicationContext() == null ? null : getApplicationContext() .getBean(beanName);
    }


    public static final Object getBean(String beanName, String className) throws ClassNotFoundException {
        Class<?> clz = Class.forName(className);
        return getApplicationContext() == null ? null : getApplicationContext() .getBean(beanName, clz.getClass());

    }


    public static boolean containsBean(String name) {
        return getApplicationContext() == null ? null : getApplicationContext() .containsBean(name);
    }


    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return getApplicationContext() == null ? null : getApplicationContext() .isSingleton(name);
    }


    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return getApplicationContext() == null ? null : getApplicationContext() .getType(name);
    }


    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return getApplicationContext() == null ? null : getApplicationContext() .getAliases(name);
    }

}