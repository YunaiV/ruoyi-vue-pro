package cn.iocoder.yudao.module.crm.service.receivable;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivablePlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 回款计划 Service 接口
 *
 * @author 芋道源码
 */
public interface ReceivablePlanService {

    /**
     * 创建回款计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReceivablePlan(@Valid ReceivablePlanCreateReqVO createReqVO);

    /**
     * 更新回款计划
     *
     * @param updateReqVO 更新信息
     */
    void updateReceivablePlan(@Valid ReceivablePlanUpdateReqVO updateReqVO);

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
    ReceivablePlanDO getReceivablePlan(Long id);

    /**
     * 获得回款计划列表
     *
     * @param ids 编号
     * @return 回款计划列表
     */
    List<ReceivablePlanDO> getReceivablePlanList(Collection<Long> ids);

    /**
     * 获得回款计划分页
     *
     * @param pageReqVO 分页查询
     * @return 回款计划分页
     */
    PageResult<ReceivablePlanDO> getReceivablePlanPage(ReceivablePlanPageReqVO pageReqVO);

    /**
     * 获得回款计划列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 回款计划列表
     */
    List<ReceivablePlanDO> getReceivablePlanList(ReceivablePlanExportReqVO exportReqVO);

}
