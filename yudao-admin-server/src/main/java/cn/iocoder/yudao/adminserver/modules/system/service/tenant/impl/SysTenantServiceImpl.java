package cn.iocoder.yudao.adminserver.modules.system.service.tenant.impl;

import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.tenant.SysTenantConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.tenant.SysTenantMapper;
import cn.iocoder.yudao.adminserver.modules.system.service.tenant.SysTenantService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.TENANT_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 租户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SysTenantServiceImpl implements SysTenantService {

    @Resource
    private SysTenantMapper tenantMapper;

    @Override
    public Long createTenant(SysTenantCreateReqVO createReqVO) {
        // 插入
        SysTenantDO tenant = SysTenantConvert.INSTANCE.convert(createReqVO);
        tenantMapper.insert(tenant);
        // 返回
        return tenant.getId();
    }

    @Override
    public void updateTenant(SysTenantUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateTenantExists(updateReqVO.getId());
        // 更新
        SysTenantDO updateObj = SysTenantConvert.INSTANCE.convert(updateReqVO);
        tenantMapper.updateById(updateObj);
    }

    @Override
    public void deleteTenant(Long id) {
        // 校验存在
        this.validateTenantExists(id);
        // 删除
        tenantMapper.deleteById(id);
    }

    private void validateTenantExists(Long id) {
        if (tenantMapper.selectById(id) == null) {
            throw exception(TENANT_NOT_EXISTS);
        }
    }

    @Override
    public SysTenantDO getTenant(Long id) {
        return tenantMapper.selectById(id);
    }

    @Override
    public List<SysTenantDO> getTenantList(Collection<Long> ids) {
        return tenantMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SysTenantDO> getTenantPage(SysTenantPageReqVO pageReqVO) {
        return tenantMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SysTenantDO> getTenantList(SysTenantExportReqVO exportReqVO) {
        return tenantMapper.selectList(exportReqVO);
    }

    @Override
    public SysTenantDO getTenantByName(String name) {
        return tenantMapper.selectByName(name);
    }

}
