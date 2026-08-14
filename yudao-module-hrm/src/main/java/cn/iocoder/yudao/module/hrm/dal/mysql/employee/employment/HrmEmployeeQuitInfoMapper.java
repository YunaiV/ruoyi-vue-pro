package cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmEmployeeQuitInfoMapper extends BaseMapperX<HrmEmployeeQuitInfoDO> {

    default HrmEmployeeQuitInfoDO selectByEmployeeId(Long employeeId) {
        return selectLastOne(new LambdaQueryWrapperX<HrmEmployeeQuitInfoDO>()
                .eq(HrmEmployeeQuitInfoDO::getEmployeeId, employeeId)
                .orderByAsc(HrmEmployeeQuitInfoDO::getId));
    }

    default List<HrmEmployeeQuitInfoDO> selectListByEmployeeIds(Collection<Long> employeeIds) {
        return selectList(HrmEmployeeQuitInfoDO::getEmployeeId, employeeIds);
    }

    default void deleteByEmployeeId(Long employeeId) {
        delete(HrmEmployeeQuitInfoDO::getEmployeeId, employeeId);
    }

    default List<HrmEmployeeQuitInfoDO> selectListByPlanQuitTimeBeforeOrEqual(LocalDateTime deadlineTime) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeQuitInfoDO>()
                .le(HrmEmployeeQuitInfoDO::getPlanQuitTime, deadlineTime)
                .orderByAsc(HrmEmployeeQuitInfoDO::getPlanQuitTime)
                .orderByAsc(HrmEmployeeQuitInfoDO::getId));
    }

}
