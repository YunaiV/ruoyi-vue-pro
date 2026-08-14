package cn.iocoder.yudao.module.hrm.service.home;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHomeCalendarItemRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO;
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
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.home.HrmHomeCalendarItemTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmHomeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class HrmHomeServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private HrmHomeServiceImpl homeService;

    @Mock
    private HrmEmployeeContractService employeeContractService;
    @Mock
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @Mock
    private HrmEmployeeQuitInfoService employeeQuitInfoService;
    @Mock
    private HrmEmployeeService employeeService;
    @Mock
    private HrmEmployeePersonalNoteService personalNoteService;
    @Mock
    private HrmRecruitPostService recruitPostService;
    @Mock
    private HrmRecruitCandidateService recruitCandidateService;
    @Mock
    private HrmRecruitInterviewService recruitInterviewService;
    @Mock
    private HrmAttendanceClockService attendanceClockService;
    @Mock
    private HrmSalaryMonthRecordService salaryMonthRecordService;
    @Mock
    private HrmSalaryMonthEmployeeRecordService salaryMonthEmployeeRecordService;
    @Mock
    private DeptApi deptApi;

    @Test
    public void testGetTeamHomeStatisticsSummary() {
        // mock 数据
        LocalDate today = LocalDate.now();
        HrmEmployeeDO leader = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("主管");
        HrmEmployeeDO activeEmployee = new HrmEmployeeDO().setId(2L).setName("张三")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus()).setSex(1)
                .setBirthday(today.minusYears(30).atStartOfDay())
                .setCompanyAgeStartTime(today.minusYears(2).atStartOfDay())
                .setEntryTime(today.atTime(9, 0));
        HrmEmployeeDO pendingLeaveEmployee = new HrmEmployeeDO().setId(3L).setName("李四")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus()).setSex(2)
                .setBirthday(today.minusYears(24).atStartOfDay())
                .setCompanyAgeStartTime(today.minusMonths(4).atStartOfDay())
                .setEntryTime(today.minusMonths(4).atStartOfDay());
        HrmEmployeeDO leftEmployee = new HrmEmployeeDO().setId(4L).setName("王五")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setEntryTime(today.minusYears(3).atStartOfDay()).setLeaveTime(today.atTime(18, 0));
        HrmEmployeeDO pendingEntryEmployee = new HrmEmployeeDO().setId(5L).setName("赵六")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus())
                .setEntryTime(today.atTime(9, 0));
        List<HrmEmployeeDO> teamEmployees = Arrays.asList(
                activeEmployee, pendingLeaveEmployee, leftEmployee, pendingEntryEmployee);
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(leader);
        when(employeeService.getEmployeeListByLeaderEmployeeId(leader.getId())).thenReturn(teamEmployees);
        when(employeeChangeRecordService.getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                anyCollection(), any())).thenReturn(Collections.singletonList(
                new HrmEmployeeChangeRecordDO().setEmployeeId(activeEmployee.getId())
                        .setType(HrmEmployeeChangeTypeEnum.REGULAR.getType())
                        .setEffectTime(today.atTime(10, 0))));

        // 调用
        HrmTeamHomeStatisticsRespVO summary = homeService.getTeamHomeStatisticsSummary(100L);

        // 断言
        assertEquals(leader.getId(), summary.getLeaderEmployeeId());
        assertEquals(2L, summary.getTeamOverview().getEmployeeCount());
        assertEquals(1L, summary.getTeamOverview().getEntryThisMonthCount());
        assertEquals(1L, summary.getTeamOverview().getLeaveThisMonthCount());
        assertEquals(1L, summary.getTeamOverview().getRegularThisMonthCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getStatusAnalysis(),
                item -> HrmEmployeeStatusEnum.REGULAR.getStatus().equals(item.getType())).getCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getStatusAnalysis(),
                item -> HrmEmployeeStatusEnum.PROBATION.getStatus().equals(item.getType())).getCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getSexAnalysis(),
                item -> SexEnum.MALE.getSex().equals(item.getType())).getCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getSexAnalysis(),
                item -> SexEnum.FEMALE.getSex().equals(item.getType())).getCount());
    }

    @Test
    public void testGetTeamHomeStatisticsSummary_ageNotFilled() {
        // mock 数据
        HrmEmployeeDO leader = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("主管");
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(2L).setName("未填写年龄的员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(leader);
        when(employeeService.getEmployeeListByLeaderEmployeeId(leader.getId()))
                .thenReturn(Collections.singletonList(employee));
        when(employeeChangeRecordService.getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                anyCollection(), any())).thenReturn(Collections.emptyList());

        // 调用
        HrmTeamHomeStatisticsRespVO summary = homeService.getTeamHomeStatisticsSummary(100L);

        // 断言
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getStatusAnalysis(),
                item -> item.getType() == null).getCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getSexAnalysis(),
                item -> item.getType() == null).getCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getAgeAnalysis(),
                item -> item.getType() == null).getCount());
        assertEquals(1L, CollUtil.findOne(summary.getTeamSurvey().getCompanyAgeAnalysis(),
                item -> item.getType() == null).getCount());
    }

    @Test
    public void testGetTeamHomeCalendar() {
        // mock 数据
        LocalDate date = LocalDate.of(2026, 7, 30);
        HrmEmployeeDO leader = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("主管");
        HrmEmployeeDO teamEmployee = new HrmEmployeeDO().setId(2L).setName("张三")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setBirthday(date.minusYears(28).atStartOfDay()).setAge(28)
                .setEntryTime(date.atTime(9, 0));
        HrmEmployeePersonalNoteDO personalNote = HrmEmployeePersonalNoteDO.builder()
                .id(10L).employeeId(leader.getId()).content("团队周会")
                .reminderTime(date.atTime(8, 30)).build();
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(leader);
        when(employeeService.getEmployeeListByLeaderEmployeeId(leader.getId()))
                .thenReturn(Collections.singletonList(teamEmployee));
        when(personalNoteService.getPersonalNoteList(
                eq(leader.getId()), aryEq(new LocalDateTime[]{
                        date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Collections.singletonList(personalNote));

        // 调用
        List<HrmHomeCalendarItemRespVO> items = homeService.getTeamHomeCalendar(100L, date, date);

        // 断言
        assertEquals(3, items.size());
        assertTrue(items.stream().anyMatch(item -> personalNote.getId().equals(item.getPersonalNoteId())));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 2
                && teamEmployee.getId().equals(item.getTypeId())));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 3
                && teamEmployee.getId().equals(item.getTypeId())));
    }

    @Test
    public void testGetHrHomeStatisticsSummary() {
        // mock 员工和待办数据
        when(employeeService.getEmployeeStatusCount(any())).thenReturn(Collections.singletonMap(
                HrmEmployeeStatusTabEnum.ACTIVE.getStatus(), 2L));
        Map<Integer, Long> employeeSurveyCountMap = new HashMap<>();
        employeeSurveyCountMap.put(HrmEmployeeSurveyTypeEnum.ENTRY.getType(), 1L);
        employeeSurveyCountMap.put(HrmEmployeeSurveyTypeEnum.PENDING_ENTRY.getType(), 1L);
        employeeSurveyCountMap.put(HrmEmployeeSurveyTypeEnum.LEAVE.getType(), 1L);
        employeeSurveyCountMap.put(HrmEmployeeSurveyTypeEnum.PENDING_LEAVE.getType(), 1L);
        employeeSurveyCountMap.put(HrmEmployeeSurveyTypeEnum.REGULAR.getType(), 1L);
        when(employeeService.getEmployeeSurveyCountMap()).thenReturn(employeeSurveyCountMap);
        when(employeeContractService.getExpireEmployeeCountInMonth(any())).thenReturn(1L);

        // mock 招聘数据
        when(recruitPostService.getRecruitPostStatusCount(any())).thenReturn(Collections.singletonMap(
                HrmRecruitPostStatusEnum.RECRUITING.getStatus(), 1L));
        Map<Integer, Long> candidateStatusCount = new HashMap<>();
        candidateStatusCount.put(HrmRecruitCandidateStatusEnum.NEW.getStatus(), 1L);
        candidateStatusCount.put(HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus(), 1L);
        candidateStatusCount.put(HrmRecruitCandidateStatusEnum.JOINED.getStatus(), 1L);
        candidateStatusCount.put(HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus(), 1L);
        when(recruitCandidateService.getRecruitCandidateStatusCount(any())).thenReturn(candidateStatusCount);
        when(recruitCandidateService.getRecruitCandidateCountByStatusAndStatusUpdateTimeBetween(
                eq(HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus()), any())).thenReturn(1L);
        when(recruitCandidateService.getRecruitCandidateCountByStatusAndEntryTimeBetween(
                eq(HrmRecruitCandidateStatusEnum.JOINED.getStatus()), any())).thenReturn(1L);

        // mock 薪资数据
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        HrmSalaryMonthRecordDO salaryMonthRecord = new HrmSalaryMonthRecordDO().setId(10L)
                .setYear(lastMonth.getYear()).setMonth(lastMonth.getMonthValue())
                .setStatus(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus());
        when(salaryMonthRecordService.getMonthRecordByYearMonth(
                lastMonth.getYear(), lastMonth.getMonthValue())).thenReturn(salaryMonthRecord);
        when(salaryMonthRecordService.getMonthRecordListByStatus(
                HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus()))
                .thenReturn(Collections.singletonList(new HrmSalaryMonthRecordDO()));
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords = Arrays.asList(
                new HrmSalaryMonthEmployeeRecordDO().setEmployeeId(1L)
                        .setRealPaySalary(BigDecimal.valueOf(600)),
                new HrmSalaryMonthEmployeeRecordDO().setEmployeeId(2L)
                        .setRealPaySalary(BigDecimal.valueOf(300)));
        when(salaryMonthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(10L))
                .thenReturn(employeeRecords);
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(1L, new HrmEmployeeDO().setId(1L).setDeptId(10L));
        employeeMap.put(2L, new HrmEmployeeDO().setId(2L).setDeptId(20L));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(employeeMap);
        Map<Long, DeptRespDTO> deptMap = new HashMap<>();
        DeptRespDTO developmentDept = new DeptRespDTO();
        developmentDept.setId(10L);
        developmentDept.setName("研发部");
        deptMap.put(developmentDept.getId(), developmentDept);
        DeptRespDTO humanResourceDept = new DeptRespDTO();
        humanResourceDept.setId(20L);
        humanResourceDept.setName("人事部");
        deptMap.put(humanResourceDept.getId(), humanResourceDept);
        when(deptApi.getDeptMap(anyCollection())).thenReturn(deptMap);

        // 调用
        HrmHrHomeStatisticsRespVO summary = homeService.getHrHomeStatisticsSummary();

        // 断言
        assertEquals(2L, summary.getEmployeeSurvey().getActiveCount());
        assertEquals(1L, summary.getEmployeeSurvey().getEntryThisMonthCount());
        assertEquals(1L, summary.getEmployeeSurvey().getPendingEntryThisMonthCount());
        assertEquals(1L, summary.getEmployeeSurvey().getLeaveThisMonthCount());
        assertEquals(1L, summary.getEmployeeSurvey().getPendingLeaveThisMonthCount());
        assertEquals(1L, summary.getEmployeeSurvey().getRegularThisMonthCount());
        assertEquals(0L, summary.getEmployeeSurvey().getTransferThisMonthCount());

        assertEquals(1L, summary.getRecruitSurvey().getRecruitingPostCount());
        assertEquals(1L, summary.getRecruitSurvey().getCandidateInProcessCount());
        assertEquals(1L, summary.getRecruitSurvey().getPendingEntryCount());
        assertEquals(1L, summary.getRecruitSurvey().getJoinedCount());

        assertEquals(2, summary.getSalarySurvey().getEmployeeCount());
        assertEquals(0, BigDecimal.valueOf(900).compareTo(summary.getSalarySurvey().getRealPaySalary()));
        assertEquals(2, summary.getSalarySurvey().getDeptProportions().size());
        assertEquals("研发部", summary.getSalarySurvey().getDeptProportions().get(0).getDeptName());
        assertEquals(0, new BigDecimal("0.6667").compareTo(
                summary.getSalarySurvey().getDeptProportions().get(0).getProportion()));
        assertEquals("人事部", summary.getSalarySurvey().getDeptProportions().get(1).getDeptName());
        assertEquals(0, new BigDecimal("0.3334").compareTo(
                summary.getSalarySurvey().getDeptProportions().get(1).getProportion()));

        assertEquals(1L, summary.getTodoSurvey().getToEntryCount());
        assertEquals(1L, summary.getTodoSurvey().getToLeaveCount());
        assertEquals(1L, summary.getTodoSurvey().getToExpireContractCount());
        assertEquals(2L, summary.getTodoSurvey().getToRegularCount());
        assertEquals(1L, summary.getTodoSurvey().getToSalaryComputeCount());
        assertEquals(2L, summary.getTodoSurvey().getToBirthdayCount());
    }

    @Test
    public void testGetCalendar() {
        // mock 数据
        LocalDate date = LocalDate.of(2026, 7, 30);
        HrmEmployeeDO loginEmployee = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("张三");
        HrmEmployeeDO birthdayEmployee = new HrmEmployeeDO().setId(2L).setName("李四")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setBirthday(date.minusYears(28).atStartOfDay())
                .setAge(28).setRegularTime(date.atTime(10, 0));
        HrmEmployeeDO pendingEntryEmployee = new HrmEmployeeDO().setId(3L).setName("赵六")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setEntryTime(date.atTime(9, 0));
        HrmEmployeeDO pendingLeaveEmployee = new HrmEmployeeDO().setId(4L).setName("孙七")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setLeaveTime(date.minusDays(1).atTime(18, 0));
        HrmEmployeeDO leftEmployee = new HrmEmployeeDO().setId(5L).setName("周八")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setBirthday(date.minusYears(30).atStartOfDay()).setLeaveTime(date.atTime(18, 0));
        HrmEmployeeQuitInfoDO quitInfo = new HrmEmployeeQuitInfoDO()
                .setEmployeeId(pendingLeaveEmployee.getId()).setPlanQuitTime(date.atTime(17, 0));
        HrmEmployeePersonalNoteDO personalNote = HrmEmployeePersonalNoteDO.builder()
                .id(10L).employeeId(loginEmployee.getId()).content("准备员工月报")
                .reminderTime(date.atTime(8, 30)).build();
        HrmRecruitInterviewDO interview = new HrmRecruitInterviewDO().setCandidateId(20L)
                .setStageNumber(2).setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult())
                .setInterviewTime(date.atTime(14, 0));
        HrmRecruitInterviewDO cancelledInterview = new HrmRecruitInterviewDO().setCandidateId(20L)
                .setStageNumber(2).setResult(HrmRecruitInterviewResultEnum.CANCEL.getResult())
                .setInterviewTime(date.atTime(15, 0));
        HrmRecruitInterviewDO historyInterview = new HrmRecruitInterviewDO().setCandidateId(20L)
                .setStageNumber(1).setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult())
                .setInterviewTime(date.atTime(16, 0));
        HrmRecruitCandidateDO candidate = new HrmRecruitCandidateDO().setId(20L).setName("王五")
                .setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus()).setStageNumber(2);
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(loginEmployee);
        when(personalNoteService.getPersonalNoteList(
                eq(loginEmployee.getId()), aryEq(new LocalDateTime[]{
                        date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Collections.singletonList(personalNote));
        when(employeeService.getEmployeeList(any()))
                .thenReturn(Arrays.asList(loginEmployee, birthdayEmployee, pendingEntryEmployee,
                        pendingLeaveEmployee, leftEmployee));
        when(employeeQuitInfoService.getQuitInfoListByEmployeeIds(anyCollection()))
                .thenReturn(Collections.singletonList(quitInfo));
        when(recruitInterviewService.getRecruitInterviewListByInterviewTimeBetween(
                aryEq(new LocalDateTime[]{date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Arrays.asList(interview, cancelledInterview, historyInterview));
        when(recruitCandidateService.getRecruitCandidateMap(anyCollection()))
                .thenReturn(Collections.singletonMap(candidate.getId(), candidate));

        // 调用
        List<HrmHomeCalendarItemRespVO> items = homeService.getHrHomeCalendar(100L, date, date);

        // 断言
        assertEquals(6, items.size());
        assertTrue(items.stream().anyMatch(item -> item.getType() == 1
                && personalNote.getId().equals(item.getPersonalNoteId())));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 2
                && birthdayEmployee.getId().equals(item.getTypeId())));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 3));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 4));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 5));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 5
                && pendingLeaveEmployee.getId().equals(item.getTypeId())
                && quitInfo.getPlanQuitTime().equals(item.getEventTime())));
        assertTrue(items.stream().noneMatch(item -> leftEmployee.getId().equals(item.getTypeId())));
        assertTrue(items.stream().anyMatch(item -> item.getType() == 6
                && "王五下午14:00面试".equals(item.getContent())));
        assertEquals(1L, items.stream().filter(item -> item.getType() == 6).count());
    }

    @Test
    public void testGetCalendarRange() {
        // mock 数据
        LocalDate date = LocalDate.of(2026, 7, 30);
        HrmEmployeeDO loginEmployee = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("张三");
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(loginEmployee);
        when(personalNoteService.getPersonalNoteList(
                eq(loginEmployee.getId()), aryEq(new LocalDateTime[]{
                        date.atStartOfDay(), date.plusDays(1).atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Arrays.asList(
                        HrmEmployeePersonalNoteDO.builder().id(1L).employeeId(1L).content("事项一")
                                .reminderTime(date.atTime(9, 0)).build(),
                        HrmEmployeePersonalNoteDO.builder().id(2L).employeeId(1L).content("事项二")
                                .reminderTime(date.atTime(10, 0)).build()));
        when(employeeService.getEmployeeList(any())).thenReturn(Collections.emptyList());
        when(recruitInterviewService.getRecruitInterviewListByInterviewTimeBetween(
                aryEq(new LocalDateTime[]{
                        date.atStartOfDay(), date.plusDays(1).atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Collections.emptyList());
        when(recruitCandidateService.getRecruitCandidateMap(anyCollection()))
                .thenReturn(Collections.emptyMap());

        // 调用
        List<HrmHomeCalendarItemRespVO> items =
                homeService.getHrHomeCalendar(100L, date, date.plusDays(1));

        // 断言
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(item -> date.equals(item.getDate())));
    }

    @Test
    public void testGetCalendar_birthdayAcrossYear() {
        // mock 数据
        LocalDate startDate = LocalDate.of(2026, 12, 31);
        LocalDate endDate = LocalDate.of(2027, 1, 1);
        HrmEmployeeDO loginEmployee = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("张三");
        HrmEmployeeDO birthdayEmployee = new HrmEmployeeDO().setId(2L).setName("李四")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setAge(20)
                .setBirthday(LocalDate.of(1990, 1, 1).atStartOfDay());
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(loginEmployee);
        when(personalNoteService.getPersonalNoteList(eq(loginEmployee.getId()), any()))
                .thenReturn(Collections.emptyList());
        when(employeeService.getEmployeeList(any())).thenReturn(Collections.singletonList(birthdayEmployee));
        when(recruitInterviewService.getRecruitInterviewListByInterviewTimeBetween(any()))
                .thenReturn(Collections.emptyList());
        when(recruitCandidateService.getRecruitCandidateMap(anyCollection()))
                .thenReturn(Collections.emptyMap());

        // 调用
        List<HrmHomeCalendarItemRespVO> items =
                homeService.getHrHomeCalendar(100L, startDate, endDate);

        // 断言
        assertEquals(1, items.size());
        assertEquals(HrmHomeCalendarItemTypeEnum.BIRTHDAY.getType(), items.get(0).getType());
        assertEquals("李四37岁生日", items.get(0).getContent());
        assertEquals(endDate, items.get(0).getDate());
    }

    @Test
    public void testGetEmployeeCalendar() {
        // mock 数据
        LocalDate date = LocalDate.of(2026, 7, 30);
        LocalDateTime reminderTime = date.atTime(15, 30);
        HrmEmployeeDO loginEmployee = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("张三");
        HrmEmployeePersonalNoteDO personalNote = HrmEmployeePersonalNoteDO.builder()
                .id(10L).employeeId(loginEmployee.getId()).content("跟进转正材料")
                .reminderTime(reminderTime).build();
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(loginEmployee);
        when(personalNoteService.getPersonalNoteList(eq(loginEmployee.getId()),
                aryEq(new LocalDateTime[]{date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Collections.singletonList(personalNote));

        // 调用
        List<HrmHomeCalendarItemRespVO> items =
                homeService.getEmployeeCalendar(100L, date, date);

        // 断言
        assertTrue(items.stream().anyMatch(item -> personalNote.getId().equals(item.getPersonalNoteId())
                && item.getType() == 1 && reminderTime.equals(item.getEventTime())));
    }

    @Test
    public void testGetEmployeeCalendarAttendanceItem() {
        // mock 数据
        LocalDate date = LocalDate.of(2026, 7, 30);
        HrmEmployeeDO loginEmployee = new HrmEmployeeDO().setId(1L).setUserId(100L).setName("张三");
        when(employeeService.getEmployeeByUserId(100L)).thenReturn(loginEmployee);
        when(personalNoteService.getPersonalNoteList(eq(loginEmployee.getId()),
                aryEq(new LocalDateTime[]{date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Collections.emptyList());
        when(attendanceClockService.getAttendanceClockListByEmployeeIdAndClockTime(
                eq(loginEmployee.getId()), aryEq(new LocalDateTime[]{
                        date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)})))
                .thenReturn(Arrays.asList(
                        clock(loginEmployee.getId(), date.atTime(9, 0)),
                        clock(loginEmployee.getId(), date.atTime(18, 0))));

        // 调用
        List<HrmHomeCalendarItemRespVO> items =
                homeService.getEmployeeCalendar(100L, date, date);

        // 断言
        assertEquals(1, items.size());
        assertEquals(7, items.get(0).getType());
        assertEquals(date, items.get(0).getDate());
        verify(attendanceClockService).getAttendanceClockListByEmployeeIdAndClockTime(
                eq(loginEmployee.getId()), aryEq(new LocalDateTime[]{
                        date.atStartOfDay(), date.atTime(java.time.LocalTime.MAX)}));
    }

    private HrmAttendanceClockDO clock(Long employeeId, LocalDateTime clockTime) {
        HrmAttendanceClockDO clock = new HrmAttendanceClockDO();
        clock.setId(1L);
        clock.setEmployeeId(employeeId);
        clock.setClockTime(clockTime);
        clock.setType(1);
        clock.setAttendanceTime(clockTime);
        clock.setStatus(1);
        return clock;
    }

}
