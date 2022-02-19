package cn.iocoder.yudao.framework.tenant.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 多租户上下文 Holder
 *
 * @author 芋道源码
 */
public class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 租户编号 - 空
     */
    private static final Long TENANT_ID_NULL = 0L;

    /**
     * 获得租户编号。
     *
     * @return 租户编号
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 获得租户编号。如果不存在，则抛出 NullPointerException 异常
     *
     * @return 租户编号
     */
    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new NullPointerException("TenantContextHolder 不存在租户编号"); // TODO 芋艿：增加文档链接
        }
        return tenantId;
    }

    /**
     * 在一些前端场景下，可能无法请求带上租户。例如说，<img /> 方式获取图片等
     * 此时，暂时的解决方案，是在该接口的 Controller 方法上，调用该方法
     * TODO 芋艿：思考有没更合适的方案，目标是去掉该方法
     */
    public static void setNullTenantId() {
        TENANT_ID.set(TENANT_ID_NULL);
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void clear() {
        TENANT_ID.remove();
    }

}
