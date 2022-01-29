package cn.iocoder.yudao.module.system.service.tenant;

import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.tenant.TenantConvert;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.SysTenantMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.TENANT_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 租户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TenantServiceImpl implements TenantService {

    @Resource
    private SysTenantMapper tenantMapper;

    @Override
    public Long createTenant(TenantCreateReqVO createReqVO) {
        // 插入
        SysTenantDO tenant = TenantConvert.INSTANCE.convert(createReqVO);
        tenantMapper.insert(tenant);
        // 返回
        return tenant.getId();
    }

    @Override
    public void updateTenant(TenantUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateTenantExists(updateReqVO.getId());
        // 更新
        SysTenantDO updateObj = TenantConvert.INSTANCE.convert(updateReqVO);
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
    public PageResult<SysTenantDO> getTenantPage(TenantPageReqVO pageReqVO) {
        return tenantMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SysTenantDO> getTenantList(TenantExportReqVO exportReqVO) {
        return tenantMapper.selectList(exportReqVO);
    }

    @Override
    public SysTenantDO getTenantByName(String name) {
        return tenantMapper.selectByName(name);
    }

}
