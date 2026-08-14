package cn.iocoder.yudao.module.hrm.dal.mysql.salary.employee;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HrmSalaryChangeRecordMapper extends BaseMapperX<HrmSalaryChangeRecordDO> {

    default List<HrmSalaryChangeRecordDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryChangeRecordDO>()
                .eq(HrmSalaryChangeRecordDO::getEmployeeId, employeeId)
                .orderByDesc(HrmSalaryChangeRecordDO::getEffectTime)
                .orderByDesc(HrmSalaryChangeRecordDO::getId));
    }

    default HrmSalaryChangeRecordDO selectByEmployeeIdAndType(Long employeeId, Integer type) {
        return selectLastOne(new LambdaQueryWrapperX<HrmSalaryChangeRecordDO>()
                .eq(HrmSalaryChangeRecordDO::getEmployeeId, employeeId)
                .eq(HrmSalaryChangeRecordDO::getType, type)
                .orderByAsc(HrmSalaryChangeRecordDO::getId));
    }

    default Long selectCountByEmployeeIdAndStatus(Long employeeId, Integer status, Long excludeId) {
        return selectCount(new LambdaQueryWrapperX<HrmSalaryChangeRecordDO>()
                .eq(HrmSalaryChangeRecordDO::getEmployeeId, employeeId)
                .eq(HrmSalaryChangeRecordDO::getStatus, status)
                .neIfPresent(HrmSalaryChangeRecordDO::getId, excludeId));
    }

    default Long selectCountByEmployeeIdAndTypeAndStatusNot(
            Long employeeId, Integer type, Integer status) {
        return selectCount(new LambdaQueryWrapperX<HrmSalaryChangeRecordDO>()
                .eq(HrmSalaryChangeRecordDO::getEmployeeId, employeeId)
                .eq(HrmSalaryChangeRecordDO::getType, type)
                .ne(HrmSalaryChangeRecordDO::getStatus, status));
    }

    default List<HrmSalaryChangeRecordDO> selectListByStatusAndEffectTimeBeforeOrEqual(
            Integer status, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<HrmSalaryChangeRecordDO>()
                .eq(HrmSalaryChangeRecordDO::getStatus, status)
                .le(HrmSalaryChangeRecordDO::getEffectTime, endTime)
                .orderByAsc(HrmSalaryChangeRecordDO::getEffectTime)
                .orderByAsc(HrmSalaryChangeRecordDO::getId));
    }

    default Long selectCountByStatusAndEffectTimeBeforeOrEqual(Integer status, LocalDateTime endTime) {
        return selectCount(new LambdaQueryWrapperX<HrmSalaryChangeRecordDO>()
                .eq(HrmSalaryChangeRecordDO::getStatus, status)
                .le(HrmSalaryChangeRecordDO::getEffectTime, endTime));
    }

    default int updateByIdAndStatus(HrmSalaryChangeRecordDO updateObj, Integer status) {
        return update(updateObj,
                new LambdaUpdateWrapper<HrmSalaryChangeRecordDO>()
                        .eq(HrmSalaryChangeRecordDO::getId, updateObj.getId())
                        .eq(HrmSalaryChangeRecordDO::getStatus, status));
    }

}
