package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.shift.MesCalPlanShiftPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.shift.MesCalPlanShiftSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 计划班次 Service 接口
 *
 * @author 芋道源码
 */
public interface MesCalPlanShiftService {

    /**
     * 创建计划班次
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPlanShift(@Valid MesCalPlanShiftSaveReqVO createReqVO);

    /**
     * 更新计划班次
     *
     * @param updateReqVO 更新信息
     */
    void updatePlanShift(@Valid MesCalPlanShiftSaveReqVO updateReqVO);

    /**
     * 删除计划班次
     *
     * @param id 编号
     */
    void deletePlanShift(Long id);

    /**
     * 获得计划班次
     *
     * @param id 编号
     * @return 计划班次
     */
    MesCalPlanShiftDO getPlanShift(Long id);

    /**
     * 获得计划班次分页
     *
     * @param pageReqVO 分页查询
     * @return 计划班次分页
     */
    PageResult<MesCalPlanShiftDO> getPlanShiftPage(MesCalPlanShiftPageReqVO pageReqVO);

    /**
     * 获得指定排班计划的班次列表
     *
     * @param planId 排班计划编号
     * @return 班次列表
     */
    List<MesCalPlanShiftDO> getPlanShiftListByPlanId(Long planId);

    /**
     * 获得指定排班计划的班次数量
     *
     * @param planId 排班计划编号
     * @return 班次数量
     */
    Long getPlanShiftCountByPlanId(Long planId);

    /**
     * 根据轮班方式添加默认班次
     *
     * @param planId    排班计划编号
     * @param shiftType 轮班方式
     */
    void addDefaultPlanShift(Long planId, Integer shiftType);

    /**
     * 获得计划班次列表
     *
     * @param ids 班次编号集合
     * @return 班次列表
     */
    List<MesCalPlanShiftDO> getPlanShiftList(Collection<Long> ids);

    /**
     * 获得计划班次 Map
     *
     * @param ids 班次编号集合
     * @return 班次 Map，key 为编号
     */
    default Map<Long, MesCalPlanShiftDO> getPlanShiftMap(Collection<Long> ids) {
        return convertMap(getPlanShiftList(ids), MesCalPlanShiftDO::getId);
    }

    /**
     * 根据排班计划编号删除所有班次
     *
     * @param planId 排班计划编号
     */
    void deletePlanShiftByPlanId(Long planId);

}
