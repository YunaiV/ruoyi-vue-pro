package cn.iocoder.yudao.framework.tenant.core.service;

import cn.iocoder.yudao.module.system.api.tenant.TenantApi;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Tenant 框架 Service 实现类
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class TenantFrameworkServiceImpl implements TenantFrameworkService {

    private final TenantApi tenantApi;

    @Override
    public List<Long> getTenantIds() {
        return tenantApi.getTenantIds();
    }

    @Override
    public void validTenant(Long id) {
        tenantApi.validTenant(id);
    }

}
