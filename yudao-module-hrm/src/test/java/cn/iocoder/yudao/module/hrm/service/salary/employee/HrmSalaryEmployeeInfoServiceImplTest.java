package cn.iocoder.yudao.module.hrm.service.salary.employee;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoImportRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.employee.HrmSalaryEmployeeInfoMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryBatchAdjustTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeInfoChangeTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_EFFECT_DATE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_RECORD_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalaryEmployeeInfoServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryEmployeeInfoServiceImpl.class)
public class HrmSalaryEmployeeInfoServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryEmployeeInfoServiceImpl salaryEmployeeInfoService;

    @Resource
    private HrmSalaryEmployeeInfoMapper salaryEmployeeInfoMapper;

    @MockitoBean
    private HrmSalaryOptionService salaryOptionService;
    @MockitoBean
    private HrmSalaryChangeRecordService salaryChangeRecordService;
    @MockitoBean
    private HrmSalaryMonthRecordService salaryMonthRecordService;
    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testGetSalaryEmployeeInfoPage_noMatchedEmployee() {
        // mock 数据
        salaryEmployeeInfoMapper.insert(HrmSalaryEmployeeInfoDO.builder()
                .employeeId(1L).changeType(HrmSalaryEmployeeInfoChangeTypeEnum.SET.getType()).build());
        // 准备参数
        HrmSalaryEmployeeInfoPageReqVO reqVO = new HrmSalaryEmployeeInfoPageReqVO();
        reqVO.setEmployeeId(2L);
        reqVO.setChangeType(HrmSalaryEmployeeInfoChangeTypeEnum.SET.getType());

        // 调用
        PageResult<HrmEmployeeDO> result =
                salaryEmployeeInfoService.getSalaryEmployeeInfoPage(reqVO);

        // 断言
        assertTrue(result.getList().isEmpty());
        assertEquals(0L, result.getTotal());
        verifyNoInteractions(employeeService);
    }

    @Test
    public void testGetSalaryEmployeeInfoPage_unsetSalary() {
        // mock 数据
        salaryEmployeeInfoMapper.insert(HrmSalaryEmployeeInfoDO.builder()
                .employeeId(1L).changeType(HrmSalaryEmployeeInfoChangeTypeEnum.SET.getType()).build());
        when(employeeService.getEmployeePage(any())).thenReturn(PageResult.empty());
        // 准备参数
        HrmSalaryEmployeeInfoPageReqVO reqVO = new HrmSalaryEmployeeInfoPageReqVO();
        reqVO.setEmployeeIds(Arrays.asList(2L, 3L));
        reqVO.setChangeType(HrmSalaryEmployeeInfoChangeTypeEnum.UNSET.getType());

        // 调用
        salaryEmployeeInfoService.getSalaryEmployeeInfoPage(reqVO);

        // 断言
        ArgumentCaptor<HrmEmployeePageReqVO> reqVOCaptor =
                ArgumentCaptor.forClass(HrmEmployeePageReqVO.class);
        verify(employeeService).getEmployeePage(reqVOCaptor.capture());
        assertEquals(Arrays.asList(2L, 3L), reqVOCaptor.getValue().getIds());
        assertEquals(Collections.singletonList(1L), reqVOCaptor.getValue().getExcludeIds());
    }

    @Test
    public void testGetSalaryEmployeeInfoStatusCount() {
        // mock 数据
        Map<Integer, Long> expectedCountMap = Collections.singletonMap(
                HrmEmployeeStatusEnum.REGULAR.getStatus(), 2L);
        when(employeeService.getEmployeeStatusCount(any())).thenReturn(expectedCountMap);
        // 准备参数
        HrmSalaryEmployeeInfoPageReqVO reqVO = new HrmSalaryEmployeeInfoPageReqVO();
        reqVO.setEmployeeIds(Arrays.asList(1L, 2L));

        // 调用
        Map<Integer, Long> countMap =
                salaryEmployeeInfoService.getSalaryEmployeeInfoStatusCount(reqVO);

        // 断言
        assertEquals(expectedCountMap, countMap);
    }

    @Test
    public void testUpdateSalaryEmployeeInfo_setSalary() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        mockCreateChangeRecord();
        // 准备参数
        HrmSalaryEmployeeInfoUpdateReqVO reqVO =
                buildSetSalaryReqVO(employee.getId(), new BigDecimal("8000"),
                        new BigDecimal("2000"), new BigDecimal("6000"));

        // 调用
        Long changeRecordId = salaryEmployeeInfoService.updateSalaryEmployeeInfo(reqVO);

        // 断言
        assertEquals(99L, changeRecordId);
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                salaryEmployeeInfoMapper.selectByEmployeeId(employee.getId());
        assertNotNull(salaryEmployeeInfo);
        assertEquals(HrmSalaryEmployeeInfoChangeTypeEnum.SET.getType(), salaryEmployeeInfo.getChangeType());
        assertAmount("10000.00", salaryEmployeeInfo.getRegularSalary());
        assertAmount("6000.00", salaryEmployeeInfo.getProbationSalary());
        ArgumentCaptor<HrmSalaryChangeRecordDO> recordCaptor =
                ArgumentCaptor.forClass(HrmSalaryChangeRecordDO.class);
        verify(salaryChangeRecordService).createSalaryChangeRecord(recordCaptor.capture());
        HrmSalaryChangeRecordDO changeRecord = recordCaptor.getValue();
        assertEquals(HrmSalaryChangeReasonEnum.ENTRY_SALARY.getReason(), changeRecord.getReason());
        assertEquals(HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(), changeRecord.getStatus());
        assertAmount("0", changeRecord.getBeforeTotal());
        assertAmount("10000.00", changeRecord.getAfterTotal());
    }

    @Test
    public void testUpdateSalaryEmployeeInfo_adjustmentPending() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employee.getId()));
        mockCreateChangeRecord();
        // 准备参数
        HrmSalaryEmployeeInfoUpdateReqVO reqVO =
                buildAdjustmentReqVO(employee.getId(), LocalDate.now().plusDays(1));

        // 调用
        salaryEmployeeInfoService.updateSalaryEmployeeInfo(reqVO);

        // 断言
        assertAmount("10000.00", salaryEmployeeInfoMapper
                .selectByEmployeeId(employee.getId()).getRegularSalary());
        ArgumentCaptor<HrmSalaryChangeRecordDO> recordCaptor =
                ArgumentCaptor.forClass(HrmSalaryChangeRecordDO.class);
        verify(salaryChangeRecordService).createSalaryChangeRecord(recordCaptor.capture());
        assertEquals(HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(), recordCaptor.getValue().getStatus());
        assertAmount("10000.00", recordCaptor.getValue().getBeforeTotal());
        assertAmount("11000.00", recordCaptor.getValue().getAfterTotal());
    }

    @Test
    public void testUpdateSalaryEmployeeInfo_adjustmentEffective() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employee.getId()));
        mockCreateChangeRecord();
        // 准备参数
        HrmSalaryEmployeeInfoUpdateReqVO reqVO =
                buildAdjustmentReqVO(employee.getId(), LocalDate.now());
        reqVO.setEffectTime(LocalDate.now().atTime(15, 30));

        // 调用
        salaryEmployeeInfoService.updateSalaryEmployeeInfo(reqVO);

        // 断言
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                salaryEmployeeInfoMapper.selectByEmployeeId(employee.getId());
        assertEquals(HrmSalaryEmployeeInfoChangeTypeEnum.ADJUSTED.getType(), salaryEmployeeInfo.getChangeType());
        assertAmount("11000.00", salaryEmployeeInfo.getRegularSalary());
        ArgumentCaptor<HrmSalaryChangeRecordDO> recordCaptor =
                ArgumentCaptor.forClass(HrmSalaryChangeRecordDO.class);
        verify(salaryChangeRecordService).createSalaryChangeRecord(recordCaptor.capture());
        assertEquals(HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus(), recordCaptor.getValue().getStatus());
        assertEquals(LocalDate.now().atStartOfDay(), recordCaptor.getValue().getEffectTime());
        assertEquals(LocalDate.now().atStartOfDay(), salaryEmployeeInfo.getEffectTime());
    }

    @Test
    public void testUpdateSalaryEmployeeInfoList_percentPrecision() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employee.getId()));
        when(employeeService.getEmployeeListByDeptIds(Collections.emptyList()))
                .thenReturn(Collections.emptyList());
        mockCreateChangeRecord();
        // 准备参数
        HrmSalaryEmployeeInfoUpdateListReqVO reqVO = new HrmSalaryEmployeeInfoUpdateListReqVO()
                .setEmployeeIds(Collections.singletonList(employee.getId()))
                .setDeptIds(Collections.emptyList())
                .setType(HrmSalaryBatchAdjustTypeEnum.PERCENT.getType())
                .setChangeReason(HrmSalaryChangeReasonEnum.PROMOTION.getReason())
                .setEffectTime(LocalDate.now().atStartOfDay())
                .setSalaryOptions(Collections.singletonList(
                        optionValue(10101, "基本工资", new BigDecimal("2.5"))));

        // 调用
        salaryEmployeeInfoService.updateSalaryEmployeeInfoList(reqVO);

        // 断言
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo = salaryEmployeeInfoMapper
                .selectByEmployeeId(employee.getId());
        assertAmount("10200.00", salaryEmployeeInfo.getRegularSalary());
        assertAmount("8200.00", salaryEmployeeInfo.getSalaryOptions().get(0).getValue());
    }

    @Test
    public void testUpdateSalaryEmployeeInfoList_smallPercentPrecision() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employee.getId()));
        when(employeeService.getEmployeeListByDeptIds(Collections.emptyList()))
                .thenReturn(Collections.emptyList());
        mockCreateChangeRecord();
        // 准备参数
        HrmSalaryEmployeeInfoUpdateListReqVO reqVO = new HrmSalaryEmployeeInfoUpdateListReqVO()
                .setEmployeeIds(Collections.singletonList(employee.getId()))
                .setDeptIds(Collections.emptyList())
                .setType(HrmSalaryBatchAdjustTypeEnum.PERCENT.getType())
                .setChangeReason(HrmSalaryChangeReasonEnum.PROMOTION.getReason())
                .setEffectTime(LocalDate.now().atStartOfDay())
                .setSalaryOptions(Collections.singletonList(
                        optionValue(10101, "基本工资", new BigDecimal("0.01"))));

        // 调用
        salaryEmployeeInfoService.updateSalaryEmployeeInfoList(reqVO);

        // 断言
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo = salaryEmployeeInfoMapper
                .selectByEmployeeId(employee.getId());
        assertAmount("10000.80", salaryEmployeeInfo.getRegularSalary());
        assertAmount("8000.80", salaryEmployeeInfo.getSalaryOptions().get(0).getValue());
    }

    @Test
    public void testUpdateSalaryEmployeeInfo_fixedSalaryWithAdjustment() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        when(salaryChangeRecordService.hasUncancelledSalaryAdjustmentRecord(employee.getId()))
                .thenReturn(true);
        // 准备参数
        HrmSalaryEmployeeInfoUpdateReqVO reqVO =
                buildSetSalaryReqVO(employee.getId(), new BigDecimal("8000"),
                        new BigDecimal("2000"), new BigDecimal("6000"));

        // 调用，并断言异常
        assertServiceException(() -> salaryEmployeeInfoService.updateSalaryEmployeeInfo(reqVO),
                SALARY_CHANGE_RECORD_STATUS_INVALID);
    }

    @Test
    public void testUpdateSalaryEmployeeInfo_effectDateBeforeLastSalaryMonth() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employee.getId()));
        LocalDate latestSalaryMonthBeginDate = LocalDate.now().withDayOfMonth(1);
        when(salaryMonthRecordService.getLastMonthRecord()).thenReturn(
                HrmSalaryMonthRecordDO.builder()
                        .year(latestSalaryMonthBeginDate.getYear())
                        .month(latestSalaryMonthBeginDate.getMonthValue()).build());
        // 准备参数
        HrmSalaryEmployeeInfoUpdateReqVO reqVO =
                buildAdjustmentReqVO(employee.getId(), latestSalaryMonthBeginDate.minusDays(1));

        // 调用，并断言异常
        assertServiceException(() -> salaryEmployeeInfoService.updateSalaryEmployeeInfo(reqVO),
                SALARY_CHANGE_EFFECT_DATE_INVALID);
    }

    @Test
    public void testGetSalaryImportOptionList() {
        // mock 数据
        List<HrmSalaryOptionDO> options = buildSalaryOptions();
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(options);

        // 调用
        List<HrmSalaryOptionDO> result =
                salaryEmployeeInfoService.getSalaryImportOptionList();

        // 断言
        assertEquals(2, result.size());
        assertEquals(Arrays.asList(10101, 10102),
                Arrays.asList(result.get(0).getCode(), result.get(1).getCode()));
    }

    @Test
    public void testImportFixSalaryList_successAndFailure() {
        // mock 数据
        mockSalaryOptions();
        HrmEmployeeDO employee = mockEmployee(1L, HrmEmployeeStatusEnum.REGULAR.getStatus());
        employee.setJobNumber("NO-导入定薪");
        when(employeeService.getEmployeeByJobNumber(employee.getJobNumber())).thenReturn(employee);
        mockCreateChangeRecord();
        Map<Integer, String> successRow = row(
                1, employee.getJobNumber(), 4, "6000", 5, "0",
                6, "8000", 7, "2000", 8, "导入定薪");
        Map<Integer, String> failureRow = row(1, "NO-不存在");

        // 调用
        HrmSalaryEmployeeInfoImportRespVO result =
                salaryEmployeeInfoService.importFixSalaryList(
                        Arrays.asList(successRow, failureRow));

        // 断言
        assertEquals(Collections.singletonList(employee.getJobNumber()),
                result.getSuccessJobNumbers());
        assertEquals("工号对应的员工不存在",
                result.getFailureJobNumbers().get("NO-不存在"));
        assertAmount("10000.00", salaryEmployeeInfoMapper
                .selectByEmployeeId(employee.getId()).getRegularSalary());
    }

    @Test
    public void testApplyDueSalaryChanges_success() {
        // mock 数据
        mockSalaryOptions();
        Long employeeId = 1L;
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employeeId));
        HrmSalaryChangeRecordDO changeRecord = HrmSalaryChangeRecordDO.builder()
                .id(99L).employeeId(employeeId).type(HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType())
                .reason(HrmSalaryChangeReasonEnum.PROMOTION.getReason()).effectTime(LocalDate.now().atStartOfDay())
                .salaryOptions(Arrays.asList(
                        changeRecordOption(10101, "基本工资", new BigDecimal("9000")),
                        changeRecordOption(10102, "岗位工资", new BigDecimal("2000"))))
                .probationSalaryOptions(Collections.singletonList(
                        changeRecordOption(10101, "基本工资", new BigDecimal("6500"))))
                .status(HrmSalaryChangeRecordStatusEnum.PENDING.getStatus()).build();
        when(salaryChangeRecordService.getDueSalaryChangeRecordList(LocalDate.now()))
                .thenReturn(Collections.singletonList(changeRecord));
        when(salaryChangeRecordService.updateSalaryChangeRecordStatus(
                changeRecord.getId(), HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus())).thenReturn(true);

        // 调用
        List<Long> employeeIds =
                salaryEmployeeInfoService.applyDueSalaryChanges(LocalDate.now());

        // 断言
        assertEquals(Collections.singletonList(employeeId), employeeIds);
        assertAmount("11000.00", salaryEmployeeInfoMapper
                .selectByEmployeeId(employeeId).getRegularSalary());
        verify(salaryChangeRecordService).updateSalaryChangeRecordStatus(
                changeRecord.getId(), HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus());
        verify(salaryChangeRecordService, never()).updateSalaryChangeRecord(any());
    }

    @Test
    public void testApplyDueSalaryChanges_statusChanged() {
        // mock 数据
        Long employeeId = 1L;
        salaryEmployeeInfoMapper.insert(buildSalaryEmployeeInfo(employeeId));
        HrmSalaryChangeRecordDO changeRecord = HrmSalaryChangeRecordDO.builder()
                .id(99L).employeeId(employeeId)
                .status(HrmSalaryChangeRecordStatusEnum.PENDING.getStatus()).build();
        when(salaryChangeRecordService.getDueSalaryChangeRecordList(LocalDate.now()))
                .thenReturn(Collections.singletonList(changeRecord));
        when(salaryChangeRecordService.updateSalaryChangeRecordStatus(
                changeRecord.getId(), HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus())).thenReturn(false);

        // 调用
        List<Long> employeeIds = salaryEmployeeInfoService.applyDueSalaryChanges(LocalDate.now());

        // 断言
        assertTrue(employeeIds.isEmpty());
        assertAmount("10000.00", salaryEmployeeInfoMapper
                .selectByEmployeeId(employeeId).getRegularSalary());
    }

    @Test
    public void testGetSalaryEmployeeInfoList_empty() {
        // 调用
        List<HrmSalaryEmployeeInfoDO> result =
                salaryEmployeeInfoService.getSalaryEmployeeInfoList(Collections.emptyList());

        // 断言
        assertTrue(result.isEmpty());
    }

    // ========== 随机对象 ==========

    private HrmEmployeeDO mockEmployee(Long id, Integer status) {
        HrmEmployeeDO employee = HrmEmployeeDO.builder()
                .id(id).name("测试员工").jobNumber("NO-" + id).status(status).build();
        when(employeeService.validateEmployeeExists(id)).thenReturn(employee);
        when(employeeService.getEmployee(id)).thenReturn(employee);
        return employee;
    }

    private void mockSalaryOptions() {
        when(salaryOptionService.getSalaryOptionList(false))
                .thenReturn(buildSalaryOptions());
    }

    private void mockCreateChangeRecord() {
        doAnswer(invocation -> {
            HrmSalaryChangeRecordDO record = invocation.getArgument(0);
            record.setId(99L);
            return record.getId();
        }).when(salaryChangeRecordService).createSalaryChangeRecord(
                any(HrmSalaryChangeRecordDO.class));
    }

    private static HrmSalaryEmployeeInfoUpdateReqVO buildSetSalaryReqVO(
            Long employeeId, BigDecimal regularBase,
            BigDecimal regularPost, BigDecimal probationBase) {
        return new HrmSalaryEmployeeInfoUpdateReqVO()
                .setEmployeeId(employeeId).setRecordType(HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType())
                .setChangeReason(HrmSalaryChangeReasonEnum.ENTRY_SALARY.getReason())
                .setEffectTime(LocalDate.of(2026, 7, 1).atStartOfDay())
                .setSalaryOptions(Arrays.asList(
                        optionValue(10101, "基本工资", regularBase),
                        optionValue(10102, "岗位工资", regularPost)))
                .setProbationSalaryOptions(Collections.singletonList(
                        optionValue(10101, "基本工资", probationBase)));
    }

    private static HrmSalaryEmployeeInfoUpdateReqVO buildAdjustmentReqVO(
            Long employeeId, LocalDate effectDate) {
        return new HrmSalaryEmployeeInfoUpdateReqVO()
                .setEmployeeId(employeeId).setRecordType(HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType())
                .setChangeReason(HrmSalaryChangeReasonEnum.PROMOTION.getReason()).setEffectTime(effectDate.atStartOfDay())
                .setSalaryOptions(Arrays.asList(
                        optionValue(10101, "基本工资", new BigDecimal("9000")),
                        optionValue(10102, "岗位工资", new BigDecimal("2000"))))
                .setProbationSalaryOptions(Collections.singletonList(
                        optionValue(10101, "基本工资", new BigDecimal("6500"))));
    }

    private static HrmSalaryEmployeeInfoDO buildSalaryEmployeeInfo(Long employeeId) {
        return HrmSalaryEmployeeInfoDO.builder().employeeId(employeeId)
                .changeType(HrmSalaryEmployeeInfoChangeTypeEnum.SET.getType()).effectTime(LocalDate.of(2026, 7, 1).atStartOfDay())
                .regularSalary(new BigDecimal("10000"))
                .probationSalary(new BigDecimal("6000"))
                .salaryOptions(Arrays.asList(
                        salaryOption(10101, "基本工资", new BigDecimal("8000")),
                        salaryOption(10102, "岗位工资", new BigDecimal("2000"))))
                .probationSalaryOptions(Collections.singletonList(
                        salaryOption(10101, "基本工资", new BigDecimal("6000"))))
                .build();
    }

    private static List<HrmSalaryOptionDO> buildSalaryOptions() {
        return Arrays.asList(
                optionDO(10, 0, "工资", false),
                optionDO(10101, 10, "基本工资", true),
                optionDO(10102, 10, "岗位工资", true));
    }

    private static HrmSalaryOptionDO optionDO(
            Integer code, Integer parentCode, String name, boolean calculateEnabled) {
        return new HrmSalaryOptionDO().setCode(code).setParentCode(parentCode)
                .setName(name).setType(1).setTaxEnabled(true)
                .setCalculateEnabled(calculateEnabled).setVisible(true).setEnabled(true);
    }

    private static HrmSalaryOptionValueVO optionValue(
            Integer code, String name, BigDecimal value) {
        return new HrmSalaryOptionValueVO().setCode(code).setName(name).setValue(value);
    }

    private static HrmSalaryEmployeeInfoDO.SalaryOption salaryOption(
            Integer code, String name, BigDecimal value) {
        return HrmSalaryEmployeeInfoDO.SalaryOption.builder()
                .code(code).name(name).value(value).build();
    }

    private static HrmSalaryChangeRecordDO.SalaryOption changeRecordOption(
            Integer code, String name, BigDecimal value) {
        return HrmSalaryChangeRecordDO.SalaryOption.builder()
                .code(code).name(name).value(value).build();
    }

    private static Map<Integer, String> row(Object... values) {
        Map<Integer, String> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            row.put((Integer) values[i], String.valueOf(values[i + 1]));
        }
        return row;
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

}
