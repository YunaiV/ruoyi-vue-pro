package cn.iocoder.yudao.framework.desensitize.core.base;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;
import cn.iocoder.yudao.framework.desensitize.core.regex.handler.DefaultRegexDesensitizationHandler;
import cn.iocoder.yudao.framework.desensitize.core.slider.handler.DefaultDesensitizationHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱敏处理器 Holder
 */
public class DesensitizationHandlerHolder {
    /**
     * handler 缓存，默认初始化内置的处理器
     */
    private static final Map<Class<? extends DesensitizationHandler>, DesensitizationHandler> HANDLER_MAP = new ConcurrentHashMap<>() {{
        put(DefaultRegexDesensitizationHandler.class, new DefaultRegexDesensitizationHandler());
        put(DefaultDesensitizationHandler.class, new DefaultDesensitizationHandler());
    }};

    public static DesensitizationHandler getDesensitizationHandler(Class<? extends DesensitizationHandler> clazz) {
        DesensitizationHandler handler = HANDLER_MAP.get(clazz);
        if (handler != null) {
            return handler;
        }
        synchronized (DesensitizationHandlerHolder.class) {
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
