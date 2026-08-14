package cn.iocoder.yudao.module.hrm.job.employee;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeQuitInfoService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * HRM 员工异动生效 Job
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class HrmEmployeeChangeJob implements JobHandler {

    @Resource
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmEmployeeQuitInfoService employeeQuitInfoService;

    @Override
    @TenantJob
    public String execute(String param) {
        LocalDateTime deadlineTime = StrUtil.isBlank(param) ? LocalDateTime.now()
                : LocalDateTimeUtils.getDayEndTime(LocalDate.parse(param));
        Set<Long> affectedEmployeeIds = new HashSet<>();

        // 1. 逐条应用到期且尚未生效的员工异动记录
        List<HrmEmployeeChangeRecordDO> changeRecords =
                employeeChangeRecordService.getPendingEmployeeChangeRecordList(deadlineTime);
        int changeSuccessCount = 0;
        int failureCount = 0;
        for (HrmEmployeeChangeRecordDO changeRecord : changeRecords) {
            try {
                if (employeeService.applyEmployeeChange(changeRecord)) {
                    affectedEmployeeIds.add(changeRecord.getEmployeeId());
                    changeSuccessCount++;
                }
            } catch (RuntimeException ex) {
                failureCount++;
                log.error("[execute][应用员工异动记录失败，changeRecordId=({})]", changeRecord.getId(), ex);
            }
        }

        // 2. 逐个应用到期转正
        List<HrmEmployeeDO> regularEmployees = employeeService.getDueRegularEmployeeList(deadlineTime);
        int regularEmployeeCount = 0;
        for (HrmEmployeeDO employee : regularEmployees) {
            try {
                if (employeeService.applyEmployeeRegular(employee.getId())) {
                    affectedEmployeeIds.add(employee.getId());
                    regularEmployeeCount++;
                }
            } catch (RuntimeException ex) {
                failureCount++;
                log.error("[execute][应用员工转正失败，employeeId=({})]", employee.getId(), ex);
            }
        }

        // 3. 逐个应用到期离职
        List<HrmEmployeeQuitInfoDO> quitInfos =
                employeeQuitInfoService.getDueQuitInfoList(deadlineTime);
        int quitEmployeeCount = 0;
        for (HrmEmployeeQuitInfoDO quitInfo : quitInfos) {
            try {
                if (employeeService.applyEmployeeQuit(quitInfo)) {
                    affectedEmployeeIds.add(quitInfo.getEmployeeId());
                    quitEmployeeCount++;
                }
            } catch (RuntimeException ex) {
                failureCount++;
                log.error("[execute][应用员工离职失败，quitInfoId=({})]", quitInfo.getId(), ex);
            }
        }

        // 4. 返回执行结果
        return String.format("HRM 员工异动生效：截止 %s，异动 %s/%s 条，转正 %s/%s 人，离职 %s/%s 人，"
                        + "失败 %s 条，影响员工 %s 人",
                deadlineTime, changeSuccessCount, changeRecords.size(), regularEmployeeCount,
                regularEmployees.size(), quitEmployeeCount, quitInfos.size(), failureCount,
                affectedEmployeeIds.size());
    }

}
