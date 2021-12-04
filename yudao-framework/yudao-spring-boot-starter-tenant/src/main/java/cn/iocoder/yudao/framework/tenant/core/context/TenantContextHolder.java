package cn.iocoder.yudao.framework.tenant.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 多租户上下文 Holder
 *
 * @author 芋道源码
 */
public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void clear() {
        TENANT_ID.remove();
    }

}
