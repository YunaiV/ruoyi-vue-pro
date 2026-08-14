package cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmEmployeeChangeRecordMapper extends BaseMapperX<HrmEmployeeChangeRecordDO> {

    default List<HrmEmployeeChangeRecordDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeChangeRecordDO>()
                .eq(HrmEmployeeChangeRecordDO::getEmployeeId, employeeId)
                .orderByDesc(HrmEmployeeChangeRecordDO::getEffectTime)
                .orderByDesc(HrmEmployeeChangeRecordDO::getId));
    }

    default List<HrmEmployeeChangeRecordDO> selectListByAppliedTimeNullAndEffectTimeBeforeOrEqual(
            LocalDateTime deadlineTime, Collection<Integer> types) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeChangeRecordDO>()
                .isNull(HrmEmployeeChangeRecordDO::getAppliedTime)
                .le(HrmEmployeeChangeRecordDO::getEffectTime, deadlineTime)
                .in(HrmEmployeeChangeRecordDO::getType, types)
                .orderByAsc(HrmEmployeeChangeRecordDO::getEffectTime)
                .orderByAsc(HrmEmployeeChangeRecordDO::getId));
    }

    default List<HrmEmployeeChangeRecordDO> selectListByEmployeeIdsAndEffectTimeBetween(Collection<Long> employeeIds,
                                                                                       LocalDateTime[] effectTimes) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeChangeRecordDO>()
                .in(HrmEmployeeChangeRecordDO::getEmployeeId, employeeIds)
                .betweenIfPresent(HrmEmployeeChangeRecordDO::getEffectTime, effectTimes)
                .orderByDesc(HrmEmployeeChangeRecordDO::getEffectTime)
                .orderByDesc(HrmEmployeeChangeRecordDO::getId));
    }

}
