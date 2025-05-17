package cn.iocoder.yudao.module.crm.service.receivable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * CRM 回款计划 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmReceivablePlanService {

    /**
     * 创建回款计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReceivablePlan(@Valid CrmReceivablePlanSaveReqVO createReqVO);

    /**
     * 更新回款计划
     *
     * @param updateReqVO 更新信息
     */
    void updateReceivablePlan(@Valid CrmReceivablePlanSaveReqVO updateReqVO);

    /**
     * 更新回款计划关联的回款编号
     *
     * @param id           编号
     * @param receivableId 回款编号
     */
    void updateReceivablePlanReceivableId(Long id, Long receivableId);

    /**
     * 删除回款计划
     *
     * @param id 编号
     */
    void deleteReceivablePlan(Long id);

    /**
     * 获得回款计划
     *
     * @param id 编号
     * @return 回款计划
     */
    CrmReceivablePlanDO getReceivablePlan(Long id);

    /**
     * 获得回款计划列表
     *
     * @param ids 编号
     * @return 回款计划列表
     */
    List<CrmReceivablePlanDO> getReceivablePlanList(Collection<Long> ids);

    /**
     * 获得回款计划分页
     *
     * 数据权限：基于 {@link CrmReceivablePlanDO} 读取
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 回款计划分页
     */
    PageResult<CrmReceivablePlanDO> getReceivablePlanPage(CrmReceivablePlanPageReqVO pageReqVO, Long userId);

    /**
     * 获得回款计划分页，基于指定客户
     *
     * 数据权限：基于 {@link CrmCustomerDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 回款计划分页
     */
    PageResult<CrmReceivablePlanDO> getReceivablePlanPageByCustomerId(CrmReceivablePlanPageReqVO pageReqVO);

    /**
     * 获得待回款提醒数量
     *
     * @param userId 用户编号
     * @return 提醒数量
     */
    Long getReceivablePlanRemindCount(Long userId);

}
