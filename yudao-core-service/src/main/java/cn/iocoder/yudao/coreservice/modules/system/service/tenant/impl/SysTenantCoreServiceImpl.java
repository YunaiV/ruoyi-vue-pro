package cn.iocoder.yudao.coreservice.modules.system.service.tenant.impl;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.tenant.SysTenantCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.tenant.SysTenantCoreService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysTenantCoreServiceImpl implements SysTenantCoreService {

    @Resource
    private SysTenantCoreMapper tenantCoreMapper;

    @Override
    public List<Long> getTenantIds() {
        List<SysTenantDO> tenants = tenantCoreMapper.selectList();
        return CollectionUtils.convertList(tenants, SysTenantDO::getId);
    }

}
