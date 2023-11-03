package cn.iocoder.yudao.module.crm.service.businessstatus;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatus.CrmBusinessStatusDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 商机状态 Service 接口
 *
 * @author ljlleo
 */
public interface CrmBusinessStatusService {

    /**
     * 创建商机状态
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBusinessStatus(@Valid CrmBusinessStatusCreateReqVO createReqVO);

    /**
     * 更新商机状态
     *
     * @param updateReqVO 更新信息
     */
    void updateBusinessStatus(@Valid CrmBusinessStatusUpdateReqVO updateReqVO);

    /**
     * 删除商机状态
     *
     * @param id 编号
     */
    void deleteBusinessStatus(Long id);

    /**
     * 获得商机状态
     *
     * @param id 编号
     * @return 商机状态
     */
    CrmBusinessStatusDO getBusinessStatus(Long id);

    /**
     * 获得商机状态列表
     *
     * @param ids 编号
     * @return 商机状态列表
     */
    List<CrmBusinessStatusDO> getBusinessStatusList(Collection<Long> ids);

    /**
     * 获得商机状态分页
     *
     * @param pageReqVO 分页查询
     * @return 商机状态分页
     */
    PageResult<CrmBusinessStatusDO> getBusinessStatusPage(CrmBusinessStatusPageReqVO pageReqVO);

    /**
     * 获得商机状态列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 商机状态列表
     */
    List<CrmBusinessStatusDO> getBusinessStatusList(CrmBusinessStatusExportReqVO exportReqVO);

    /**
     * 根据类型 ID 获得商机状态列表
     *
     * @param typeId 商机状态类型 ID
     * @return 商机状态列表
     */
    List<CrmBusinessStatusDO> getBusinessStatusListByTypeId(Integer typeId);

    /**
     * 获得商机状态列表
     *
     * @return 商机状态列表
     */
    List<CrmBusinessStatusDO> getBusinessStatusList();

}
