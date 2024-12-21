package cn.iocoder.yudao.module.iot.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务注册表 - 插架模块使用，无法使用 Spring 注入
 */
public class ServiceRegistry {
    private static final Map<Class<?>, Object> services = new HashMap<>();

    /**
     * 注册服务
     *
     * @param serviceClass 服务类
     * @param serviceImpl  服务实现
     * @param <T>          服务类
     */
    public static <T> void registerService(Class<T> serviceClass, T serviceImpl) {
        services.put(serviceClass, serviceImpl);
    }

    /**
     * 获得服务
     *
     * @param serviceClass 服务类
     * @param <T>          服务类
     * @return 服务实现
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }
}