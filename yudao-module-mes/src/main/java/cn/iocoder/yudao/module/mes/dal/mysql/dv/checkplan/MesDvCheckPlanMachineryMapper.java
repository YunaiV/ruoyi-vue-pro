package cn.iocoder.yudao.module.mes.dal.mysql.dv.checkplan;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanMachineryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 点检保养方案设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesDvCheckPlanMachineryMapper extends BaseMapperX<MesDvCheckPlanMachineryDO> {

    default List<MesDvCheckPlanMachineryDO> selectListByPlanId(Long planId) {
        return selectList(MesDvCheckPlanMachineryDO::getPlanId, planId);
    }

    default Long selectCountByPlanId(Long planId) {
        return selectCount(MesDvCheckPlanMachineryDO::getPlanId, planId);
    }

    default void deleteByPlanId(Long planId) {
        delete(new LambdaQueryWrapperX<MesDvCheckPlanMachineryDO>()
                .eq(MesDvCheckPlanMachineryDO::getPlanId, planId));
    }

    default Long selectCountByMachineryId(Long machineryId) {
        return selectCount(MesDvCheckPlanMachineryDO::getMachineryId, machineryId);
    }

    default List<MesDvCheckPlanMachineryDO> selectListByMachineryId(Long machineryId) {
        return selectList(MesDvCheckPlanMachineryDO::getMachineryId, machineryId);
    }

    default MesDvCheckPlanMachineryDO selectByPlanIdAndMachineryId(Long planId, Long machineryId) {
        return selectOne(new LambdaQueryWrapperX<MesDvCheckPlanMachineryDO>()
                .eq(MesDvCheckPlanMachineryDO::getPlanId, planId)
                .eq(MesDvCheckPlanMachineryDO::getMachineryId, machineryId));
    }

}
