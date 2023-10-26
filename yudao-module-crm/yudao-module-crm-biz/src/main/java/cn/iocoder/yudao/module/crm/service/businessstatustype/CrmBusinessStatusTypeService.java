package cn.iocoder.yudao.module.crm.service.businessstatustype;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype.CrmBusinessStatusTypeDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 商机状态类型 Service 接口
 *
 * @author ljlleo
 */
public interface CrmBusinessStatusTypeService {

    /**
     * 创建商机状态类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBusinessStatusType(@Valid CrmBusinessStatusTypeCreateReqVO createReqVO);

    /**
     * 更新商机状态类型
     *
     * @param updateReqVO 更新信息
     */
    void updateBusinessStatusType(@Valid CrmBusinessStatusTypeUpdateReqVO updateReqVO);

    /**
     * 删除商机状态类型
     *
     * @param id 编号
     */
    void deleteBusinessStatusType(Long id);

    /**
     * 获得商机状态类型
     *
     * @param id 编号
     * @return 商机状态类型
     */
    CrmBusinessStatusTypeDO getBusinessStatusType(Long id);

    /**
     * 获得商机状态类型列表
     *
     * @param ids 编号
     * @return 商机状态类型列表
     */
    List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList(Collection<Long> ids);

    /**
     * 获得商机状态类型分页
     *
     * @param pageReqVO 分页查询
     * @return 商机状态类型分页
     */
    PageResult<CrmBusinessStatusTypeDO> getBusinessStatusTypePage(CrmBusinessStatusTypePageReqVO pageReqVO);

    /**
     * 获得商机状态类型列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 商机状态类型列表
     */
    List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList(CrmBusinessStatusTypeExportReqVO exportReqVO);

    /**
     * 获得商机状态类型列表
     *
     * @param status 状态
     * @return 商机状态类型列表
     */
    List<CrmBusinessStatusTypeDO> getBusinessStatusTypeListByStatus(Integer status);

}
