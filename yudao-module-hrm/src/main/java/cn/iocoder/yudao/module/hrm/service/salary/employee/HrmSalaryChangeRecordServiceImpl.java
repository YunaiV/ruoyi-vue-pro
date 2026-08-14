package cn.iocoder.yudao.module.hrm.service.salary.employee;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.employee.HrmSalaryChangeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_RECORD_STATUS_INVALID;

/**
 * HRM 定薪/调薪记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryChangeRecordServiceImpl implements HrmSalaryChangeRecordService {

    @Resource
    private HrmSalaryChangeRecordMapper salaryChangeRecordMapper;

    @Override
    public Long createSalaryChangeRecord(HrmSalaryChangeRecordDO changeRecord) {
        salaryChangeRecordMapper.insert(changeRecord);
        return changeRecord.getId();
    }

    @Override
    public void updateSalaryChangeRecord(HrmSalaryChangeRecordDO changeRecord) {
        salaryChangeRecordMapper.updateById(changeRecord);
    }

    @Override
    public boolean updateSalaryChangeRecordStatus(Long id, Integer oldStatus, Integer newStatus) {
        return salaryChangeRecordMapper.updateByIdAndStatus(
                new HrmSalaryChangeRecordDO().setId(id).setStatus(newStatus), oldStatus) > 0;
    }

    @Override
    public HrmSalaryChangeRecordDO validateSalaryChangeRecordExists(Long id) {
        HrmSalaryChangeRecordDO changeRecord = salaryChangeRecordMapper.selectById(id);
        if (changeRecord == null) {
            throw exception(SALARY_CHANGE_RECORD_NOT_EXISTS);
        }
        return changeRecord;
    }

    @Override
    public boolean hasPendingSalaryChangeRecord(Long employeeId, Long excludeId) {
        return salaryChangeRecordMapper.selectCountByEmployeeIdAndStatus(
                employeeId, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), excludeId) > 0;
    }

    @Override
    public boolean hasUncancelledSalaryAdjustmentRecord(Long employeeId) {
        return salaryChangeRecordMapper.selectCountByEmployeeIdAndTypeAndStatusNot(
                employeeId, HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType(),
                HrmSalaryChangeRecordStatusEnum.CANCELLED.getStatus()) > 0;
    }

    @Override
    public HrmSalaryChangeRecordDO getSalarySetRecordByEmployeeId(Long employeeId) {
        return salaryChangeRecordMapper.selectByEmployeeIdAndType(
                employeeId, HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType());
    }

    @Override
    public HrmSalaryChangeRecordDO getSalaryChangeRecord(Long id) {
        return salaryChangeRecordMapper.selectById(id);
    }

    @Override
    public List<HrmSalaryChangeRecordDO> getSalaryChangeRecordList(Long employeeId) {
        return salaryChangeRecordMapper.selectListByEmployeeId(employeeId);
    }

    @Override
    public void cancelSalaryChangeRecord(Long id) {
        // 1. 校验记录处于待生效状态
        HrmSalaryChangeRecordDO changeRecord = validateSalaryChangeRecordExists(id);
        if (ObjUtil.notEqual(changeRecord.getStatus(), HrmSalaryChangeRecordStatusEnum.PENDING.getStatus())) {
            throw exception(SALARY_CHANGE_RECORD_STATUS_INVALID);
        }

        // 2. 取消定薪/调薪记录
        if (!updateSalaryChangeRecordStatus(id, HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                HrmSalaryChangeRecordStatusEnum.CANCELLED.getStatus())) {
            throw exception(SALARY_CHANGE_RECORD_STATUS_INVALID);
        }
    }

    @Override
    public void deleteSalaryChangeRecord(Long id) {
        // 1. 校验已生效记录不能删除
        HrmSalaryChangeRecordDO changeRecord = validateSalaryChangeRecordExists(id);
        if (ObjUtil.equal(changeRecord.getStatus(), HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus())) {
            throw exception(SALARY_CHANGE_RECORD_STATUS_INVALID);
        }

        // 2. 删除定薪/调薪记录
        salaryChangeRecordMapper.deleteById(id);
    }

    @Override
    public long getDueSalaryChangeCount(LocalDate targetDate) {
        LocalDate date = targetDate == null ? LocalDate.now() : targetDate;
        LocalDateTime dayEndTime = LocalDateTimeUtils.getDayEndTime(date);
        return salaryChangeRecordMapper.selectCountByStatusAndEffectTimeBeforeOrEqual(
                HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), dayEndTime);
    }

    @Override
    public List<HrmSalaryChangeRecordDO> getDueSalaryChangeRecordList(LocalDate targetDate) {
        LocalDate date = targetDate == null ? LocalDate.now() : targetDate;
        LocalDateTime dayEndTime = LocalDateTimeUtils.getDayEndTime(date);
        return salaryChangeRecordMapper.selectListByStatusAndEffectTimeBeforeOrEqual(
                HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), dayEndTime);
    }

}
