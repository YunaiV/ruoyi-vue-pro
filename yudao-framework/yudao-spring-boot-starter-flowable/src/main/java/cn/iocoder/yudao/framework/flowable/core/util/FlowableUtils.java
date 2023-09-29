package cn.iocoder.yudao.framework.flowable.core.util;

import org.flowable.common.engine.impl.identity.Authentication;

/**
 * Flowable 相关的工具方法
 *
 * @author 芋道源码
 */
public class FlowableUtils {

    // ========== User 相关的工具方法 ==========

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }

    // ========== Execution 相关的工具方法 ==========

    public static String formatCollectionVariable(String activityId) {
        return activityId + "_assignees";
    }

    public static String formatCollectionElementVariable(String activityId) {
        return activityId + "_assignee";
    }

}
