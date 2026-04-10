package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 排班计划 Service 接口
 *
 * @author 芋道源码
 */
public interface MesCalPlanService {

    /**
     * 创建排班计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPlan(@Valid MesCalPlanSaveReqVO createReqVO);

    /**
     * 更新排班计划
     *
     * @param updateReqVO 更新信息
     */
    void updatePlan(@Valid MesCalPlanSaveReqVO updateReqVO);

    /**
     * 确认排班计划
     *
     * @param id 编号
     */
    void confirmPlan(Long id);

    /**
     * 删除排班计划
     *
     * @param id 编号
     */
    void deletePlan(Long id);

    /**
     * 校验排班计划为草稿状态（确认后不允许编辑）
     *
     * @param planId 计划编号
     * @return 排班计划
     */
    MesCalPlanDO validatePlanPrepare(Long planId);

    /**
     * 获得排班计划
     *
     * @param id 编号
     * @return 排班计划
     */
    MesCalPlanDO getPlan(Long id);

    /**
     * 获得排班计划列表
     *
     * @param ids 排班计划编号集合
     * @return 排班计划列表
     */
    List<MesCalPlanDO> getPlanList(Collection<Long> ids);

    /**
     * 获得排班计划 Map
     *
     * @param ids 排班计划编号集合
     * @return 排班计划 Map，key 为编号
     */
    default Map<Long, MesCalPlanDO> getPlanMap(Collection<Long> ids) {
        return convertMap(getPlanList(ids), MesCalPlanDO::getId);
    }

    /**
     * 获得排班计划分页
     *
     * @param pageReqVO 分页查询
     * @return 排班计划分页
     */
    PageResult<MesCalPlanDO> getPlanPage(MesCalPlanPageReqVO pageReqVO);

}
