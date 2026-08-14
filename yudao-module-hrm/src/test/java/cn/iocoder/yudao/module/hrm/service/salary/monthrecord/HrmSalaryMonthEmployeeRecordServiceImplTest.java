package cn.iocoder.yudao.module.hrm.service.salary.monthrecord;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryEmployeeMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryPerformanceCoefficientReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.monthrecord.HrmSalaryMonthEmployeeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentService;
import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalaryMonthEmployeeRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryMonthEmployeeRecordServiceImpl.class)
public class HrmSalaryMonthEmployeeRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryMonthEmployeeRecordServiceImpl monthEmployeeRecordService;

    @Resource
    private HrmSalaryMonthEmployeeRecordMapper monthEmployeeRecordMapper;

    @MockBean
    private HrmSalaryMonthRecordService monthRecordService;
    @MockBean
    private HrmEmployeeService employeeService;
    @MockBean
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @MockBean
    private HrmPerformanceAssessmentService performanceAssessmentService;
    @MockBean
    private HrmPerformancePlanService performancePlanService;
    @MockBean
    private HrmSalaryOptionService salaryOptionService;
    @MockBean
    private HrmSalaryGroupService salaryGroupService;
    @MockBean
    private HrmSalaryTaxRuleService salaryTaxRuleService;

    @Test
    public void testUpdateMonthEmployeeRecordList_success() {
        // mock 数据
        HrmSalaryMonthEmployeeRecordDO firstRecord = randomMonthEmployeeRecord(2001L, 1001L);
        HrmSalaryMonthEmployeeRecordDO secondRecord = randomMonthEmployeeRecord(2001L, 1002L);
        monthEmployeeRecordMapper.insert(firstRecord);
        monthEmployeeRecordMapper.insert(secondRecord);
        when(monthRecordService.validateMonthRecordEditableForUpdate(2001L))
                .thenReturn(new HrmSalaryMonthRecordDO().setId(2001L));
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Collections.singletonList(
                new HrmSalaryOptionDO().setCode(10101).setName("基本工资").setParentCode(10)));
        HrmSalaryOptionValueVO salaryOption = new HrmSalaryOptionValueVO()
                .setCode(10101).setName("基本工资").setValue(BigDecimal.valueOf(9000));
        // 准备参数
        HrmSalaryMonthEmployeeRecordUpdateReqVO firstReqVO = new HrmSalaryMonthEmployeeRecordUpdateReqVO();
        firstReqVO.setId(firstRecord.getId()).setOptionValues(Collections.singletonList(salaryOption));
        HrmSalaryMonthEmployeeRecordUpdateReqVO secondReqVO = new HrmSalaryMonthEmployeeRecordUpdateReqVO();
        secondReqVO.setId(secondRecord.getId()).setOptionValues(Collections.singletonList(salaryOption));

        // 调用
        monthEmployeeRecordService.updateMonthEmployeeRecordList(Arrays.asList(firstReqVO, secondReqVO));

        // 断言
        assertEquals(0, BigDecimal.valueOf(9000).compareTo(
                monthEmployeeRecordMapper.selectById(firstRecord.getId()).getExpectedPaySalary()));
        assertEquals(0, BigDecimal.valueOf(9000).compareTo(
                monthEmployeeRecordMapper.selectById(secondRecord.getId()).getExpectedPaySalary()));
        verify(monthRecordService).validateMonthRecordEditableForUpdate(2001L);
        verify(monthRecordService).updateMonthRecordSummary(2001L);
    }

    @Test
    public void testSaveMonthEmployeeRecordList_keepIdAndDeleteRemovedRecord() {
        // mock 数据
        HrmSalaryMonthEmployeeRecordDO retainedRecord = randomMonthEmployeeRecord(2001L, 1001L);
        HrmSalaryMonthEmployeeRecordDO removedRecord = randomMonthEmployeeRecord(2001L, 1002L);
        monthEmployeeRecordMapper.insert(retainedRecord);
        monthEmployeeRecordMapper.insert(removedRecord);
        HrmSalaryMonthEmployeeRecordDO updatedRecord = randomMonthEmployeeRecord(2001L, 1001L)
                .setRealPaySalary(new BigDecimal("9000.00"));
        HrmSalaryMonthEmployeeRecordDO addedRecord = randomMonthEmployeeRecord(2001L, 1003L);

        // 调用
        monthEmployeeRecordService.saveMonthEmployeeRecordList(
                2001L, Arrays.asList(updatedRecord, addedRecord));

        // 断言
        assertEquals(retainedRecord.getId(), updatedRecord.getId());
        assertEquals(0, new BigDecimal("9000.00").compareTo(
                monthEmployeeRecordMapper.selectById(retainedRecord.getId()).getRealPaySalary()));
        assertNull(monthEmployeeRecordMapper.selectById(removedRecord.getId()));
        assertNotNull(addedRecord.getId());
    }

    @Test
    public void testSaveMonthEmployeeRecordList_insertAfterDeletedRecord() {
        // mock 数据
        HrmSalaryMonthEmployeeRecordDO deletedRecord = randomMonthEmployeeRecord(2001L, 1001L);
        monthEmployeeRecordMapper.insert(deletedRecord);
        monthEmployeeRecordMapper.deleteById(deletedRecord.getId());
        HrmSalaryMonthEmployeeRecordDO newRecord = randomMonthEmployeeRecord(2001L, 1001L)
                .setRealPaySalary(new BigDecimal("9100.00"));

        // 调用
        monthEmployeeRecordService.saveMonthEmployeeRecordList(
                2001L, Collections.singletonList(newRecord));

        // 断言
        assertNotEquals(deletedRecord.getId(), newRecord.getId());
        assertNull(monthEmployeeRecordMapper.selectById(deletedRecord.getId()));
        HrmSalaryMonthEmployeeRecordDO result = monthEmployeeRecordMapper.selectById(newRecord.getId());
        assertNotNull(result);
        assertEquals(0, new BigDecimal("9100.00").compareTo(result.getRealPaySalary()));
    }

    @Test
    public void testCalculateMonthEmployeeRecord_remunerationBelowThreshold() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Collections.singletonList(
                salaryOption(10101, 10, "劳务报酬", true, true)));
        HrmSalaryMonthEmployeeRecordDO employeeRecord = randomMonthEmployeeRecord(2001L, 1001L);
        Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap = Collections.singletonMap(
                employeeRecord.getEmployeeId(), Collections.singletonList(
                        salaryOptionValue(10101, "劳务报酬", "500")));
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder()
                .type(HrmSalaryTaxTypeEnum.REMUNERATION.getType()).taxEnabled(true)
                .threshold(new BigDecimal("800")).decimalScale(2).build();

        // 调用
        monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                Collections.singletonList(employeeRecord), employeeOptionValueMap,
                Collections.singletonMap(employeeRecord.getEmployeeId(), taxRule));

        // 断言
        assertAmount("500.00", employeeRecord.getExpectedPaySalary());
        assertAmount("0.00", employeeRecord.getTaxableSalary());
        assertAmount("0.00", employeeRecord.getPersonalTax());
        assertAmount("500.00", employeeRecord.getRealPaySalary());
    }

    @Test
    public void testCalculateMonthEmployeeRecord_remunerationIgnoresTaxDisabledOption() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Arrays.asList(
                salaryOption(10101, 10, "计税劳务报酬", true, true),
                salaryOption(20101, 20, "免税劳务补贴", true, false)));
        HrmSalaryMonthEmployeeRecordDO employeeRecord = randomMonthEmployeeRecord(2001L, 1001L);
        Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap = Collections.singletonMap(
                employeeRecord.getEmployeeId(), Arrays.asList(
                        salaryOptionValue(10101, "计税劳务报酬", "10000"),
                        salaryOptionValue(20101, "免税劳务补贴", "5000")));
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder()
                .type(HrmSalaryTaxTypeEnum.REMUNERATION.getType()).taxEnabled(true)
                .threshold(new BigDecimal("800")).decimalScale(2).build();

        // 调用
        monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                Collections.singletonList(employeeRecord), employeeOptionValueMap,
                Collections.singletonMap(employeeRecord.getEmployeeId(), taxRule));

        // 断言
        assertAmount("15000.00", employeeRecord.getExpectedPaySalary());
        assertAmount("8000.00", employeeRecord.getTaxableSalary());
        assertAmount("1600.00", employeeRecord.getPersonalTax());
        assertAmount("13400.00", employeeRecord.getRealPaySalary());
    }

    @Test
    public void testCalculateMonthEmployeeRecord_taxEnabledAndAfterTaxAdjustment() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Arrays.asList(
                salaryOption(10101, 10, "计税工资", true, true),
                salaryOption(20101, 20, "免税补贴", true, false),
                salaryOption(150101, 150, "税后增加", false, false),
                salaryOption(160101, 160, "税后扣减", false, false)));
        HrmSalaryMonthEmployeeRecordDO employeeRecord = randomMonthEmployeeRecord(2001L, 1001L);
        Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap = Collections.singletonMap(
                employeeRecord.getEmployeeId(), Arrays.asList(
                        salaryOptionValue(10101, "计税工资", "10000"),
                        salaryOptionValue(20101, "免税补贴", "5000"),
                        salaryOptionValue(150101, "税后增加", "200"),
                        salaryOptionValue(160101, "税后扣减", "50")));
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder()
                .type(HrmSalaryTaxTypeEnum.SALARY.getType()).taxEnabled(true)
                .threshold(BigDecimal.ZERO).decimalScale(2)
                .cycleType(HrmSalaryTaxCycleTypeEnum.JANUARY_TO_DECEMBER.getType()).build();

        // 调用
        monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                Collections.singletonList(employeeRecord), employeeOptionValueMap,
                Collections.singletonMap(employeeRecord.getEmployeeId(), taxRule));

        // 断言
        assertAmount("15000.00", employeeRecord.getExpectedPaySalary());
        assertAmount("10000.00", employeeRecord.getTaxableSalary());
        assertAmount("300.00", employeeRecord.getPersonalTax());
        assertAmount("14850.00", employeeRecord.getRealPaySalary());
    }

    @Test
    public void testCalculateMonthEmployeeRecord_negativePersonalTaxRefund() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Collections.singletonList(
                salaryOption(10101, 10, "基本工资", true, true)));
        HrmSalaryMonthEmployeeRecordDO employeeRecord = randomMonthEmployeeRecord(2001L, 1001L);
        Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap = Collections.singletonMap(
                employeeRecord.getEmployeeId(), Arrays.asList(
                        salaryOptionValue(10101, "基本工资", "1000"),
                        salaryOptionValue(HrmSalaryOptionCodeEnum.LAST_MONTH_PREPAID_TAX.getCode(),
                                "累计已预缴税额", "100")));
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder()
                .type(HrmSalaryTaxTypeEnum.SALARY.getType()).taxEnabled(true)
                .threshold(new BigDecimal("5000.00")).decimalScale(2)
                .cycleType(HrmSalaryTaxCycleTypeEnum.JANUARY_TO_DECEMBER.getType()).build();

        // 调用
        monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                Collections.singletonList(employeeRecord), employeeOptionValueMap,
                Collections.singletonMap(employeeRecord.getEmployeeId(), taxRule));

        // 断言
        assertAmount("1000.00", employeeRecord.getExpectedPaySalary());
        assertAmount("0.00", employeeRecord.getTaxableSalary());
        assertAmount("-100.00", employeeRecord.getPersonalTax());
        assertAmount("1100.00", employeeRecord.getRealPaySalary());
    }

    @Test
    public void testCalculateMonthEmployeeRecord_attendanceDeductionReducesSalary() {
        // mock 数据
        HrmSalaryOptionDO attendanceDeductionOption = salaryOption(
                HrmSalaryOptionCodeEnum.ATTENDANCE_DEDUCTION_TOTAL.getCode(), 200,
                "考勤扣款合计", true, false).setType(HrmSalaryOptionTypeEnum.MINUS.getType());
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Arrays.asList(
                salaryOption(10101, 10, "基本工资", true, true), attendanceDeductionOption));
        HrmSalaryMonthEmployeeRecordDO employeeRecord = randomMonthEmployeeRecord(2001L, 1001L);
        Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap = Collections.singletonMap(
                employeeRecord.getEmployeeId(), Arrays.asList(
                        salaryOptionValue(10101, "基本工资", "1000"),
                        salaryOptionValue(HrmSalaryOptionCodeEnum.ATTENDANCE_DEDUCTION_TOTAL.getCode(),
                                "考勤扣款合计", "100")));
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder()
                .type(HrmSalaryTaxTypeEnum.NONE.getType()).taxEnabled(false).decimalScale(2).build();

        // 调用
        monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                Collections.singletonList(employeeRecord), employeeOptionValueMap,
                Collections.singletonMap(employeeRecord.getEmployeeId(), taxRule));

        // 断言
        assertAmount("900.00", employeeRecord.getExpectedPaySalary());
        assertAmount("0.00", employeeRecord.getPersonalTax());
        assertAmount("900.00", employeeRecord.getRealPaySalary());
    }

    @Test
    public void testCalculateMonthEmployeeRecord_naturalYearReset() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Collections.singletonList(
                salaryOption(10101, 10, "基本工资", true, true)));
        HrmSalaryMonthEmployeeRecordDO lastRecord = randomMonthEmployeeRecord(2000L, 1001L)
                .setYear(2025).setMonth(12).setOptionValues(Collections.singletonList(
                        monthOptionValue(HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_INCOME.getCode(),
                                "累计收入额", "120000")));
        monthEmployeeRecordMapper.insert(lastRecord);
        HrmSalaryMonthEmployeeRecordDO currentRecord = randomMonthEmployeeRecord(2001L, 1001L)
                .setYear(2026).setMonth(1);
        HrmSalaryTaxRuleDO taxRule = HrmSalaryTaxRuleDO.builder()
                .type(HrmSalaryTaxTypeEnum.SALARY.getType()).taxEnabled(true)
                .threshold(BigDecimal.ZERO).decimalScale(2)
                .cycleType(HrmSalaryTaxCycleTypeEnum.JANUARY_TO_DECEMBER.getType()).build();

        // 调用
        monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                Collections.singletonList(currentRecord),
                Collections.singletonMap(currentRecord.getEmployeeId(), Collections.singletonList(
                        salaryOptionValue(10101, "基本工资", "10000"))),
                Collections.singletonMap(currentRecord.getEmployeeId(), taxRule));

        // 断言
        HrmSalaryMonthEmployeeRecordDO.OptionValue cumulativeIncome = currentRecord.getOptionValues().stream()
                .filter(value -> HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_INCOME.getCode().equals(value.getCode()))
                .findFirst().orElse(null);
        assertNotNull(cumulativeIncome);
        assertAmount("10000.00", cumulativeIncome.getValue());
    }

    @Test
    public void testGetMonthEmployeeRecordPage_filtersEmployee() {
        // mock 数据
        HrmSalaryMonthEmployeeRecordDO matchedRecord = randomMonthEmployeeRecord(2001L, 1001L);
        HrmSalaryMonthEmployeeRecordDO anotherRecord = randomMonthEmployeeRecord(2001L, 1002L);
        monthEmployeeRecordMapper.insert(matchedRecord);
        monthEmployeeRecordMapper.insert(anotherRecord);
        HrmSalaryMonthRecordDO monthRecord = randomMonthRecord(2001L);
        when(monthRecordService.getMonthRecord(monthRecord.getId())).thenReturn(monthRecord);
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(matchedRecord.getEmployeeId(), new HrmEmployeeDO().setId(matchedRecord.getEmployeeId())
                .setName("张三").setJobNumber("HRM-001").setDeptId(3001L).setPostName("开发工程师"));
        employeeMap.put(anotherRecord.getEmployeeId(), new HrmEmployeeDO().setId(anotherRecord.getEmployeeId())
                .setName("李四").setJobNumber("HRM-002").setDeptId(3002L).setPostName("测试工程师"));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(employeeMap);
        // 准备参数
        HrmSalaryMonthEmployeeRecordPageReqVO reqVO = new HrmSalaryMonthEmployeeRecordPageReqVO();
        reqVO.setMonthRecordId(monthRecord.getId());
        reqVO.setEmployeeName("张");
        reqVO.setDeptId(3001L);

        // 调用
        PageResult<HrmSalaryMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getMonthEmployeeRecordPage(reqVO);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(matchedRecord.getEmployeeId(), pageResult.getList().get(0).getEmployeeId());
    }

    @Test
    public void testGetMonthEmployeeChangeCount() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord = randomMonthRecord(2001L);
        when(monthRecordService.getMonthRecord(monthRecord.getId())).thenReturn(monthRecord);
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords = Arrays.asList(
                randomMonthEmployeeRecord(monthRecord.getId(), 1001L),
                randomMonthEmployeeRecord(monthRecord.getId(), 1002L),
                randomMonthEmployeeRecord(monthRecord.getId(), 1003L),
                randomMonthEmployeeRecord(monthRecord.getId(), 1004L));
        employeeRecords.forEach(monthEmployeeRecordMapper::insert);
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(1001L, new HrmEmployeeDO().setId(1001L)
                .setEntryTime(LocalDate.of(2026, 7, 3).atStartOfDay()));
        employeeMap.put(1002L, new HrmEmployeeDO().setId(1002L)
                .setLeaveTime(LocalDate.of(2026, 7, 20).atStartOfDay()));
        employeeMap.put(1003L, new HrmEmployeeDO().setId(1003L));
        employeeMap.put(1004L, new HrmEmployeeDO().setId(1004L));
        when(employeeService.getEmployeeMap(anyCollection())).thenReturn(employeeMap);
        List<HrmEmployeeChangeRecordDO> changeRecords = Arrays.asList(
                new HrmEmployeeChangeRecordDO().setEmployeeId(1003L)
                        .setType(HrmEmployeeChangeTypeEnum.REGULAR.getType()),
                new HrmEmployeeChangeRecordDO().setEmployeeId(1004L)
                        .setType(HrmEmployeeChangeTypeEnum.TRANSFER.getType()));
        when(employeeChangeRecordService.getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
                anyCollection(), any(LocalDateTime[].class)))
                .thenReturn(changeRecords);
        // 准备参数
        HrmSalaryMonthEmployeeRecordPageReqVO reqVO = new HrmSalaryMonthEmployeeRecordPageReqVO();
        reqVO.setMonthRecordId(monthRecord.getId());

        // 调用
        Map<Integer, Long> countMap = monthEmployeeRecordService.getMonthEmployeeChangeCount(reqVO);

        // 断言
        assertEquals(4L, countMap.get(HrmSalaryEmployeeChangeTypeEnum.ALL.getType()));
        assertEquals(1L, countMap.get(HrmSalaryEmployeeChangeTypeEnum.ENTRY.getType()));
        assertEquals(1L, countMap.get(HrmSalaryEmployeeChangeTypeEnum.LEAVE.getType()));
        assertEquals(1L, countMap.get(HrmSalaryEmployeeChangeTypeEnum.REGULAR.getType()));
        assertEquals(1L, countMap.get(HrmSalaryEmployeeChangeTypeEnum.TRANSFER.getType()));
    }

    @Test
    public void testGetMonthEmployeeRecord_monthRecordNotExists() {
        // 准备参数
        HrmSalaryMonthEmployeeRecordPageReqVO pageReqVO = new HrmSalaryMonthEmployeeRecordPageReqVO();
        pageReqVO.setMonthRecordId(2001L);
        HrmSalaryMonthEmployeeRecordListReqVO listReqVO = new HrmSalaryMonthEmployeeRecordListReqVO();
        listReqVO.setMonthRecordId(2001L);

        // 调用，并断言
        assertEquals(0L, monthEmployeeRecordService.getMonthEmployeeRecordPage(pageReqVO).getTotal());
        assertTrue(monthEmployeeRecordService.getMonthEmployeeRecordList(listReqVO).isEmpty());
        assertTrue(monthEmployeeRecordService.getMonthEmployeeChangeCount(pageReqVO).isEmpty());
    }

    @Test
    public void testGetPerformanceCoefficientMap() {
        // mock 方法
        when(performancePlanService.getPerformancePlanListByPaidForMonth("2026-07"))
                .thenReturn(Arrays.asList(
                        new HrmPerformancePlanDO().setId(2001L).setSyncToSalary(true),
                        new HrmPerformancePlanDO().setId(2002L).setSyncToSalary(false)));
        when(performanceAssessmentService.getPerformanceArchiveEmployeeCoefficientMap(
                eq(Collections.singleton(2001L)), anyCollection()))
                .thenReturn(Collections.singletonMap(1001L, BigDecimal.valueOf(1.1)));
        // 准备参数
        HrmSalaryPerformanceCoefficientReqVO reqVO = new HrmSalaryPerformanceCoefficientReqVO();
        reqVO.setYear(2026);
        reqVO.setMonth(7);
        reqVO.setEmployeeIds(Collections.singletonList(1001L));

        // 调用
        Map<Long, BigDecimal> coefficientMap = monthEmployeeRecordService.getPerformanceCoefficientMap(reqVO);

        // 断言
        assertEquals(0, BigDecimal.valueOf(1.1).compareTo(coefficientMap.get(1001L)));
    }

    @Test
    public void testGetEmployeeMonthRecordPage() {
        // mock 数据
        HrmSalaryMonthRecordDO historyMonthRecord = randomMonthRecord(2001L).setStatus(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus());
        when(monthRecordService.getMonthRecordListByStatus(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus()))
                .thenReturn(Collections.singletonList(historyMonthRecord));
        HrmSalaryMonthEmployeeRecordDO employeeRecord = randomMonthEmployeeRecord(historyMonthRecord.getId(), 1001L);
        monthEmployeeRecordMapper.insert(employeeRecord);
        // 准备参数
        HrmSalaryEmployeeMonthRecordPageReqVO reqVO = new HrmSalaryEmployeeMonthRecordPageReqVO();
        reqVO.setEmployeeId(employeeRecord.getEmployeeId());
        reqVO.setMonthRecordStatus(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus());

        // 调用
        PageResult<HrmSalaryMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getEmployeeMonthRecordPage(reqVO);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(employeeRecord.getId(), pageResult.getList().get(0).getId());
    }

    // ========== 随机对象 ==========

    private HrmSalaryMonthEmployeeRecordDO randomMonthEmployeeRecord(Long monthRecordId, Long employeeId) {
        return randomPojo(HrmSalaryMonthEmployeeRecordDO.class, record -> record.setId(null)
                .setMonthRecordId(monthRecordId).setEmployeeId(employeeId).setYear(2026).setMonth(7)
                .setActualWorkDay(BigDecimal.valueOf(20)).setNeedWorkDay(BigDecimal.valueOf(22))
                .setExpectedPaySalary(BigDecimal.valueOf(8000)).setTaxableSalary(BigDecimal.valueOf(7000))
                .setPersonalTax(BigDecimal.valueOf(200)).setRealPaySalary(BigDecimal.valueOf(7800))
                .setOptionValues(Collections.emptyList()));
    }

    private HrmSalaryMonthRecordDO randomMonthRecord(Long id) {
        return randomPojo(HrmSalaryMonthRecordDO.class, record -> record.setId(id).setYear(2026).setMonth(7)
                .setStartTime(LocalDate.of(2026, 7, 1).atStartOfDay())
                .setEndTime(LocalDate.of(2026, 7, 31).atStartOfDay()));
    }

    private static HrmSalaryOptionDO salaryOption(Integer code, Integer parentCode, String name,
                                                   boolean calculateEnabled, boolean taxEnabled) {
        return new HrmSalaryOptionDO().setCode(code).setParentCode(parentCode).setName(name)
                .setType(1).setCalculateEnabled(calculateEnabled).setTaxEnabled(taxEnabled)
                .setEnabled(true).setVisible(true);
    }

    private static HrmSalaryOptionValueVO salaryOptionValue(Integer code, String name, String value) {
        return new HrmSalaryOptionValueVO().setCode(code).setName(name).setValue(new BigDecimal(value));
    }

    private static HrmSalaryMonthEmployeeRecordDO.OptionValue monthOptionValue(
            Integer code, String name, String value) {
        return HrmSalaryMonthEmployeeRecordDO.OptionValue.builder()
                .code(code).name(name).value(new BigDecimal(value)).build();
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

}
