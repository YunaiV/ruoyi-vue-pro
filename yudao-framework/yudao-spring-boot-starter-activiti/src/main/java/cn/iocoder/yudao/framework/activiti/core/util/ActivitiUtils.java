package cn.iocoder.yudao.framework.activiti.core.util;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.activiti.engine.impl.identity.Authentication;

/**
 * Activiti 工具类
 *
 * @author 芋道源码
 */
public class ActivitiUtils {

    static {
        setAuthenticationThreadLocal();
    }

    /**
     * 反射修改 Authentication 的 authenticatedUserIdThreadLocal 静态变量，使用 TTL 线程变量
     * 目的：保证 @Async 等异步执行时，变量丢失的问题
     */
    private static void setAuthenticationThreadLocal() {
        ReflectUtil.setFieldValue(Authentication.class, "authenticatedUserIdThreadLocal",
                new TransmittableThreadLocal<String>());
    }

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }

}
