package cn.iocoder.yudao.framework.activiti.core.util;

import org.activiti.engine.impl.identity.Authentication;

/**
 * Activiti 工具类
 *
 * @author 芋道源码
 */
public class ActivitiUtils {

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }

}
