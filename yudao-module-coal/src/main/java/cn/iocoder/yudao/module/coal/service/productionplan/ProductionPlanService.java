package cn.iocoder.yudao.module.coal.service.productionplan;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo.*;
import cn.iocoder.yudao.module.coal.dal.dataobject.productionplan.ProductionPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 生产计划 Service 接口
 *
 * @author 京京
 */
public interface ProductionPlanService {

    /**
     * 创建生产计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductionPlan(@Valid ProductionPlanSaveReqVO createReqVO);

    /**
     * 更新生产计划
     *
     * @param updateReqVO 更新信息
     */
    void updateProductionPlan(@Valid ProductionPlanSaveReqVO updateReqVO);

    /**
     * 删除生产计划
     *
     * @param id 编号
     */
    void deleteProductionPlan(Long id);


    /**
     * 获得生产计划
     *
     * @param id 编号
     * @return 生产计划
     */
    ProductionPlanDO getProductionPlan(Long id);

    /**
     * 获得生产计划列表
     *
     * @param listReqVO 查询条件
     * @return 生产计划列表
     */
    List<ProductionPlanDO> getProductionPlanList(ProductionPlanListReqVO listReqVO);

    /**
     * 一键生成年度计划
     *
     * @param createReqVO 年度计划信息
     */
    void generateYearlyPlan(@Valid ProductionPlanSaveReqVO createReqVO);

    /**
     * 根据年份删除生产计划
     *
     * @param year 年份
     */
    void deleteProductionPlanByYear(Integer year);

    /**
     * 根据年份物理删除生产计划（调试用）
     *
     * @param year 年份
     */
    void physicalDeleteProductionPlanByYear(Integer year);
}