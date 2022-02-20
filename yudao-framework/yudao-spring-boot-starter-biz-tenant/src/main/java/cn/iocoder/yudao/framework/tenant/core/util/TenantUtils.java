package cn.iocoder.yudao.framework.tenant.core.util;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;

/**
 * 多租户 Util
 *
 * @author 芋道源码
 */
public class TenantUtils {

    /**
     * 使用指定租户，执行对应的逻辑
     *
     * @param tenantId 租户编号
     * @param runnable 逻辑
     */
    public static void execute(Long tenantId, Runnable runnable) {
        Long oldTenantId = TenantContextHolder.getTenantId();
        try {
            TenantContextHolder.setTenantId(tenantId);
            // 执行逻辑
            runnable.run();
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
        }
    }


}
