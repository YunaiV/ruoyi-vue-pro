package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.team.MesCalPlanTeamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 计划班组关联 Service 接口
 *
 * @author 芋道源码
 */
public interface MesCalPlanTeamService {

    /**
     * 创建计划班组关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPlanTeam(@Valid MesCalPlanTeamSaveReqVO createReqVO);

    /**
     * 删除计划班组关联
     *
     * @param id 编号
     */
    void deletePlanTeam(Long id);

    /**
     * 获得指定排班计划的班组列表
     *
     * @param planId 排班计划编号
     * @return 班组关联列表
     */
    List<MesCalPlanTeamDO> getPlanTeamListByPlanId(Long planId);

    /**
     * 获得指定排班计划的班组数量
     *
     * @param planId 排班计划编号
     * @return 班组数量
     */
    Long getPlanTeamCountByPlanId(Long planId);

    /**
     * 根据排班计划编号删除所有班组关联
     *
     * @param planId 排班计划编号
     */
    void deleteByPlanId(Long planId);

}
