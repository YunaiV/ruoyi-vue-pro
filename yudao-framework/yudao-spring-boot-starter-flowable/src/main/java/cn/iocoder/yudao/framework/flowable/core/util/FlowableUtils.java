package cn.iocoder.yudao.framework.flowable.core.util;

import org.flowable.common.engine.impl.identity.Authentication;

public class FlowableUtils {

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }
}
