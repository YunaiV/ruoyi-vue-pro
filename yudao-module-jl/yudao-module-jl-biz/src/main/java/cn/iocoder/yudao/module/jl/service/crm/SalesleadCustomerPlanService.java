package cn.iocoder.yudao.module.jl.service.crm;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 销售线索中的客户方案 Service 接口
 *
 */
public interface SalesleadCustomerPlanService {

    /**
     * 创建销售线索中的客户方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalesleadCustomerPlan(@Valid SalesleadCustomerPlanCreateReqVO createReqVO);

    /**
     * 更新销售线索中的客户方案
     *
     * @param updateReqVO 更新信息
     */
    void updateSalesleadCustomerPlan(@Valid SalesleadCustomerPlanUpdateReqVO updateReqVO);

    /**
     * 删除销售线索中的客户方案
     *
     * @param id 编号
     */
    void deleteSalesleadCustomerPlan(Long id);

    /**
     * 获得销售线索中的客户方案
     *
     * @param id 编号
     * @return 销售线索中的客户方案
     */
    Optional<SalesleadCustomerPlan> getSalesleadCustomerPlan(Long id);

    /**
     * 获得销售线索中的客户方案列表
     *
     * @param ids 编号
     * @return 销售线索中的客户方案列表
     */
    List<SalesleadCustomerPlan> getSalesleadCustomerPlanList(Collection<Long> ids);

    /**
     * 获得销售线索中的客户方案分页
     *
     * @param pageReqVO 分页查询
     * @return 销售线索中的客户方案分页
     */
    PageResult<SalesleadCustomerPlan> getSalesleadCustomerPlanPage(SalesleadCustomerPlanPageReqVO pageReqVO, SalesleadCustomerPlanPageOrder orderV0);

    /**
     * 获得销售线索中的客户方案列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 销售线索中的客户方案列表
     */
    List<SalesleadCustomerPlan> getSalesleadCustomerPlanList(SalesleadCustomerPlanExportReqVO exportReqVO);

}
