package cn.iocoder.dashboard.framework.apollox.spring.util;

import cn.hutool.core.lang.Singleton;

/**
 * Spring 注入器
 */
public class SpringInjector {

    public static <T> T getInstance(Class<T> clazz) {
        return Singleton.get(clazz);
    }

}
