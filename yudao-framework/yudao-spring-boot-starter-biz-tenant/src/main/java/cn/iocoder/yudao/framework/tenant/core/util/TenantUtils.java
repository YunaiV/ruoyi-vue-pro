package cn.iocoder.yudao.framework.tenant.core.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.google.common.base.Suppliers;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * 多租户 Util
 *
 * @author 芋道源码
 */
public class TenantUtils {

    /**
     * 平台共享数据的租户编号；不是未设置租户上下文时的 null
     */
    public static final Long GLOBAL_TENANT_ID = 0L;

    /**
     * 多租户开关，首次使用时读取并缓存
     */
    private static final Supplier<Boolean> TENANT_ENABLE = Suppliers.memoize(
            () -> !Boolean.FALSE.equals(SpringUtil.getProperty("yudao.tenant.enable", Boolean.class, true)));

    /**
     * 判断是否开启多租户
     *
     * @return 是否开启多租户
     */
    public static boolean isTenantEnable() {
        return TENANT_ENABLE.get();
    }

    /**
     * 使用指定租户，执行对应的逻辑
     *
     * 注意，如果当前是忽略租户的情况下，会被强制设置成不忽略租户
     * 当然，执行完成后，还是会恢复回去
     *
     * @param tenantId 租户编号
     * @param runnable 逻辑
     */
    public static void execute(Long tenantId, Runnable runnable) {
        Long oldTenantId = TenantContextHolder.getTenantId();
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setTenantId(tenantId);
            TenantContextHolder.setIgnore(false);
            // 执行逻辑
            runnable.run();
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 使用指定租户，执行对应的逻辑
     *
     * 注意，如果当前是忽略租户的情况下，会被强制设置成不忽略租户
     * 当然，执行完成后，还是会恢复回去
     *
     * @param tenantId 租户编号
     * @param callable 逻辑
     * @return 结果
     */
    public static <V> V execute(Long tenantId, Callable<V> callable) {
        Long oldTenantId = TenantContextHolder.getTenantId();
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setTenantId(tenantId);
            TenantContextHolder.setIgnore(false);
            // 执行逻辑
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            TenantContextHolder.setTenantId(oldTenantId);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 忽略租户，执行对应的逻辑
     *
     * @param runnable 逻辑
     */
    public static void executeIgnore(Runnable runnable) {
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setIgnore(true);
            // 执行逻辑
            runnable.run();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 忽略租户，执行对应的逻辑
     *
     * @param callable 逻辑
     * @return 结果
     */
    public static <V> V executeIgnore(Callable<V> callable) {
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setIgnore(true);
            // 执行逻辑
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 将多租户编号，添加到 header 中
     *
     * @param headers HTTP 请求 headers
     * @param tenantId 租户编号
     */
    public static void addTenantHeader(Map<String, String> headers, Long tenantId) {
        if (tenantId != null) {
            headers.put(HEADER_TENANT_ID, tenantId.toString());
        }
    }

}
