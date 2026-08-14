package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendEmployeeReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipSendRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip.HrmSalarySlipSendRecordMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_EMP_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_EMPLOYEE_ACCOUNT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalarySlipSendRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalarySlipSendRecordServiceImpl.class)
public class HrmSalarySlipSendRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalarySlipSendRecordServiceImpl salarySlipSendRecordService;
    @Resource
    private HrmSalarySlipSendRecordMapper salarySlipSendRecordMapper;

    @MockitoBean
    private HrmSalaryMonthRecordService monthRecordService;
    @MockitoBean
    private HrmSalaryMonthEmployeeRecordService monthEmployeeRecordService;
    @MockitoBean
    private HrmSalarySlipTemplateService salarySlipTemplateService;
    @MockitoBean
    private HrmSalarySlipService salarySlipService;
    @MockitoBean
    private HrmEmployeeService employeeService;

    @Test
    public void testSendSalarySlip_selectedEmployees_success() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord = createMonthRecord(2026, 8, 2);
        HrmSalaryMonthEmployeeRecordDO employeeRecord =
                createMonthEmployeeRecord(monthRecord.getId(), 1002L);
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setHideEmpty(false).setOptions(Collections.emptyList());
        when(monthRecordService.validateMonthRecordExistsForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(salarySlipTemplateService.buildSalarySlipTemplateSnapshot(false, Collections.emptyList()))
                .thenReturn(template);
        when(monthEmployeeRecordService.getMonthEmployeeRecordList(
                any(HrmSalaryMonthEmployeeRecordListReqVO.class)))
                .thenReturn(Collections.singletonList(employeeRecord));
        when(employeeService.getEmployeeMap(Collections.singleton(employeeRecord.getEmployeeId())))
                .thenReturn(Collections.singletonMap(employeeRecord.getEmployeeId(),
                        new HrmEmployeeDO().setId(employeeRecord.getEmployeeId()).setUserId(10002L)));
        HrmSalarySlipSendReqVO reqVO = new HrmSalarySlipSendReqVO();
        reqVO.setMonthRecordId(monthRecord.getId());
        reqVO.setHideEmpty(false);
        reqVO.setOptions(Collections.emptyList());
        reqVO.setAll(false);
        reqVO.setEmployeeIds(Collections.singletonList(employeeRecord.getEmployeeId()));

        // 调用
        Long sendRecordId = salarySlipSendRecordService.sendSalarySlip(reqVO);

        // 断言
        HrmSalarySlipSendRecordDO sendRecord = salarySlipSendRecordMapper.selectById(sendRecordId);
        assertEquals(monthRecord.getEmployeeCount(), sendRecord.getEmployeeCount());
        assertEquals(1, sendRecord.getSendEmployeeCount());
        verify(salarySlipService).createSalarySlipList(
                sendRecordId, Collections.singletonList(employeeRecord), template);
    }

    @Test
    public void testSendSalarySlip_employeeNotInMonthRecord() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord = createMonthRecord(2026, 9, 1);
        when(monthRecordService.validateMonthRecordExistsForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(salarySlipTemplateService.buildSalarySlipTemplateSnapshot(false, null))
                .thenReturn(new HrmSalarySlipTemplateDO().setHideEmpty(false).setOptions(Collections.emptyList()));
        when(monthEmployeeRecordService.getMonthEmployeeRecordList(
                any(HrmSalaryMonthEmployeeRecordListReqVO.class))).thenReturn(Collections.emptyList());
        HrmSalarySlipSendReqVO reqVO = new HrmSalarySlipSendReqVO();
        reqVO.setMonthRecordId(monthRecord.getId());
        reqVO.setHideEmpty(false);
        reqVO.setAll(false);
        reqVO.setEmployeeIds(Collections.singletonList(2999L));

        // 调用，并断言异常
        assertServiceException(() -> salarySlipSendRecordService.sendSalarySlip(reqVO),
                SALARY_MONTH_EMP_RECORD_NOT_EXISTS);
    }

    @Test
    public void testSendSalarySlip_selectedEmployeeWithoutAccount() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord = createMonthRecord(2026, 9, 1);
        HrmSalaryMonthEmployeeRecordDO employeeRecord =
                createMonthEmployeeRecord(monthRecord.getId(), 2101L);
        when(monthRecordService.validateMonthRecordExistsForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(salarySlipTemplateService.buildSalarySlipTemplateSnapshot(false, null))
                .thenReturn(new HrmSalarySlipTemplateDO().setHideEmpty(false).setOptions(Collections.emptyList()));
        when(monthEmployeeRecordService.getMonthEmployeeRecordList(
                any(HrmSalaryMonthEmployeeRecordListReqVO.class)))
                .thenReturn(Collections.singletonList(employeeRecord));
        when(employeeService.getEmployeeMap(Collections.singleton(employeeRecord.getEmployeeId())))
                .thenReturn(Collections.singletonMap(employeeRecord.getEmployeeId(),
                        new HrmEmployeeDO().setId(employeeRecord.getEmployeeId())));
        HrmSalarySlipSendReqVO reqVO = new HrmSalarySlipSendReqVO()
                .setMonthRecordId(monthRecord.getId()).setHideEmpty(false).setAll(false)
                .setEmployeeIds(Collections.singletonList(employeeRecord.getEmployeeId()));

        // 调用，并断言异常
        assertServiceException(() -> salarySlipSendRecordService.sendSalarySlip(reqVO),
                SALARY_SLIP_EMPLOYEE_ACCOUNT_NOT_EXISTS);
    }

    @Test
    public void testSendSalarySlip_allSkipEmployeeWithoutAccount() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord = createMonthRecord(2026, 9, 2);
        HrmSalaryMonthEmployeeRecordDO boundEmployeeRecord =
                createMonthEmployeeRecord(monthRecord.getId(), 2201L);
        HrmSalaryMonthEmployeeRecordDO unboundEmployeeRecord =
                createMonthEmployeeRecord(monthRecord.getId(), 2202L);
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setHideEmpty(false).setOptions(Collections.emptyList());
        when(monthRecordService.validateMonthRecordExistsForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(salarySlipTemplateService.buildSalarySlipTemplateSnapshot(false, null)).thenReturn(template);
        when(monthEmployeeRecordService.getMonthEmployeeRecordList(
                any(HrmSalaryMonthEmployeeRecordListReqVO.class)))
                .thenReturn(Arrays.asList(boundEmployeeRecord, unboundEmployeeRecord));
        when(employeeService.getEmployeeMap(any())).thenReturn(Collections.singletonMap(
                boundEmployeeRecord.getEmployeeId(), new HrmEmployeeDO()
                        .setId(boundEmployeeRecord.getEmployeeId()).setUserId(12201L)));
        HrmSalarySlipSendReqVO reqVO = new HrmSalarySlipSendReqVO()
                .setMonthRecordId(monthRecord.getId()).setHideEmpty(false).setAll(true);

        // 调用
        Long sendRecordId = salarySlipSendRecordService.sendSalarySlip(reqVO);

        // 断言
        assertEquals(1, salarySlipSendRecordMapper.selectById(sendRecordId).getSendEmployeeCount());
        verify(salarySlipService).createSalarySlipList(
                sendRecordId, Collections.singletonList(boundEmployeeRecord), template);
    }

    @Test
    public void testSendSalarySlip_allFiltered_success() {
        // mock 数据
        HrmSalaryMonthRecordDO monthRecord = createMonthRecord(2026, 10, 2);
        HrmSalaryMonthEmployeeRecordDO employeeRecord =
                createMonthEmployeeRecord(monthRecord.getId(), 2202L);
        HrmEmployeeDO employee = randomPojo(HrmEmployeeDO.class, o -> {
            o.setId(employeeRecord.getEmployeeId()).setUserId(12202L)
                    .setName("全部发放员工").setDeptId(200L);
        });
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setHideEmpty(false).setOptions(Collections.emptyList());
        when(monthRecordService.validateMonthRecordExistsForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(salarySlipTemplateService.buildSalarySlipTemplateSnapshot(false, null)).thenReturn(template);
        when(employeeService.getEmployeeList(any(HrmEmployeeListReqVO.class)))
                .thenReturn(Collections.singletonList(employee));
        when(monthEmployeeRecordService.getMonthEmployeeRecordList(
                any(HrmSalaryMonthEmployeeRecordListReqVO.class)))
                .thenReturn(Collections.singletonList(employeeRecord));
        when(employeeService.getEmployeeMap(Collections.singleton(employeeRecord.getEmployeeId())))
                .thenReturn(Collections.singletonMap(employeeRecord.getEmployeeId(), employee));
        HrmSalarySlipSendReqVO reqVO = new HrmSalarySlipSendReqVO();
        reqVO.setMonthRecordId(monthRecord.getId());
        reqVO.setHideEmpty(false);
        reqVO.setAll(true);
        reqVO.setDeptId(employee.getDeptId());

        // 调用
        Long sendRecordId = salarySlipSendRecordService.sendSalarySlip(reqVO);

        // 断言
        assertEquals(1, salarySlipSendRecordMapper.selectById(sendRecordId).getSendEmployeeCount());
        verify(salarySlipService).createSalarySlipList(
                sendRecordId, Collections.singletonList(employeeRecord), template);
    }

    @Test
    public void testDeleteSalarySlipSendRecord_success() {
        // mock 数据
        HrmSalarySlipSendRecordDO sendRecord = HrmSalarySlipSendRecordDO.builder()
                .monthRecordId(1001L).employeeCount(2).sendEmployeeCount(1)
                .year(2026).month(8).build();
        salarySlipSendRecordMapper.insert(sendRecord);

        // 调用
        salarySlipSendRecordService.deleteSalarySlipSendRecord(sendRecord.getId());

        // 断言
        assertNull(salarySlipSendRecordMapper.selectById(sendRecord.getId()));
        verify(salarySlipService).deleteSalarySlipListBySendRecordId(sendRecord.getId());
    }

    @Test
    public void testGetSalarySlipSendRecordPage_success() {
        // mock 数据
        HrmSalarySlipSendRecordDO matchedRecord = HrmSalarySlipSendRecordDO.builder()
                .monthRecordId(1001L).employeeCount(2).sendEmployeeCount(1)
                .year(2026).month(8).build();
        salarySlipSendRecordMapper.insert(matchedRecord);
        salarySlipSendRecordMapper.insert(HrmSalarySlipSendRecordDO.builder()
                .monthRecordId(1002L).employeeCount(2).sendEmployeeCount(2)
                .year(2026).month(9).build());
        HrmSalarySlipSendRecordPageReqVO reqVO = new HrmSalarySlipSendRecordPageReqVO();
        reqVO.setYear(2026);
        reqVO.setMonth(8);

        // 调用
        PageResult<HrmSalarySlipSendRecordDO> pageResult =
                salarySlipSendRecordService.getSalarySlipSendRecordPage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(matchedRecord.getId(), pageResult.getList().get(0).getId());
    }

    @Test
    public void testGetSalarySlipSendEmployeePage_monthRecordNotExists() {
        // 准备参数
        HrmSalarySlipSendEmployeeReqVO reqVO = new HrmSalarySlipSendEmployeeReqVO();
        reqVO.setMonthRecordId(1001L);

        // 调用
        PageResult<HrmSalaryMonthEmployeeRecordDO> pageResult =
                salarySlipSendRecordService.getSalarySlipSendEmployeePage(reqVO);

        // 断言
        assertEquals(0L, pageResult.getTotal());
    }

    // ========== 随机对象 ==========

    private HrmSalaryMonthRecordDO createMonthRecord(Integer year, Integer month, Integer employeeCount) {
        return randomPojo(HrmSalaryMonthRecordDO.class, o -> {
            o.setId((long) (year * 100 + month));
            o.setYear(year).setMonth(month).setEmployeeCount(employeeCount);
            o.setStatus(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus());
        });
    }

    private HrmSalaryMonthEmployeeRecordDO createMonthEmployeeRecord(Long monthRecordId, Long employeeId) {
        return randomPojo(HrmSalaryMonthEmployeeRecordDO.class, o -> {
            o.setId(employeeId + 10000L).setMonthRecordId(monthRecordId).setEmployeeId(employeeId);
            o.setYear(2026).setMonth(8).setRealPaySalary(new BigDecimal("7910.00"));
        });
    }

}
