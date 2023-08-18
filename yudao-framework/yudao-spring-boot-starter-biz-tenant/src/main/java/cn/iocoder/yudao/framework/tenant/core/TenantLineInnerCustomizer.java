package cn.iocoder.yudao.framework.tenant.core;

import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;

/**
 * @author lemoncc
 */
@FunctionalInterface
public interface TenantLineInnerCustomizer {

    /** 用于自定义配置 {@link TenantDatabaseInterceptor} */
    void customize(TenantDatabaseInterceptor interceptor);
}