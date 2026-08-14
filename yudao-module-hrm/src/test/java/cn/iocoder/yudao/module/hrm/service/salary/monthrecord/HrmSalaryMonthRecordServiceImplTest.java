package cn.iocoder.yudao.module.hrm.service.salary.monthrecord;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryPayrollReadinessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.monthrecord.HrmSalaryMonthRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.service.attendance.statistics.HrmAttendanceStatisticsService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthRecordService;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryConfigService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
import cn.iocoder.yudao.module.hrm.service.salary.employee.HrmSalaryEmployeeInfoService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_CANNOT_DELETE_ONLY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_NOT_LATEST;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalaryMonthRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryMonthRecordServiceImpl.class)
public class HrmSalaryMonthRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryMonthRecordServiceImpl monthRecordService;

    @Resource
    private HrmSalaryMonthRecordMapper monthRecordMapper;

    @MockBean
    private HrmSalaryOptionService salaryOptionService;
    @MockBean
    private HrmSalaryConfigService salaryConfigService;
    @MockBean
    private HrmSalaryEmployeeInfoService salaryEmployeeInfoService;
    @MockBean
    private HrmSalaryGroupService salaryGroupService;
    @MockBean
    private HrmSalaryTaxRuleService salaryTaxRuleService;
    @MockBean
    private HrmSalaryMonthEmployeeRecordService monthEmployeeRecordService;
    @MockBean
    private HrmEmployeeService employeeService;
    @MockBean
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @MockBean
    private HrmAttendanceStatisticsService attendanceStatisticsService;
    @MockBean
    private HrmInsuranceMonthEmployeeRecordService insuranceMonthEmployeeRecordService;
    @MockBean
    private HrmInsuranceMonthRecordService insuranceMonthRecordService;

    @Test
    public void testCreateMonthRecord_success() {
        // 准备参数
        HrmSalaryMonthRecordCreateReqVO reqVO =
                new HrmSalaryMonthRecordCreateReqVO().setYear(2026).setMonth(7);
        when(salaryOptionService.getSalaryOptionList(false, true))
                .thenReturn(Collections.emptyList());

        // 调用
        Long monthRecordId = monthRecordService.createMonthRecord(reqVO);

        // 断言
        assertNotNull(monthRecordId);
        HrmSalaryMonthRecordDO monthRecord = monthRecordMapper.selectById(monthRecordId);
        assertEquals("2026年7月工资表", monthRecord.getTitle());
        assertEquals(LocalDate.of(2026, 7, 1).atStartOfDay(), monthRecord.getStartTime());
        assertEquals(LocalDate.of(2026, 7, 31).atStartOfDay(), monthRecord.getEndTime());
        assertEquals(HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus(), monthRecord.getStatus());
        assertEquals(0, monthRecord.getEmployeeCount());
    }

    @Test
    public void testCreateMonthRecord_salaryConfigCycle() {
        // mock 数据
        when(salaryConfigService.getSalaryConfig()).thenReturn(
                HrmSalaryConfigDO.builder().cycleStartDay(26).cycleEndDay(25).build());
        when(salaryOptionService.getSalaryOptionList(false, true))
                .thenReturn(Collections.emptyList());
        // 准备参数
        HrmSalaryMonthRecordCreateReqVO reqVO =
                new HrmSalaryMonthRecordCreateReqVO().setYear(2026).setMonth(7);

        // 调用
        Long monthRecordId = monthRecordService.createMonthRecord(reqVO);

        // 断言
        HrmSalaryMonthRecordDO monthRecord = monthRecordMapper.selectById(monthRecordId);
        assertEquals(LocalDate.of(2026, 7, 26).atStartOfDay(), monthRecord.getStartTime());
        assertEquals(LocalDate.of(2026, 8, 25).atStartOfDay(), monthRecord.getEndTime());
    }

    @Test
    public void testCreateMonthRecord_exists() {
        // mock 数据
        monthRecordMapper.insert(buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus()));
        // 准备参数
        HrmSalaryMonthRecordCreateReqVO reqVO =
                new HrmSalaryMonthRecordCreateReqVO().setYear(2026).setMonth(7);

        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.createMonthRecord(reqVO),
                SALARY_MONTH_RECORD_EXISTS);
    }

    @Test
    public void testCreateMonthRecord_insertAfterDeletedRecord() {
        // mock 数据
        HrmSalaryMonthRecordDO deletedRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(deletedRecord);
        monthRecordMapper.deleteById(deletedRecord.getId());
        when(salaryOptionService.getSalaryOptionList(false, true))
                .thenReturn(Collections.emptyList());
        // 准备参数
        HrmSalaryMonthRecordCreateReqVO reqVO =
                new HrmSalaryMonthRecordCreateReqVO().setYear(2026).setMonth(7);

        // 调用
        Long monthRecordId = monthRecordService.createMonthRecord(reqVO);

        // 断言
        assertNotEquals(deletedRecord.getId(), monthRecordId);
        assertNull(monthRecordMapper.selectById(deletedRecord.getId()));
        assertNotNull(monthRecordMapper.selectById(monthRecordId));
    }

    @Test
    public void testCreateNextMonthRecord_firstMonth() {
        // mock 数据
        when(salaryConfigService.getSalaryConfig()).thenReturn(
                HrmSalaryConfigDO.builder().startYear(2026).startMonth(7).build());
        when(salaryOptionService.getSalaryOptionList(false, true))
                .thenReturn(Collections.emptyList());

        // 调用
        Long monthRecordId = monthRecordService.createNextMonthRecord();

        // 断言
        HrmSalaryMonthRecordDO monthRecord = monthRecordMapper.selectById(monthRecordId);
        assertEquals(2026, monthRecord.getYear());
        assertEquals(7, monthRecord.getMonth());
    }

    @Test
    public void testCreateNextMonthRecord_salaryConfigNotExists() {
        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.createNextMonthRecord(),
                SALARY_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testCreateNextMonthRecord_archivePrevious() {
        // mock 数据
        HrmSalaryMonthRecordDO previousMonthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus());
        monthRecordMapper.insert(previousMonthRecord);
        when(salaryOptionService.getSalaryOptionList(false, true))
                .thenReturn(Collections.emptyList());

        // 调用
        Long monthRecordId = monthRecordService.createNextMonthRecord();

        // 断言
        assertEquals(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus(),
                monthRecordMapper.selectById(previousMonthRecord.getId()).getStatus());
        HrmSalaryMonthRecordDO monthRecord = monthRecordMapper.selectById(monthRecordId);
        assertEquals(2026, monthRecord.getYear());
        assertEquals(8, monthRecord.getMonth());
    }

    @Test
    public void testComputeMonthRecord_success() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(monthRecord);
        HrmEmployeeDO employee = buildActiveEmployee(1L, "NO-001");
        when(employeeService.getEmployeeList(any())).thenReturn(
                Collections.singletonList(employee));
        HrmSalaryGroupDO salaryGroup = HrmSalaryGroupDO.builder()
                .id(10L).taxRuleId(20L).salaryStandard(new BigDecimal("21.75")).build();
        when(salaryGroupService.getEmployeeSalaryGroupMap(any()))
                .thenReturn(Collections.singletonMap(employee.getId(), salaryGroup));
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder().id(20L).name("默认计税").build();
        when(salaryTaxRuleService.getSalaryTaxRuleMap(anyCollection()))
                .thenReturn(Collections.singletonMap(taxRule.getId(), taxRule));
        when(salaryTaxRuleService.getSalaryTaxRuleList())
                .thenReturn(Collections.singletonList(taxRule));
        HrmSalaryOptionDO basicSalaryOption = new HrmSalaryOptionDO()
                .setCode(10101).setName("基本工资").setParentCode(10)
                .setType(1).setCalculateEnabled(true).setEnabled(true);
        when(salaryOptionService.getSalaryOptionList(false))
                .thenReturn(Collections.singletonList(basicSalaryOption));
        when(salaryOptionService.getSalaryOptionList(false, true))
                .thenReturn(Collections.singletonList(basicSalaryOption));
        when(salaryEmployeeInfoService.getEffectiveSalaryOptionList(
                eq(employee), any(LocalDateTime[].class)))
                .thenReturn(Collections.singletonList(
                        HrmSalaryEmployeeInfoDO.SalaryOption.builder()
                                .code(10101).name("基本工资")
                                .value(new BigDecimal("10000")).build()));
        when(monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                any(), any(), any())).thenAnswer(invocation -> {
            List<HrmSalaryMonthEmployeeRecordDO> records = invocation.getArgument(0);
            records.forEach(record -> record
                    .setExpectedPaySalary(new BigDecimal("10000"))
                    .setPersonalTax(new BigDecimal("500"))
                    .setRealPaySalary(new BigDecimal("9500"))
                    .setOptionValues(Collections.emptyList()));
            return records;
        });
        when(monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(
                monthRecord.getId())).thenAnswer(invocation -> Collections.singletonList(
                HrmSalaryMonthEmployeeRecordDO.builder()
                        .monthRecordId(monthRecord.getId()).employeeId(employee.getId())
                        .expectedPaySalary(new BigDecimal("10000"))
                        .personalTax(new BigDecimal("500"))
                        .realPaySalary(new BigDecimal("9500"))
                        .optionValues(Collections.emptyList()).build()));
        when(monthEmployeeRecordService.getMonthOptionSummary(any()))
                .thenReturn(Collections.emptyList());

        // 调用
        monthRecordService.computeMonthRecord(monthRecord.getId());

        // 断言
        ArgumentCaptor<LocalDateTime[]> salaryTimesCaptor = ArgumentCaptor.forClass(LocalDateTime[].class);
        verify(salaryEmployeeInfoService).getEffectiveSalaryOptionList(eq(employee), salaryTimesCaptor.capture());
        LocalDateTime[] salaryTimes = salaryTimesCaptor.getValue();
        assertTrue(LocalDateTimeUtils.isBetween(salaryTimes[0], salaryTimes[1],
                monthRecord.getEndTime().toLocalDate().atTime(12, 0)));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<HrmSalaryMonthEmployeeRecordDO>> recordCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(monthEmployeeRecordService)
                .saveMonthEmployeeRecordList(eq(monthRecord.getId()), recordCaptor.capture());
        assertEquals(1, recordCaptor.getValue().size());
        HrmSalaryMonthRecordDO result = monthRecordMapper.selectById(monthRecord.getId());
        assertEquals(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus(), result.getStatus());
        assertEquals(1, result.getEmployeeCount());
        assertAmount("10000.00", result.getExpectedPaySalary());
        assertAmount("500.00", result.getPersonalTax());
        assertAmount("9500.00", result.getRealPaySalary());
    }

    @Test
    public void testComputeMonthRecord_syncAttendanceData() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(monthRecord);
        HrmEmployeeDO employee = buildActiveEmployee(1L, "NO-001");
        when(employeeService.getEmployeeList(any())).thenReturn(Collections.singletonList(employee));
        HrmSalaryGroupDO salaryGroup = HrmSalaryGroupDO.builder()
                .id(10L).taxRuleId(20L).salaryStandard(new BigDecimal("21.75")).build();
        when(salaryGroupService.getEmployeeSalaryGroupMap(any()))
                .thenReturn(Collections.singletonMap(employee.getId(), salaryGroup));
        when(salaryTaxRuleService.getSalaryTaxRuleMap(anyCollection())).thenReturn(Collections.emptyMap());
        when(salaryTaxRuleService.getSalaryTaxRuleList()).thenReturn(Collections.emptyList());
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Collections.emptyList());
        when(salaryOptionService.getSalaryOptionList(false, true)).thenReturn(Collections.emptyList());
        when(salaryEmployeeInfoService.getEffectiveSalaryOptionList(
                eq(employee), any(LocalDateTime[].class))).thenReturn(Collections.emptyList());
        HrmAttendanceMonthRecordRespVO attendance = new HrmAttendanceMonthRecordRespVO()
                .setEmployeeId(employee.getId()).setActualDays(new BigDecimal("20.50"))
                .setLateDeductAmount(new BigDecimal("10.00"))
                .setEarlyDeductAmount(new BigDecimal("20.00"))
                .setAbsenteeismDeductAmount(new BigDecimal("30.00"))
                .setMisscardDeductAmount(new BigDecimal("40.00"))
                .setAttendanceDeductAmount(new BigDecimal("100.00"));
        when(attendanceStatisticsService.getAttendanceMonthRecordMap(
                2026, 7, Collections.singletonList(employee.getId())))
                .thenReturn(Collections.singletonMap(employee.getId(), attendance));
        when(monthEmployeeRecordService.calculateMonthEmployeeRecordList(any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(monthRecord.getId()))
                .thenReturn(Collections.emptyList());
        when(monthEmployeeRecordService.getMonthOptionSummary(any())).thenReturn(Collections.emptyList());

        // 调用
        monthRecordService.computeMonthRecord(monthRecord.getId(), false, true,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        // 断言
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HrmSalaryMonthEmployeeRecordDO>> employeeRecordsCaptor =
                ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, List<HrmSalaryOptionValueVO>>> optionValueMapCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(monthEmployeeRecordService).calculateMonthEmployeeRecordList(
                employeeRecordsCaptor.capture(), optionValueMapCaptor.capture(), any());
        assertAmount("20.50", CollUtil.getFirst(employeeRecordsCaptor.getValue()).getActualWorkDay());
        List<HrmSalaryOptionValueVO> optionValues = optionValueMapCaptor.getValue().get(employee.getId());
        assertOptionAmount("10.00", optionValues, HrmSalaryOptionCodeEnum.LATE_DEDUCTION);
        assertOptionAmount("20.00", optionValues, HrmSalaryOptionCodeEnum.EARLY_DEDUCTION);
        assertOptionAmount("30.00", optionValues, HrmSalaryOptionCodeEnum.ABSENTEEISM_DEDUCTION);
        assertOptionAmount("0.00", optionValues, HrmSalaryOptionCodeEnum.LEAVE_DEDUCTION);
        assertOptionAmount("40.00", optionValues, HrmSalaryOptionCodeEnum.MISSING_CARD_DEDUCTION);
        assertOptionAmount("100.00", optionValues, HrmSalaryOptionCodeEnum.ATTENDANCE_DEDUCTION_TOTAL);
    }

    @Test
    public void testGetPayrollEmployeeList() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        HrmEmployeeDO activeEmployee = buildActiveEmployee(3L, "NO-003");
        HrmEmployeeDO pendingEntryEmployee = buildActiveEmployee(1L, "NO-001")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());
        HrmEmployeeDO leftBeforeCycleEmployee = buildActiveEmployee(2L, "NO-002")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setLeaveTime(monthRecord.getStartTime().minusDays(1));
        HrmEmployeeDO leftInCycleEmployee = buildActiveEmployee(4L, "NO-004")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setLeaveTime(monthRecord.getStartTime());
        when(employeeService.getEmployeeList(any())).thenReturn(Arrays.asList(
                activeEmployee, pendingEntryEmployee, leftBeforeCycleEmployee, leftInCycleEmployee));

        // 调用
        List<HrmEmployeeDO> employees =
                monthRecordService.getPayrollEmployeeList(monthRecord);

        // 断言
        assertEquals(Arrays.asList(activeEmployee.getId(), leftInCycleEmployee.getId()),
                Arrays.asList(employees.get(0).getId(), employees.get(1).getId()));
    }

    @Test
    public void testDeleteMonthRecord_success() {
        // mock 数据
        HrmSalaryMonthRecordDO previousMonthRecord =
                buildMonthRecord(2026, 6, HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus());
        monthRecordMapper.insert(previousMonthRecord);
        HrmSalaryMonthRecordDO currentMonthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(currentMonthRecord);

        // 调用
        monthRecordService.deleteMonthRecord(currentMonthRecord.getId());

        // 断言
        assertNull(monthRecordMapper.selectById(currentMonthRecord.getId()));
        assertEquals(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus(),
                monthRecordMapper.selectById(previousMonthRecord.getId()).getStatus());
        verify(monthEmployeeRecordService)
                .deleteMonthEmployeeRecordListByMonthRecordId(currentMonthRecord.getId());
    }

    @Test
    public void testDeleteMonthRecord_notLatest() {
        // mock 数据
        HrmSalaryMonthRecordDO previousMonthRecord =
                buildMonthRecord(2026, 6, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(previousMonthRecord);
        monthRecordMapper.insert(buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.deleteMonthRecord(
                previousMonthRecord.getId()), SALARY_MONTH_RECORD_NOT_LATEST);
    }

    @Test
    public void testDeleteMonthRecord_onlyOne() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(monthRecord);

        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.deleteMonthRecord(
                monthRecord.getId()), SALARY_MONTH_RECORD_CANNOT_DELETE_ONLY);
    }

    @Test
    public void testGetPayrollReadiness() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus());
        monthRecordMapper.insert(monthRecord);
        HrmEmployeeDO salaryMissingEmployee = buildActiveEmployee(1L, "NO-001");
        HrmEmployeeDO groupMissingEmployee = buildActiveEmployee(2L, "NO-002");
        List<HrmEmployeeDO> employees =
                Arrays.asList(salaryMissingEmployee, groupMissingEmployee);
        when(employeeService.getEmployeeList(any())).thenReturn(employees);
        when(salaryGroupService.getEmployeeSalaryGroupMap(employees)).thenReturn(
                Collections.singletonMap(salaryMissingEmployee.getId(),
                        HrmSalaryGroupDO.builder().id(10L).taxRuleId(20L).build()));
        when(salaryEmployeeInfoService.getSalaryEmployeeInfoMap(
                Collections.singleton(salaryMissingEmployee.getId())))
                .thenReturn(Collections.emptyMap());
        HrmEmployeeChangeRecordDO changeRecord = HrmEmployeeChangeRecordDO.builder()
                .employeeId(salaryMissingEmployee.getId())
                .type(HrmEmployeeChangeTypeEnum.TRANSFER.getType()).build();
        when(employeeChangeRecordService.getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                anyCollection(), any(LocalDateTime[].class)))
                .thenReturn(Collections.singletonList(changeRecord));

        // 调用
        HrmSalaryPayrollReadinessRespVO result =
                monthRecordService.getPayrollReadiness(monthRecord.getId());

        // 断言
        assertEquals(1L, result.getPayrollEmployeeCount());
        assertEquals(0L, result.getSalaryEmployeeCount());
        assertEquals(1L, result.getNoSalaryEmployeeCount());
        assertEquals(1L, result.getNoSalaryGroupEmployeeCount());
        assertEquals(1L, result.getChangeEmployeeCount());
        ArgumentCaptor<LocalDateTime[]> effectTimesCaptor = ArgumentCaptor.forClass(LocalDateTime[].class);
        verify(employeeChangeRecordService).getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                anyCollection(), effectTimesCaptor.capture());
        LocalDateTime[] effectTimes = effectTimesCaptor.getValue();
        assertTrue(LocalDateTimeUtils.isBetween(effectTimes[0], effectTimes[1],
                monthRecord.getEndTime().toLocalDate().atTime(12, 0)));
    }

    @Test
    public void testGetPayrollReadiness_monthRecordNotExists() {
        // 准备参数
        Long monthRecordId = randomLongId();

        // 调用，并断言
        assertNull(monthRecordService.getPayrollReadiness(monthRecordId));
    }

    @Test
    public void testValidateMonthRecordEditable_history() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus());
        monthRecordMapper.insert(monthRecord);

        // 调用，并断言异常
        assertServiceException(() -> monthRecordService.validateMonthRecordEditable(
                monthRecord.getId()), SALARY_MONTH_RECORD_STATUS_INVALID);
    }

    @Test
    public void testGetMonthRecordPage() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus());
        monthRecordMapper.insert(monthRecord);
        // 测试 status 不匹配
        monthRecordMapper.insert(buildMonthRecord(2026, 8, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus()));
        // 准备参数
        HrmSalaryMonthRecordPageReqVO reqVO = new HrmSalaryMonthRecordPageReqVO();
        reqVO.setStatus(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus());

        // 调用
        PageResult<HrmSalaryMonthRecordDO> result =
                monthRecordService.getMonthRecordPage(reqVO);

        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(monthRecord.getId(), result.getList().get(0).getId());
    }

    @Test
    public void testGetMonthRecordListByStatus() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord =
                buildMonthRecord(2026, 7, HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus());
        monthRecordMapper.insert(monthRecord);
        // 测试 status 不匹配
        monthRecordMapper.insert(buildMonthRecord(2026, 8, HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus()));

        // 调用
        List<HrmSalaryMonthRecordDO> result =
                monthRecordService.getMonthRecordListByStatus(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus());

        // 断言
        assertEquals(1, result.size());
        assertEquals(monthRecord.getId(), result.get(0).getId());
    }

    // ========== 随机对象 ==========

    private static HrmSalaryMonthRecordDO buildMonthRecord(
            Integer year, Integer month, Integer status) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return HrmSalaryMonthRecordDO.builder()
                .title(year + "年" + month + "月工资表")
                .year(year).month(month).employeeCount(0)
                .startTime(yearMonth.atDay(1).atStartOfDay())
                .endTime(yearMonth.atEndOfMonth().atStartOfDay())
                .expectedPaySalary(BigDecimal.ZERO)
                .personalTax(BigDecimal.ZERO).realPaySalary(BigDecimal.ZERO)
                .optionHeaders(Collections.emptyList()).status(status).build();
    }

    private static HrmEmployeeDO buildActiveEmployee(Long id, String jobNumber) {
        return HrmEmployeeDO.builder().id(id).name("员工" + id).jobNumber(jobNumber)
                .entryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .status(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .entryTime(LocalDate.of(2026, 1, 1).atStartOfDay()).build();
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static void assertOptionAmount(
            String expected, List<HrmSalaryOptionValueVO> optionValues, HrmSalaryOptionCodeEnum optionCode) {
        HrmSalaryOptionValueVO optionValue = CollUtil.findOne(optionValues,
                value -> optionCode.getCode().equals(value.getCode()));
        assertNotNull(optionValue);
        assertAmount(expected, optionValue.getValue());
    }

}
