package cn.iocoder.yudao.module.hrm.service.insurance.monthrecord;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeProjectUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordCreateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee.HrmInsuranceEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO.Project;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceSchemeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.employee.HrmInsuranceEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_EMPLOYEE_SCHEME_NOT_CONFIGURED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_EMPLOYEE_NOT_ELIGIBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmInsuranceMonthEmployeeRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmInsuranceMonthEmployeeRecordServiceImpl.class)
public class HrmInsuranceMonthEmployeeRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmInsuranceMonthEmployeeRecordServiceImpl monthEmployeeRecordService;

    @Resource
    private HrmInsuranceMonthEmployeeRecordMapper monthEmployeeRecordMapper;

    @MockitoBean
    private HrmInsuranceMonthRecordService monthRecordService;
    @MockitoBean
    private HrmInsuranceSchemeService insuranceSchemeService;
    @MockitoBean
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @Test
    public void testCreateMonthEmployeeRecordList_success() {
        // mock 数据
        Long employeeId = 1001L;
        Long schemeId = 2001L;
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO()
                .setId(3001L).setYear(2026).setMonth(7);
        HrmInsuranceEmployeeInfoDO insuranceEmployeeInfo = new HrmInsuranceEmployeeInfoDO()
                .setEmployeeId(employeeId).setSchemeId(schemeId)
                .setSocialSecurityStartMonth(LocalDateTime.of(2026, 1, 1, 0, 0));
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(employeeId).setName("张三").setUserId(5001L)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        HrmInsuranceSchemeDO scheme = new HrmInsuranceSchemeDO().setId(schemeId)
                .setType(HrmInsuranceSchemeTypeEnum.PROPORTION.getType());
        List<HrmInsuranceSchemeProjectDO> projects = Arrays.asList(
                project(4001L, 1, BigDecimal.valueOf(10000), BigDecimal.valueOf(16), BigDecimal.valueOf(8)),
                project(4002L, 10, BigDecimal.valueOf(10000), BigDecimal.valueOf(7), BigDecimal.valueOf(7)));
        when(insuranceEmployeeInfoService.getInsuranceEmployeeInfoList())
                .thenReturn(Collections.singletonList(insuranceEmployeeInfo));
        when(insuranceSchemeService.getSchemeListByIds(anyCollection()))
                .thenReturn(Collections.singletonList(scheme));
        when(insuranceSchemeService.getSchemeProjectListMap(anyCollection()))
                .thenReturn(Collections.singletonMap(schemeId, projects));
        when(employeeService.getEmployeeMap(anyCollection()))
                .thenReturn(Collections.singletonMap(employeeId, employee));

        // 调用
        monthEmployeeRecordService.createMonthEmployeeRecordList(monthRecord);

        // 断言
        HrmInsuranceMonthEmployeeRecordDO employeeRecord =
                monthEmployeeRecordMapper.selectByMonthRecordIdAndEmployeeId(monthRecord.getId(), employeeId);
        assertEquals(0, BigDecimal.valueOf(800).compareTo(employeeRecord.getPersonalInsuranceAmount()));
        assertEquals(0, BigDecimal.valueOf(700).compareTo(employeeRecord.getPersonalProvidentFundAmount()));
        assertEquals(0, BigDecimal.valueOf(1600).compareTo(employeeRecord.getCorporateInsuranceAmount()));
        verify(insuranceEmployeeInfoService).updateSocialSecurityStartMonthIfAbsent(
                employeeId, LocalDateTime.of(2026, 7, 1, 0, 0));
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testCreateMonthEmployeeRecordList_leftInTargetMonth() {
        // mock 数据
        Long employeeId = 1001L;
        Long schemeId = 2001L;
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO()
                .setId(3001L).setYear(2026).setMonth(7);
        HrmInsuranceEmployeeInfoDO insuranceEmployeeInfo = new HrmInsuranceEmployeeInfoDO()
                .setEmployeeId(employeeId).setSchemeId(schemeId);
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(employeeId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setEntryTime(LocalDateTime.of(2025, 1, 1, 0, 0))
                .setLeaveTime(LocalDateTime.of(2026, 7, 20, 0, 0));
        HrmInsuranceSchemeDO scheme = new HrmInsuranceSchemeDO().setId(schemeId)
                .setType(HrmInsuranceSchemeTypeEnum.AMOUNT.getType());
        when(insuranceEmployeeInfoService.getInsuranceEmployeeInfoList())
                .thenReturn(Collections.singletonList(insuranceEmployeeInfo));
        when(insuranceSchemeService.getSchemeListByIds(anyCollection()))
                .thenReturn(Collections.singletonList(scheme));
        when(insuranceSchemeService.getSchemeProjectListMap(anyCollection()))
                .thenReturn(Collections.singletonMap(schemeId, Collections.singletonList(
                        project(4001L, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))));
        when(employeeService.getEmployeeMap(anyCollection()))
                .thenReturn(Collections.singletonMap(employeeId, employee));

        // 调用
        monthEmployeeRecordService.createMonthEmployeeRecordList(monthRecord);

        // 断言
        assertNotNull(monthEmployeeRecordMapper.selectByMonthRecordIdAndEmployeeId(monthRecord.getId(), employeeId));
    }

    @Test
    public void testUpdateMonthEmployeeRecord_success() {
        // mock 数据
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = randomEmployeeRecord(3001L, 1001L);
        monthEmployeeRecordMapper.insert(employeeRecord);
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO().setId(3001L);
        HrmInsuranceSchemeDO scheme = new HrmInsuranceSchemeDO().setId(2001L)
                .setType(HrmInsuranceSchemeTypeEnum.AMOUNT.getType());
        HrmInsuranceSchemeProjectDO schemeProject = project(
                4001L, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(monthRecordService.validateMonthRecordEditableForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(insuranceSchemeService.validateSchemeExists(scheme.getId())).thenReturn(scheme);
        when(insuranceSchemeService.getSchemeProjectList(scheme.getId()))
                .thenReturn(Collections.singletonList(schemeProject));
        when(employeeService.getEmployee(employeeRecord.getEmployeeId())).thenReturn(new HrmEmployeeDO()
                .setId(employeeRecord.getEmployeeId()).setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
        // 准备参数
        HrmInsuranceMonthEmployeeRecordUpdateReqVO reqVO =
                new HrmInsuranceMonthEmployeeRecordUpdateReqVO()
                        .setId(employeeRecord.getId()).setSchemeId(scheme.getId())
                        .setProjects(Collections.singletonList(
                                new HrmInsuranceMonthEmployeeProjectUpdateReqVO()
                                        .setSchemeProjectId(schemeProject.getId())
                                        .setCorporateAmount(BigDecimal.valueOf(900))
                                        .setPersonalAmount(BigDecimal.valueOf(300))));

        // 调用
        monthEmployeeRecordService.updateMonthEmployeeRecord(reqVO);

        // 断言
        HrmInsuranceMonthEmployeeRecordDO dbEmployeeRecord =
                monthEmployeeRecordMapper.selectById(employeeRecord.getId());
        assertEquals(scheme.getId(), dbEmployeeRecord.getSchemeId());
        assertEquals(0, BigDecimal.valueOf(900).compareTo(dbEmployeeRecord.getCorporateInsuranceAmount()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(dbEmployeeRecord.getPersonalInsuranceAmount()));
        verify(insuranceEmployeeInfoService).updateEmployeeScheme(employeeRecord.getEmployeeId(), scheme.getId());
        verify(monthRecordService).updateMonthRecordSummary(monthRecord.getId());
    }

    @Test
    public void testStopMonthEmployeeRecordList_success() {
        // mock 数据
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = randomEmployeeRecord(3001L, 1001L);
        monthEmployeeRecordMapper.insert(employeeRecord);
        when(monthRecordService.validateMonthRecordEditableForUpdate(employeeRecord.getMonthRecordId()))
                .thenReturn(new HrmInsuranceMonthRecordDO().setId(employeeRecord.getMonthRecordId()));

        // 调用
        monthEmployeeRecordService.stopMonthEmployeeRecordList(
                Collections.singletonList(employeeRecord.getId()));

        // 断言
        assertEquals(HrmInsuranceEmployeeStatusEnum.STOPPED.getStatus(),
                monthEmployeeRecordMapper.selectById(employeeRecord.getId()).getStatus());
        verify(monthRecordService).updateMonthRecordSummary(employeeRecord.getMonthRecordId());
    }

    @Test
    public void testGetMonthEmployeeRecordPage_filters() {
        // mock 数据
        HrmInsuranceMonthEmployeeRecordDO matchedRecord = randomEmployeeRecord(3001L, 1001L);
        matchedRecord.setSchemeId(2001L);
        monthEmployeeRecordMapper.insert(matchedRecord);
        HrmInsuranceMonthEmployeeRecordDO anotherRecord = randomEmployeeRecord(3001L, 1002L);
        anotherRecord.setSchemeId(2002L);
        monthEmployeeRecordMapper.insert(anotherRecord);
        when(employeeService.getEmployeeList(any(HrmEmployeeListReqVO.class)))
                .thenReturn(Collections.singletonList(new HrmEmployeeDO().setId(matchedRecord.getEmployeeId())));
        when(insuranceSchemeService.getSchemeListByAreaId(440300))
                .thenReturn(Collections.singletonList(new HrmInsuranceSchemeDO().setId(matchedRecord.getSchemeId())));
        // 准备参数
        HrmInsuranceMonthEmployeeRecordPageReqVO reqVO =
                new HrmInsuranceMonthEmployeeRecordPageReqVO()
                        .setMonthRecordId(matchedRecord.getMonthRecordId())
                        .setEmployeeName("张").setAreaId(440300);

        // 调用
        PageResult<HrmInsuranceMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getMonthEmployeeRecordPage(reqVO);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(matchedRecord.getId(), pageResult.getList().get(0).getId());
    }

    @Test
    public void testGetMonthEmployeeRecordPage_employeeNotFound() {
        // mock 方法
        when(employeeService.getEmployeeList(any(HrmEmployeeListReqVO.class)))
                .thenReturn(Collections.emptyList());
        // 准备参数
        HrmInsuranceMonthEmployeeRecordPageReqVO reqVO =
                new HrmInsuranceMonthEmployeeRecordPageReqVO()
                        .setMonthRecordId(3001L).setEmployeeName("不存在");

        // 调用
        PageResult<HrmInsuranceMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getMonthEmployeeRecordPage(reqVO);

        // 断言
        assertEquals(0, pageResult.getTotal());
    }

    @Test
    public void testGetUninsuredEmployeeList() {
        // mock 数据
        Long monthRecordId = 3001L;
        HrmInsuranceMonthRecordDO monthRecord =
                new HrmInsuranceMonthRecordDO().setId(monthRecordId).setYear(2026).setMonth(7);
        HrmEmployeeDO activeEmployee = new HrmEmployeeDO().setId(1001L)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setEntryTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        HrmEmployeeDO pendingEmployee = new HrmEmployeeDO().setId(1002L)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());
        when(monthRecordService.getMonthRecord(monthRecordId)).thenReturn(monthRecord);
        when(employeeService.getEmployeeList(any(HrmEmployeeListReqVO.class)))
                .thenReturn(Arrays.asList(activeEmployee, pendingEmployee));
        HrmInsuranceEmployeeInfoDO employeeInfo = new HrmInsuranceEmployeeInfoDO()
                .setEmployeeId(activeEmployee.getId()).setSchemeId(2001L);
        when(insuranceEmployeeInfoService.getInsuranceEmployeeInfoMap(anyCollection()))
                .thenReturn(Collections.singletonMap(activeEmployee.getId(), employeeInfo));
        when(insuranceSchemeService.getSchemeListByIds(anyCollection()))
                .thenReturn(Collections.singletonList(new HrmInsuranceSchemeDO().setId(2001L)));

        // 调用
        List<HrmEmployeeDO> employees =
                monthEmployeeRecordService.getUninsuredEmployeeList(monthRecordId);

        // 断言
        assertEquals(1, employees.size());
        assertEquals(activeEmployee.getId(), employees.get(0).getId());
    }

    @Test
    public void testGetUninsuredEmployeeList_monthRecordNotExists() {
        // 调用
        List<HrmEmployeeDO> employees = monthEmployeeRecordService.getUninsuredEmployeeList(3001L);

        // 断言
        assertTrue(employees.isEmpty());
    }

    @Test
    public void testCreateMonthEmployeeRecordList_schemeNotConfigured() {
        // mock 数据
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO()
                .setId(3001L).setYear(2026).setMonth(7);
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(1001L)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setEntryTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(monthRecordService.validateMonthRecordEditableForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(employeeService.getEmployeeMap(anyCollection()))
                .thenReturn(Collections.singletonMap(employee.getId(), employee));
        // 准备参数
        HrmInsuranceMonthEmployeeRecordCreateListReqVO reqVO =
                new HrmInsuranceMonthEmployeeRecordCreateListReqVO()
                        .setMonthRecordId(monthRecord.getId())
                        .setEmployeeIds(Collections.singletonList(employee.getId()));

        // 调用，并断言异常
        assertServiceException(() -> monthEmployeeRecordService.createMonthEmployeeRecordList(reqVO),
                INSURANCE_EMPLOYEE_SCHEME_NOT_CONFIGURED);
    }

    @Test
    public void testCreateMonthEmployeeRecordList_futureEntryIllegal() {
        // mock 数据
        Long employeeId = 1001L;
        Long schemeId = 2001L;
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO()
                .setId(3001L).setYear(2026).setMonth(7);
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(employeeId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setEntryTime(LocalDateTime.of(2026, 8, 1, 0, 0));
        HrmInsuranceEmployeeInfoDO employeeInfo = new HrmInsuranceEmployeeInfoDO()
                .setEmployeeId(employeeId).setSchemeId(schemeId);
        when(monthRecordService.validateMonthRecordEditableForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(employeeService.getEmployeeMap(anyCollection()))
                .thenReturn(Collections.singletonMap(employeeId, employee));
        when(insuranceEmployeeInfoService.getInsuranceEmployeeInfoMap(anyCollection()))
                .thenReturn(Collections.singletonMap(employeeId, employeeInfo));
        when(insuranceSchemeService.getSchemeListByIds(anyCollection()))
                .thenReturn(Collections.singletonList(new HrmInsuranceSchemeDO().setId(schemeId)));
        when(insuranceSchemeService.getSchemeProjectListMap(anyCollection()))
                .thenReturn(Collections.singletonMap(schemeId, Collections.singletonList(
                        project(4001L, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))));
        // 准备参数
        HrmInsuranceMonthEmployeeRecordCreateListReqVO reqVO =
                new HrmInsuranceMonthEmployeeRecordCreateListReqVO()
                        .setMonthRecordId(monthRecord.getId())
                        .setEmployeeIds(Collections.singletonList(employeeId));

        // 调用，并断言异常
        assertServiceException(() -> monthEmployeeRecordService.createMonthEmployeeRecordList(reqVO),
                INSURANCE_MONTH_EMPLOYEE_NOT_ELIGIBLE);
    }

    @Test
    public void testUpdateMonthEmployeeRecord_historicalLeftEmployee() {
        // mock 数据
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = randomEmployeeRecord(3001L, 1001L);
        monthEmployeeRecordMapper.insert(employeeRecord);
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO()
                .setId(3001L).setYear(2026).setMonth(7);
        HrmInsuranceSchemeDO scheme = new HrmInsuranceSchemeDO().setId(2001L)
                .setType(HrmInsuranceSchemeTypeEnum.AMOUNT.getType());
        HrmInsuranceSchemeProjectDO schemeProject = project(
                4001L, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(monthRecordService.validateMonthRecordEditableForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(insuranceSchemeService.validateSchemeExists(scheme.getId())).thenReturn(scheme);
        when(insuranceSchemeService.getSchemeProjectList(scheme.getId()))
                .thenReturn(Collections.singletonList(schemeProject));
        when(employeeService.getEmployee(employeeRecord.getEmployeeId())).thenReturn(new HrmEmployeeDO()
                .setId(employeeRecord.getEmployeeId()).setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setLeaveTime(LocalDateTime.of(2026, 7, 20, 0, 0)));
        // 准备参数
        HrmInsuranceMonthEmployeeRecordUpdateReqVO reqVO = new HrmInsuranceMonthEmployeeRecordUpdateReqVO()
                .setId(employeeRecord.getId()).setSchemeId(scheme.getId())
                .setProjects(Collections.singletonList(new HrmInsuranceMonthEmployeeProjectUpdateReqVO()
                        .setSchemeProjectId(schemeProject.getId())
                        .setCorporateAmount(BigDecimal.valueOf(900))
                        .setPersonalAmount(BigDecimal.valueOf(300))));

        // 调用
        monthEmployeeRecordService.updateMonthEmployeeRecord(reqVO);

        // 断言
        assertEquals(0, BigDecimal.valueOf(300).compareTo(monthEmployeeRecordMapper
                .selectById(employeeRecord.getId()).getPersonalInsuranceAmount()));
    }

    @Test
    public void testGetNormalMonthEmployeeRecordMap() {
        // mock 数据
        HrmInsuranceMonthEmployeeRecordDO normalRecord = randomEmployeeRecord(3001L, 1001L);
        monthEmployeeRecordMapper.insert(normalRecord);
        HrmInsuranceMonthEmployeeRecordDO stoppedRecord = randomEmployeeRecord(3001L, 1002L)
                .setStatus(HrmInsuranceEmployeeStatusEnum.STOPPED.getStatus());
        monthEmployeeRecordMapper.insert(stoppedRecord);

        // 调用
        Map<Long, HrmInsuranceMonthEmployeeRecordDO> recordMap =
                monthEmployeeRecordService.getNormalMonthEmployeeRecordMap(2026, 7);

        // 断言
        assertEquals(1, recordMap.size());
        assertTrue(recordMap.containsKey(normalRecord.getEmployeeId()));
    }

    @Test
    public void testUpdateInsuranceMonthEmployeeRecordSchemeIdBySchemeId_keepsSnapshot() {
        // mock 数据
        Long oldSchemeId = 2001L;
        List<Project> projects = Collections.singletonList(Project.builder()
                .schemeProjectId(4001L).type(1).name("养老保险")
                .baseAmount(BigDecimal.valueOf(10000))
                .corporateAmount(BigDecimal.valueOf(1600))
                .personalAmount(BigDecimal.valueOf(800)).build());
        HrmInsuranceMonthEmployeeRecordDO migratedRecord = randomEmployeeRecord(3001L, 1001L)
                .setSchemeId(oldSchemeId).setProjects(projects);
        monthEmployeeRecordMapper.insert(migratedRecord);
        HrmInsuranceMonthEmployeeRecordDO retainedRecord = randomEmployeeRecord(3001L, 1002L)
                .setSchemeId(2002L);
        monthEmployeeRecordMapper.insert(retainedRecord);
        // 准备参数
        Long newSchemeId = 2003L;

        // 调用
        monthEmployeeRecordService.updateInsuranceMonthEmployeeRecordSchemeIdBySchemeId(
                oldSchemeId, newSchemeId);

        // 断言
        HrmInsuranceMonthEmployeeRecordDO dbMigratedRecord =
                monthEmployeeRecordMapper.selectById(migratedRecord.getId());
        assertEquals(newSchemeId, dbMigratedRecord.getSchemeId());
        assertEquals(projects, dbMigratedRecord.getProjects());
        assertEquals(0, migratedRecord.getPersonalInsuranceAmount()
                .compareTo(dbMigratedRecord.getPersonalInsuranceAmount()));
        assertEquals(0, migratedRecord.getCorporateInsuranceAmount()
                .compareTo(dbMigratedRecord.getCorporateInsuranceAmount()));
        assertEquals(retainedRecord.getSchemeId(),
                monthEmployeeRecordMapper.selectById(retainedRecord.getId()).getSchemeId());
    }

    @Test
    public void testUpdateMonthEmployeeRecord_afterSchemeVersioning() {
        // mock 数据
        Long oldSchemeId = 2001L;
        Long newSchemeId = 2002L;
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = randomEmployeeRecord(3001L, 1001L)
                .setSchemeId(oldSchemeId)
                .setProjects(Collections.singletonList(Project.builder()
                        .schemeProjectId(4001L).type(1).name("养老保险")
                        .corporateAmount(BigDecimal.valueOf(600))
                        .personalAmount(BigDecimal.valueOf(300)).build()));
        monthEmployeeRecordMapper.insert(employeeRecord);
        monthEmployeeRecordService.updateInsuranceMonthEmployeeRecordSchemeIdBySchemeId(
                oldSchemeId, newSchemeId);
        HrmInsuranceMonthRecordDO monthRecord = new HrmInsuranceMonthRecordDO().setId(3001L);
        HrmInsuranceSchemeDO scheme = new HrmInsuranceSchemeDO().setId(newSchemeId)
                .setType(HrmInsuranceSchemeTypeEnum.AMOUNT.getType());
        when(monthRecordService.validateMonthRecordEditableForUpdate(monthRecord.getId())).thenReturn(monthRecord);
        when(insuranceSchemeService.validateSchemeExists(newSchemeId)).thenReturn(scheme);
        when(employeeService.getEmployee(employeeRecord.getEmployeeId())).thenReturn(new HrmEmployeeDO()
                .setId(employeeRecord.getEmployeeId()).setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
        // 准备参数
        HrmInsuranceMonthEmployeeRecordUpdateReqVO reqVO =
                new HrmInsuranceMonthEmployeeRecordUpdateReqVO()
                        .setId(employeeRecord.getId()).setSchemeId(newSchemeId)
                        .setProjects(Collections.singletonList(
                                new HrmInsuranceMonthEmployeeProjectUpdateReqVO()
                                        .setSchemeProjectId(4001L)
                                        .setCorporateAmount(BigDecimal.valueOf(900))
                                        .setPersonalAmount(BigDecimal.valueOf(400))));

        // 调用
        monthEmployeeRecordService.updateMonthEmployeeRecord(reqVO);

        // 断言
        HrmInsuranceMonthEmployeeRecordDO dbEmployeeRecord =
                monthEmployeeRecordMapper.selectById(employeeRecord.getId());
        Project project = CollUtil.getFirst(dbEmployeeRecord.getProjects());
        assertEquals(4001L, project.getSchemeProjectId());
        assertEquals(0, BigDecimal.valueOf(900).compareTo(
                project.getCorporateAmount()));
        assertEquals(0, BigDecimal.valueOf(400).compareTo(
                project.getPersonalAmount()));
    }

    // ========== 随机对象 ==========

    private HrmInsuranceMonthEmployeeRecordDO randomEmployeeRecord(Long monthRecordId, Long employeeId) {
        return randomPojo(HrmInsuranceMonthEmployeeRecordDO.class, record -> record
                .setId(null).setMonthRecordId(monthRecordId).setEmployeeId(employeeId).setSchemeId(2001L)
                .setYear(2026).setMonth(7).setStatus(HrmInsuranceEmployeeStatusEnum.NORMAL.getStatus())
                .setPersonalInsuranceAmount(BigDecimal.valueOf(100))
                .setPersonalProvidentFundAmount(BigDecimal.valueOf(50))
                .setCorporateInsuranceAmount(BigDecimal.valueOf(200))
                .setCorporateProvidentFundAmount(BigDecimal.valueOf(50))
                .setProjects(Collections.emptyList()));
    }

    private HrmInsuranceSchemeProjectDO project(Long id, Integer type, BigDecimal baseAmount,
                                                BigDecimal corporateRate, BigDecimal personalRate) {
        return new HrmInsuranceSchemeProjectDO().setId(id).setSchemeId(2001L)
                .setType(type).setName("参保项目")
                .setBaseAmount(baseAmount)
                .setCorporateRate(corporateRate).setPersonalRate(personalRate)
                .setCorporateAmount(BigDecimal.ZERO).setPersonalAmount(BigDecimal.ZERO);
    }

}
