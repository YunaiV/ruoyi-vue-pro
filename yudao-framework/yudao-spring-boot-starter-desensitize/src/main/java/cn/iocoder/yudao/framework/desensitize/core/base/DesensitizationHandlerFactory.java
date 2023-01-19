package cn.iocoder.yudao.framework.desensitize.core.base;

import cn.hutool.Hutool;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱敏处理器 Holder
 *
 * @author gaibu
 */
public class DesensitizationHandlerFactory {

    /**
     * handler 缓存，默认初始化内置的处理器
     */
    private static final Map<Class<? extends DesensitizationHandler>, DesensitizationHandler> HANDLER_MAP = new ConcurrentHashMap<Class<? extends DesensitizationHandler>, DesensitizationHandler>();

    // TODO @唐：可以考虑，使用 hutool 提供的 Singleton.get()
    public static DesensitizationHandler getDesensitizationHandler(Class<? extends DesensitizationHandler> clazz) {
        DesensitizationHandler handler = HANDLER_MAP.get(clazz);
        if (handler != null) {
            return handler;
        }
        // 不存在，则进行创建
        synchronized (DesensitizationHandlerFactory.class) {
            handler = HANDLER_MAP.get(clazz);
            // 双重校验锁
            if (handler != null) {
                return handler;
            }
            handler = ReflectUtil.newInstanceIfPossible(clazz);
            HANDLER_MAP.put(clazz, handler);
        }
        return handler;
    }

}
