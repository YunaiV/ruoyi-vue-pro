package cn.iocoder.yudao.module.mes.dal.mysql.cal.plan;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 计划班组关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesCalPlanTeamMapper extends BaseMapperX<MesCalPlanTeamDO> {

    default List<MesCalPlanTeamDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<MesCalPlanTeamDO>()
                .eq(MesCalPlanTeamDO::getPlanId, planId)
                .orderByDesc(MesCalPlanTeamDO::getId));
    }

    default Long selectCountByPlanId(Long planId) {
        return selectCount(MesCalPlanTeamDO::getPlanId, planId);
    }

    default MesCalPlanTeamDO selectByPlanIdAndTeamId(Long planId, Long teamId) {
        return selectOne(new LambdaQueryWrapperX<MesCalPlanTeamDO>()
                .eq(MesCalPlanTeamDO::getPlanId, planId)
                .eq(MesCalPlanTeamDO::getTeamId, teamId));
    }

    default void deleteByPlanId(Long planId) {
        delete(new LambdaQueryWrapperX<MesCalPlanTeamDO>()
                .eq(MesCalPlanTeamDO::getPlanId, planId));
    }

}
