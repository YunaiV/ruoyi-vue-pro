package cn.iocoder.yudao.framework.tenant.core.context;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.DocumentEnum;
import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 多租户上下文 Holder
 *
 * @author 芋道源码
 */
public class TenantContextHolder {

    /**
     * 当前租户编号
     */
    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 是否忽略租户
     */
    private static final ThreadLocal<Boolean> IGNORE = new TransmittableThreadLocal<>();

    /**
     * 获得租户编号
     *
     * @return 租户编号
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 获得租户编号 String
     *
     * @return 租户编号
     */
    public static String getTenantIdStr() {
        Long tenantId = getTenantId();
        return StrUtil.toStringOrNull(tenantId);
    }

    /**
     * 获得租户编号。如果不存在，则抛出 NullPointerException 异常
     *
     * @return 租户编号
     */
    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new NullPointerException("TenantContextHolder 不存在租户编号！可参考文档："
                + DocumentEnum.TENANT.getUrl());
        }
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void setIgnore(Boolean ignore) {
        IGNORE.set(ignore);
    }

    /**
     * 当前是否忽略租户
     *
     * @return 是否忽略
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }

}
