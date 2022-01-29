package cn.iocoder.yudao.module.system.service.tenant;

import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 租户 Service 接口
 *
 * @author 芋道源码
 */
public interface TenantService {

    /**
     * 创建租户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTenant(@Valid TenantCreateReqVO createReqVO);

    /**
     * 更新租户
     *
     * @param updateReqVO 更新信息
     */
    void updateTenant(@Valid TenantUpdateReqVO updateReqVO);

    /**
     * 删除租户
     *
     * @param id 编号
     */
    void deleteTenant(Long id);

    /**
     * 获得租户
     *
     * @param id 编号
     * @return 租户
     */
    SysTenantDO getTenant(Long id);

    /**
     * 获得租户列表
     *
     * @param ids 编号
     * @return 租户列表
     */
    List<SysTenantDO> getTenantList(Collection<Long> ids);

    /**
     * 获得租户分页
     *
     * @param pageReqVO 分页查询
     * @return 租户分页
     */
    PageResult<SysTenantDO> getTenantPage(TenantPageReqVO pageReqVO);

    /**
     * 获得租户列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 租户列表
     */
    List<SysTenantDO> getTenantList(TenantExportReqVO exportReqVO);

    /**
     * 获得名字对应的租户
     *
     * @param name 组户名
     * @return 租户
     */
    SysTenantDO getTenantByName(String name);

}
