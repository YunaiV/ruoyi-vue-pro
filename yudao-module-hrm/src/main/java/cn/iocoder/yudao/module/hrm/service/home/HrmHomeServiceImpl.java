package cn.iocoder.yudao.module.hrm.service.home;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHomeCalendarItemRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO.AnalysisItem;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO.TeamOverview;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO.TeamSurvey;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO.EmployeeSurvey;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO.RecruitSurvey;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO.SalaryDept;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO.SalarySurvey;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO.TodoSurvey;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeePersonalNoteDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusTabEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeSurveyTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTodoTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.home.HrmHomeAgeRangeEnum;
import cn.iocoder.yudao.module.hrm.enums.home.HrmHomeCalendarItemTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.home.HrmHomeCompanyAgeRangeEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.post.HrmRecruitPostStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceClockService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeContractService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeQuitInfoService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeePersonalNoteService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.recruit.candidate.HrmRecruitCandidateService;
import cn.iocoder.yudao.module.hrm.service.recruit.candidate.HrmRecruitInterviewService;
import cn.iocoder.yudao.module.hrm.service.recruit.post.HrmRecruitPostService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetBySupplier;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.count;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.distinctCount;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.sum;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.HOME_CALENDAR_DATE_RANGE_ILLEGAL;

/**
 * HRM 首页 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmHomeServiceImpl implements HrmHomeService {

    /**
     * 首页日历面试时间格式
     */
    private static final DateTimeFormatter INTERVIEW_TIME_FORMATTER = DatePattern.createFormatter("HH:mm");

    @Resource
    private HrmEmployeeContractService employeeContractService;
    @Resource
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @Resource
    private HrmEmployeeQuitInfoService employeeQuitInfoService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmEmployeePersonalNoteService personalNoteService;
    @Resource
    private HrmRecruitPostService recruitPostService;
    @Resource
    private HrmRecruitCandidateService recruitCandidateService;
    @Resource
    private HrmRecruitInterviewService recruitInterviewService;
    @Resource
    private HrmAttendanceClockService attendanceClockService;
    @Resource
    private HrmSalaryMonthRecordService salaryMonthRecordService;
    @Resource
    private HrmSalaryMonthEmployeeRecordService salaryMonthEmployeeRecordService;

    @Resource
    private DeptApi deptApi;

    @Override
    public HrmHrHomeStatisticsRespVO getHrHomeStatisticsSummary() {
        // 1. 统计人事、招聘和薪资概况
        HrmHrHomeStatisticsRespVO summary = new HrmHrHomeStatisticsRespVO();
        EmployeeSurvey employeeSurvey = buildEmployeeSurvey();
        summary.setEmployeeSurvey(employeeSurvey);
        summary.setRecruitSurvey(buildRecruitSurvey());
        summary.setSalarySurvey(buildSalarySurvey());

        // 2. 基于人事概况统计待办提醒
        summary.setTodoSurvey(buildTodoSurvey(employeeSurvey));
        return summary;
    }

    @Override
    public HrmTeamHomeStatisticsRespVO getTeamHomeStatisticsSummary(Long loginUserId) {
        // 1. 查询当前登录员工及其直属下属
        HrmEmployeeDO loginEmployee = employeeService.getEmployeeByUserId(loginUserId);
        List<HrmEmployeeDO> teamEmployees = loginEmployee == null ? Collections.emptyList()
                : employeeService.getEmployeeListByLeaderEmployeeId(loginEmployee.getId());
        List<HrmEmployeeDO> activeTeamEmployees = filterList(teamEmployees,
                employee -> HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus()));

        // 2. 统计本月团队人数变动
        HrmTeamHomeStatisticsRespVO summary = new HrmTeamHomeStatisticsRespVO();
        summary.setLeaderEmployeeId(loginEmployee == null ? null : loginEmployee.getId());
        summary.setTeamOverview(buildTeamOverview(teamEmployees, activeTeamEmployees));

        // 3. 统计当前在职团队的人员结构
        summary.setTeamSurvey(buildTeamSurvey(activeTeamEmployees));
        return summary;
    }

    @Override
    public List<HrmHomeCalendarItemRespVO> getHrHomeCalendar(
            Long loginUserId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return getCalendarItemList(loginUserId, startDate, endDate);
    }

    @Override
    public List<HrmHomeCalendarItemRespVO> getTeamHomeCalendar(
            Long loginUserId, LocalDate startDate, LocalDate endDate) {
        // 1. 校验日期范围并查询当前登录员工
        validateDateRange(startDate, endDate);
        HrmEmployeeDO loginEmployee = employeeService.getEmployeeByUserId(loginUserId);
        if (loginEmployee == null) {
            return Collections.emptyList();
        }

        // 2. 收集本人备忘及直属下属的人事日历事项
        List<HrmHomeCalendarItemRespVO> items = new ArrayList<>();
        addPersonalNoteItems(items, loginEmployee, startDate, endDate);
        addEmployeeItems(items, employeeService.getEmployeeListByLeaderEmployeeId(
                loginEmployee.getId()), startDate, endDate);

        // 3. 对日历事项稳定排序
        sortCalendarItems(items);
        return items;
    }

    @Override
    public List<HrmHomeCalendarItemRespVO> getEmployeeCalendar(
            Long loginUserId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        HrmEmployeeDO loginEmployee = employeeService.getEmployeeByUserId(loginUserId);
        List<HrmHomeCalendarItemRespVO> items = new ArrayList<>();
        addPersonalNoteItems(items, loginEmployee, startDate, endDate);
        addAttendanceItems(items, loginEmployee, startDate, endDate);
        sortCalendarItems(items);
        return items;
    }

    // ==================== HR 工作台统计 ====================

    /**
     * 构建直属团队的人员变动概况
     *
     * @param teamEmployees 直属团队员工列表
     * @param activeTeamEmployees 当前在职的直属团队员工列表
     * @return 团队人员变动概况
     */
    private TeamOverview buildTeamOverview(List<HrmEmployeeDO> teamEmployees,
                                           List<HrmEmployeeDO> activeTeamEmployees) {
        // 1. 统计团队人数和本月入离职人数
        LocalDate now = LocalDate.now();
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                now.getYear(), now.getMonthValue());
        TeamOverview overview = new TeamOverview();
        overview.setEmployeeCount((long) activeTeamEmployees.size());
        overview.setEntryThisMonthCount(count(teamEmployees,
                employee -> !HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus()
                        .equals(employee.getEntryStatus())
                        && LocalDateTimeUtils.isBetween(monthTimes[0], monthTimes[1], employee.getEntryTime())));
        overview.setLeaveThisMonthCount(count(teamEmployees,
                employee -> HrmEmployeeEntryStatusEnum.LEFT.getStatus().equals(employee.getEntryStatus())
                        && LocalDateTimeUtils.isBetween(monthTimes[0], monthTimes[1], employee.getLeaveTime())));

        // 2. 按异动记录去重统计本月转正人数
        if (CollUtil.isEmpty(teamEmployees)) {
            overview.setRegularThisMonthCount(0L);
            return overview;
        }
        List<HrmEmployeeChangeRecordDO> changeRecords = employeeChangeRecordService
                .getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                        convertSet(teamEmployees, HrmEmployeeDO::getId), monthTimes);
        overview.setRegularThisMonthCount(distinctCount(filterList(changeRecords,
                        record -> HrmEmployeeChangeTypeEnum.REGULAR.getType().equals(record.getType())),
                HrmEmployeeChangeRecordDO::getEmployeeId));
        return overview;
    }

    private TeamSurvey buildTeamSurvey(List<HrmEmployeeDO> teamEmployees) {
        return new TeamSurvey()
                .setStatusAnalysis(buildAnalysis(buildStatusCountMap(teamEmployees)))
                .setSexAnalysis(buildAnalysis(buildSexCountMap(teamEmployees)))
                .setAgeAnalysis(buildAnalysis(buildAgeCountMap(teamEmployees)))
                .setCompanyAgeAnalysis(buildAnalysis(buildCompanyAgeCountMap(teamEmployees)));
    }

    private Map<Integer, Long> buildStatusCountMap(List<HrmEmployeeDO> teamEmployees) {
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        for (HrmEmployeeStatusEnum status : HrmEmployeeStatusEnum.values()) {
            countMap.put(status.getStatus(), 0L);
        }
        countMap.put(null, 0L);
        for (HrmEmployeeDO employee : teamEmployees) {
            countMap.merge(employee.getStatus(), 1L, Long::sum);
        }
        return countMap;
    }

    private Map<Integer, Long> buildSexCountMap(List<HrmEmployeeDO> teamEmployees) {
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        countMap.put(SexEnum.MALE.getSex(), 0L);
        countMap.put(SexEnum.FEMALE.getSex(), 0L);
        countMap.put(SexEnum.UNKNOWN.getSex(), 0L);
        countMap.put(null, 0L);
        for (HrmEmployeeDO employee : teamEmployees) {
            countMap.merge(employee.getSex(), 1L, Long::sum);
        }
        return countMap;
    }

    private Map<Integer, Long> buildAgeCountMap(List<HrmEmployeeDO> teamEmployees) {
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        countMap.put(null, 0L);
        for (HrmHomeAgeRangeEnum range : HrmHomeAgeRangeEnum.values()) {
            countMap.put(range.getType(), 0L);
        }
        for (HrmEmployeeDO employee : teamEmployees) {
            Integer age = employee.getAge();
            if (employee.getBirthday() != null) {
                age = LocalDateTimeUtils.getYearsBetween(employee.getBirthday().toLocalDate(), LocalDate.now());
            }
            countMap.merge(HrmHomeAgeRangeEnum.getType(age), 1L, Long::sum);
        }
        return countMap;
    }

    private Map<Integer, Long> buildCompanyAgeCountMap(List<HrmEmployeeDO> teamEmployees) {
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        countMap.put(null, 0L);
        for (HrmHomeCompanyAgeRangeEnum range : HrmHomeCompanyAgeRangeEnum.values()) {
            countMap.put(range.getType(), 0L);
        }
        for (HrmEmployeeDO employee : teamEmployees) {
            long companyAgeDays = employee.getCompanyAgeStartTime() != null
                    ? ChronoUnit.DAYS.between(employee.getCompanyAgeStartTime().toLocalDate(), LocalDate.now())
                    : employee.getCompanyAge() == null ? -1L : employee.getCompanyAge() * 365L;
            countMap.merge(HrmHomeCompanyAgeRangeEnum.getType(companyAgeDays), 1L, Long::sum);
        }
        return countMap;
    }

    private List<AnalysisItem> buildAnalysis(Map<Integer, Long> countMap) {
        return convertList(countMap.entrySet(), entry -> new AnalysisItem()
                .setType(entry.getKey()).setCount(entry.getValue()));
    }

    private EmployeeSurvey buildEmployeeSurvey() {
        EmployeeSurvey survey = new EmployeeSurvey();
        // 1. 统计当前在职员工总数
        survey.setActiveCount(employeeService.getEmployeeStatusCount(new HrmEmployeePageReqVO())
                .getOrDefault(HrmEmployeeStatusTabEnum.ACTIVE.getStatus(), 0L));
        // 2. 按员工主档、离职状态和异动记录聚合统计本月六类人员变动
        Map<Integer, Long> surveyCountMap = employeeService.getEmployeeSurveyCountMap();
        survey.setEntryThisMonthCount(surveyCountMap.getOrDefault(
                HrmEmployeeSurveyTypeEnum.ENTRY.getType(), 0L));
        survey.setPendingEntryThisMonthCount(surveyCountMap.getOrDefault(
                HrmEmployeeSurveyTypeEnum.PENDING_ENTRY.getType(), 0L));
        survey.setLeaveThisMonthCount(surveyCountMap.getOrDefault(
                HrmEmployeeSurveyTypeEnum.LEAVE.getType(), 0L));
        survey.setPendingLeaveThisMonthCount(surveyCountMap.getOrDefault(
                HrmEmployeeSurveyTypeEnum.PENDING_LEAVE.getType(), 0L));
        survey.setRegularThisMonthCount(surveyCountMap.getOrDefault(
                HrmEmployeeSurveyTypeEnum.REGULAR.getType(), 0L));
        survey.setTransferThisMonthCount(surveyCountMap.getOrDefault(
                HrmEmployeeSurveyTypeEnum.TRANSFER.getType(), 0L));
        return survey;
    }

    private RecruitSurvey buildRecruitSurvey() {
        // 1. 统计招聘中的职位和流程中的候选人
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime[] recentSixMonthTimes = new LocalDateTime[]{currentTime.minusMonths(6), currentTime};
        RecruitSurvey survey = new RecruitSurvey();
        survey.setRecruitingPostCount(recruitPostService.getRecruitPostStatusCount(new HrmRecruitPostPageReqVO())
                .getOrDefault(HrmRecruitPostStatusEnum.RECRUITING.getStatus(), 0L));
        Map<Integer, Long> candidateStatusCount = recruitCandidateService.getRecruitCandidateStatusCount(new HrmRecruitCandidatePageReqVO());
        survey.setCandidateInProcessCount(sum(candidateStatusCount.entrySet(), entry ->
                HrmRecruitCandidateStatusEnum.PROCESSING_STATUSES.contains(entry.getKey()) ? entry.getValue() : 0L));

        // 2. 统计近六个月待入职和已入职候选人
        survey.setPendingEntryCount(recruitCandidateService.getRecruitCandidateCountByStatusAndStatusUpdateTimeBetween(
                HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus(), recentSixMonthTimes));
        survey.setJoinedCount(recruitCandidateService.getRecruitCandidateCountByStatusAndEntryTimeBetween(
                HrmRecruitCandidateStatusEnum.JOINED.getStatus(), recentSixMonthTimes));
        return survey;
    }

    private SalarySurvey buildSalarySurvey() {
        SalarySurvey survey = new SalarySurvey();
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        HrmSalaryMonthRecordDO record = salaryMonthRecordService.getMonthRecordByYearMonth(
                lastMonth.getYear(), lastMonth.getMonthValue());
        if (record == null || ObjUtil.notEqual(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus(),
                record.getStatus())) {
            fillSalaryDefaults(survey);
            return survey;
        }
        survey.setMonthRecordId(record.getId());
        fillSalaryDeptProportion(survey, record);
        return survey;
    }

    /**
     * 拼接上月工资表的计薪人数、实发工资和部门薪资占比
     *
     * @param survey 薪资概况
     * @param record 月度工资表
     */
    private void fillSalaryDeptProportion(SalarySurvey survey,
                                          HrmSalaryMonthRecordDO record) {
        // 1. 查询员工工资明细和员工部门
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords = salaryMonthEmployeeRecordService
                .getMonthEmployeeRecordListByMonthRecordId(record.getId());
        if (CollUtil.isEmpty(employeeRecords)) {
            fillSalaryDefaults(survey);
            return;
        }
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeRecords.stream()
                .map(HrmSalaryMonthEmployeeRecordDO::getEmployeeId).collect(Collectors.toSet()));

        // 2. 按员工当前部门汇总实发工资
        Map<Long, BigDecimal> deptSalaryMap = new LinkedHashMap<>();
        BigDecimal totalSalary = BigDecimal.ZERO;
        for (HrmSalaryMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            BigDecimal realPaySalary = defaultAmount(employeeRecord.getRealPaySalary());
            HrmEmployeeDO employee = employeeMap.get(employeeRecord.getEmployeeId());
            Long deptId = employee == null || employee.getDeptId() == null ? 0L : employee.getDeptId();
            deptSalaryMap.merge(deptId, realPaySalary, BigDecimal::add);
            totalSalary = totalSalary.add(realPaySalary);
        }
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(deptSalaryMap.keySet().stream()
                .filter(deptId -> ObjUtil.notEqual(deptId, 0L)).collect(Collectors.toSet()));

        // 3. 计算部门薪资占比
        List<SalaryDept> deptProportions = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : deptSalaryMap.entrySet()) {
            SalaryDept deptProportion = new SalaryDept();
            deptProportion.setDeptId(entry.getKey());
            DeptRespDTO dept = deptMap.get(entry.getKey());
            deptProportion.setDeptName(Objects.equals(entry.getKey(), 0L)
                    ? "无部门" : dept == null ? "部门已删除" : dept.getName());
            deptProportion.setTotalSalary(entry.getValue());
            deptProportion.setProportion(totalSalary.signum() == 0 ? BigDecimal.ZERO
                    : entry.getValue().divide(totalSalary, 4, RoundingMode.UP));
            deptProportions.add(deptProportion);
        }
        deptProportions.sort((first, second) ->
                second.getTotalSalary().compareTo(first.getTotalSalary()));
        survey.setEmployeeCount(employeeRecords.size());
        survey.setRealPaySalary(totalSalary);
        survey.setDeptProportions(deptProportions);
    }

    private TodoSurvey buildTodoSurvey(EmployeeSurvey employeeSurvey) {
        LocalDate monthStartDate = LocalDate.now().withDayOfMonth(1);
        TodoSurvey survey = new TodoSurvey();
        survey.setToEntryCount(employeeSurvey.getPendingEntryThisMonthCount());
        survey.setToLeaveCount(employeeSurvey.getPendingLeaveThisMonthCount());
        survey.setToExpireContractCount(employeeContractService.getExpireEmployeeCountInMonth(
                LocalDateTimeUtils.getMonthDateTimeRange(monthStartDate.getYear(), monthStartDate.getMonthValue())));
        survey.setToRegularCount(getEmployeeTodoCount(
                HrmEmployeeTodoTypeEnum.REGULAR));
        survey.setToSalaryComputeCount((long) salaryMonthRecordService.getMonthRecordListByStatus(
                HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus()).size());
        survey.setToBirthdayCount(getEmployeeTodoCount(
                HrmEmployeeTodoTypeEnum.BIRTHDAY));
        return survey;
    }

    private Long getEmployeeTodoCount(HrmEmployeeTodoTypeEnum todoType) {
        Map<Integer, Long> employeeStatusCountMap = employeeService.getEmployeeStatusCount(new HrmEmployeePageReqVO()
                .setTodoType(todoType.getType()).setStatusCategory(HrmEmployeeStatusTabEnum.ACTIVE.getStatus()));
        return employeeStatusCountMap.getOrDefault(HrmEmployeeStatusTabEnum.ACTIVE.getStatus(), 0L);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private void fillSalaryDefaults(SalarySurvey survey) {
        survey.setEmployeeCount(0).setRealPaySalary(BigDecimal.ZERO).setDeptProportions(Collections.emptyList());
    }

    // ==================== 首页日历 ====================

    private List<HrmHomeCalendarItemRespVO> getCalendarItemList(
            Long loginUserId, LocalDate startDate, LocalDate endDate) {
        // 1. 收集各业务域的日历事项
        List<HrmHomeCalendarItemRespVO> items = new ArrayList<>();
        addPersonalNoteItems(items, employeeService.getEmployeeByUserId(loginUserId), startDate, endDate);
        addEmployeeItems(items, startDate, endDate);
        addRecruitItems(items, startDate, endDate);

        // 2. 对日历事项稳定排序
        sortCalendarItems(items);
        return items;
    }

    private void addPersonalNoteItems(
            List<HrmHomeCalendarItemRespVO> items, HrmEmployeeDO loginEmployee,
            LocalDate startDate, LocalDate endDate) {
        if (loginEmployee == null) {
            return;
        }
        List<HrmEmployeePersonalNoteDO> personalNotes = personalNoteService.getPersonalNoteList(
                loginEmployee.getId(), LocalDateTimeUtils.getDateTimeRange(startDate, endDate));
        for (HrmEmployeePersonalNoteDO personalNote : personalNotes) {
            items.add(buildPersonalNoteItem(personalNote));
        }
    }

    private void addEmployeeItems(List<HrmHomeCalendarItemRespVO> items, LocalDate startDate, LocalDate endDate) {
        addEmployeeItems(items, employeeService.getEmployeeList(new HrmEmployeeListReqVO()),
                startDate, endDate);
    }

    private void addEmployeeItems(List<HrmHomeCalendarItemRespVO> items,
                                  List<HrmEmployeeDO> employees,
                                  LocalDate startDate, LocalDate endDate) {
        Set<Long> pendingLeaveEmployeeIds = convertSet(filterList(employees, employee ->
                        HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus().equals(employee.getEntryStatus())),
                HrmEmployeeDO::getId);
        Map<Long, HrmEmployeeQuitInfoDO> quitInfoMap = CollUtil.isEmpty(pendingLeaveEmployeeIds)
                ? Collections.emptyMap()
                : convertMap(employeeQuitInfoService.getQuitInfoListByEmployeeIds(pendingLeaveEmployeeIds),
                        HrmEmployeeQuitInfoDO::getEmployeeId);
        for (HrmEmployeeDO employee : employees) {
            Integer entryStatus = employee.getEntryStatus();
            if (!HrmEmployeeEntryStatusEnum.LEFT.getStatus().equals(entryStatus)) {
                addBirthdayItems(items, employee, startDate, endDate);
            }
            if (HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus().equals(entryStatus)) {
                addEmployeeDateItem(items, HrmHomeCalendarItemTypeEnum.ENTRY,
                        employee, employee.getEntryTime(), startDate, endDate);
            }
            if (HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(entryStatus)) {
                addEmployeeDateItem(items, HrmHomeCalendarItemTypeEnum.REGULAR,
                        employee, employee.getRegularTime(), startDate, endDate);
            }
            HrmEmployeeQuitInfoDO quitInfo = quitInfoMap.get(employee.getId());
            if (quitInfo != null) {
                addEmployeeDateItem(items, HrmHomeCalendarItemTypeEnum.LEAVE,
                        employee, quitInfo.getPlanQuitTime(), startDate, endDate);
            }
        }
    }

    private void addBirthdayItems(
            List<HrmHomeCalendarItemRespVO> items, HrmEmployeeDO employee,
            LocalDate startDate, LocalDate endDate) {
        if (employee.getBirthday() == null) {
            return;
        }
        LocalDate birthDate = employee.getBirthday().toLocalDate();
        MonthDay employeeBirthday = MonthDay.from(birthDate);
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            if (!employeeBirthday.isValidYear(year)) {
                continue;
            }
            LocalDate birthdayDate = employeeBirthday.atYear(year);
            if (birthdayDate.isBefore(birthDate)
                    || birthdayDate.isBefore(startDate) || birthdayDate.isAfter(endDate)) {
                continue;
            }
            String content = employee.getName()
                    + LocalDateTimeUtils.getYearsBetween(birthDate, birthdayDate) + "岁生日";
            items.add(buildCalendarItem(HrmHomeCalendarItemTypeEnum.BIRTHDAY, content,
                    employee.getId(), birthdayDate, birthdayDate.atStartOfDay()));
        }
    }

    private void addEmployeeDateItem(
            List<HrmHomeCalendarItemRespVO> items, HrmHomeCalendarItemTypeEnum type,
            HrmEmployeeDO employee, LocalDateTime eventTime,
            LocalDate startDate, LocalDate endDate) {
        if (eventTime == null || eventTime.toLocalDate().isBefore(startDate)
                || eventTime.toLocalDate().isAfter(endDate)) {
            return;
        }
        items.add(buildCalendarItem(type, employee.getName() + type.getName() + "日",
                employee.getId(), eventTime.toLocalDate(), eventTime));
    }

    private void addRecruitItems(
            List<HrmHomeCalendarItemRespVO> items, LocalDate startDate, LocalDate endDate) {
        List<HrmRecruitInterviewDO> interviews =
                recruitInterviewService.getRecruitInterviewListByInterviewTimeBetween(
                        LocalDateTimeUtils.getDateTimeRange(startDate, endDate));
        Map<Long, HrmRecruitCandidateDO> candidateMap = recruitCandidateService.getRecruitCandidateMap(
                convertSet(interviews, HrmRecruitInterviewDO::getCandidateId));

        // 根据候选人当前状态过滤已结束或已取消展示的历史面试
        for (HrmRecruitInterviewDO interview : interviews) {
            HrmRecruitCandidateDO candidate = candidateMap.get(interview.getCandidateId());
            if (candidate == null
                    || !HrmRecruitCandidateStatusEnum.CALENDAR_STATUSES.contains(candidate.getStatus())
                    || HrmRecruitInterviewResultEnum.CANCEL.getResult().equals(interview.getResult())
                    || !Objects.equals(candidate.getStageNumber(), interview.getStageNumber())) {
                continue;
            }
            LocalDateTime interviewTime = interview.getInterviewTime();
            String period = interviewTime.getHour() < 12 ? "上午" : "下午";
            String content = candidate.getName() + period + interviewTime.format(INTERVIEW_TIME_FORMATTER) + "面试";
            items.add(buildCalendarItem(HrmHomeCalendarItemTypeEnum.RECRUIT, content, candidate.getId(), interviewTime.toLocalDate(), interviewTime));
        }
    }

    private void addAttendanceItems(
            List<HrmHomeCalendarItemRespVO> items, HrmEmployeeDO loginEmployee,
            LocalDate startDate, LocalDate endDate) {
        if (loginEmployee == null) {
            return;
        }
        List<HrmAttendanceClockDO> attendanceClocks = attendanceClockService
                .getAttendanceClockListByEmployeeIdAndClockTime(loginEmployee.getId(),
                        LocalDateTimeUtils.getDateTimeRange(startDate, endDate));
        Set<LocalDate> attendanceDates = convertSetBySupplier(attendanceClocks,
                clock -> clock.getClockTime() == null ? null : clock.getClockTime().toLocalDate(), TreeSet::new);
        // 按去重且有序的打卡日期生成考勤事项
        attendanceDates.forEach(date -> items.add(buildCalendarItem(
                HrmHomeCalendarItemTypeEnum.ATTENDANCE, "考勤打卡", loginEmployee.getId(), date, date.atStartOfDay())));
    }

    private HrmHomeCalendarItemRespVO buildPersonalNoteItem(
            HrmEmployeePersonalNoteDO personalNote) {
        return buildCalendarItem(
                HrmHomeCalendarItemTypeEnum.NOTE, personalNote.getContent(), null,
                personalNote.getReminderTime().toLocalDate(), personalNote.getReminderTime())
                .setPersonalNoteId(personalNote.getId());
    }

    private HrmHomeCalendarItemRespVO buildCalendarItem(
            HrmHomeCalendarItemTypeEnum type, String content, Long typeId,
            LocalDate date, LocalDateTime eventTime) {
        return new HrmHomeCalendarItemRespVO().setType(type.getType()).setTypeName(type.getName())
                .setContent(content).setTypeId(typeId).setDate(date).setEventTime(eventTime);
    }

    /**
     * 按事项时间、类型和内容稳定排序
     *
     * @param items 日历事项
     */
    private void sortCalendarItems(List<HrmHomeCalendarItemRespVO> items) {
        items.sort(Comparator
                .comparing(HrmHomeCalendarItemRespVO::getEventTime,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(HrmHomeCalendarItemRespVO::getType)
                .thenComparing(HrmHomeCalendarItemRespVO::getContent));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)
                || ChronoUnit.DAYS.between(startDate, endDate) > 370) {
            throw exception(HOME_CALENDAR_DATE_RANGE_ILLEGAL);
        }
    }

}
