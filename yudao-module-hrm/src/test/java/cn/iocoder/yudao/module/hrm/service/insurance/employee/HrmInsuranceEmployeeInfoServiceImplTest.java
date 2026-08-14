package cn.iocoder.yudao.module.hrm.service.insurance.employee;

import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.employeeinfo.HrmInsuranceEmployeeInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee.HrmInsuranceEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.employee.HrmInsuranceEmployeeInfoMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_EMPLOYEE_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_EMP_STATUS_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmInsuranceEmployeeInfoServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmInsuranceEmployeeInfoServiceImpl.class)
public class HrmInsuranceEmployeeInfoServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmInsuranceEmployeeInfoServiceImpl insuranceEmployeeInfoService;

    @Resource
    private HrmInsuranceEmployeeInfoMapper insuranceEmployeeInfoMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private HrmInsuranceSchemeService insuranceSchemeService;

    @Test
    public void testSaveInsuranceEmployeeInfo_create() {
        // 准备参数
        Long employeeId = randomLongId();
        Long schemeId = randomLongId();
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId);
        HrmInsuranceSchemeDO scheme = randomPojo(HrmInsuranceSchemeDO.class, o -> o.setId(schemeId));
        HrmInsuranceEmployeeInfoSaveReqVO reqVO = randomInsuranceEmployeeInfoReqVO(employeeId, schemeId);
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);
        when(insuranceSchemeService.validateSchemeExists(schemeId)).thenReturn(scheme);

        // 调用
        Long id = insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(reqVO);

        // 断言
        assertNotNull(id);
        assertPojoEquals(reqVO, insuranceEmployeeInfoMapper.selectById(id), "id");
        verify(employeeService).validateEmployeeExistsForUpdate(employeeId);
        verify(insuranceSchemeService).validateSchemeExists(schemeId);
    }

    @Test
    public void testSaveInsuranceEmployeeInfo_normalizeSocialSecurityStartMonth() {
        // 准备参数
        Long employeeId = randomLongId();
        HrmInsuranceEmployeeInfoSaveReqVO reqVO = randomInsuranceEmployeeInfoReqVO(employeeId, null)
                .setSocialSecurityStartMonth(LocalDateTime.of(2026, 1, 18, 13, 45, 20));
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId))
                .thenReturn(randomEligibleEmployee(employeeId));

        // 调用
        Long id = insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(reqVO);

        // 断言
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), insuranceEmployeeInfoMapper.selectById(id)
                .getSocialSecurityStartMonth());
    }

    @Test
    public void testSaveInsuranceEmployeeInfo_updateByEmployeeId() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmInsuranceEmployeeInfoDO dbInsuranceEmployeeInfo = randomInsuranceEmployeeInfoDO(employeeId);
        insuranceEmployeeInfoMapper.insert(dbInsuranceEmployeeInfo);
        // 准备参数
        HrmInsuranceEmployeeInfoSaveReqVO reqVO = randomInsuranceEmployeeInfoReqVO(
                employeeId, dbInsuranceEmployeeInfo.getSchemeId());
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId))
                .thenReturn(randomEligibleEmployee(employeeId));
        when(insuranceSchemeService.validateSchemeExists(dbInsuranceEmployeeInfo.getSchemeId()))
                .thenReturn(randomPojo(HrmInsuranceSchemeDO.class,
                        o -> o.setId(dbInsuranceEmployeeInfo.getSchemeId())));

        // 调用
        Long id = insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(reqVO);

        // 断言
        assertEquals(dbInsuranceEmployeeInfo.getId(), id);
        assertPojoEquals(reqVO, insuranceEmployeeInfoMapper.selectById(id), "id");
        assertEquals(1L, insuranceEmployeeInfoMapper.selectCount());
    }

    @Test
    public void testSaveInsuranceEmployeeInfo_employeeMismatch() {
        // mock 数据
        HrmInsuranceEmployeeInfoDO dbInsuranceEmployeeInfo = randomInsuranceEmployeeInfoDO(randomLongId());
        insuranceEmployeeInfoMapper.insert(dbInsuranceEmployeeInfo);
        // 准备参数
        Long employeeId = randomLongId();
        HrmInsuranceEmployeeInfoSaveReqVO reqVO = randomInsuranceEmployeeInfoReqVO(employeeId, null);
        reqVO.setId(dbInsuranceEmployeeInfo.getId());
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId))
                .thenReturn(randomEligibleEmployee(employeeId));

        // 调用，并断言异常
        assertServiceException(() -> insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(reqVO),
                EMPLOYEE_RESOURCE_BELONG_INVALID, "参保信息");
    }

    @Test
    public void testSaveInsuranceEmployeeInfo_leftEmployeeKeepsScheme() {
        // mock 数据
        Long employeeId = randomLongId();
        Long schemeId = randomLongId();
        HrmInsuranceEmployeeInfoDO oldEmployeeInfo = randomInsuranceEmployeeInfoDO(employeeId)
                .setSchemeId(schemeId);
        insuranceEmployeeInfoMapper.insert(oldEmployeeInfo);
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus());
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);
        // 准备参数
        HrmInsuranceEmployeeInfoSaveReqVO reqVO = randomInsuranceEmployeeInfoReqVO(employeeId, schemeId)
                .setId(oldEmployeeInfo.getId()).setSocialSecurityNumber("SB-UPDATED");

        // 调用
        insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(reqVO);

        // 断言
        assertEquals("SB-UPDATED", insuranceEmployeeInfoMapper.selectById(oldEmployeeInfo.getId())
                .getSocialSecurityNumber());
    }

    @Test
    public void testSaveInsuranceEmployeeInfo_leftEmployeeChangesSchemeIllegal() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmInsuranceEmployeeInfoDO oldEmployeeInfo = randomInsuranceEmployeeInfoDO(employeeId)
                .setSchemeId(randomLongId());
        insuranceEmployeeInfoMapper.insert(oldEmployeeInfo);
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus());
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);
        // 准备参数
        HrmInsuranceEmployeeInfoSaveReqVO reqVO = randomInsuranceEmployeeInfoReqVO(
                employeeId, randomLongId()).setId(oldEmployeeInfo.getId());

        // 调用，并断言异常
        assertServiceException(() -> insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(reqVO),
                INSURANCE_EMP_STATUS_ILLEGAL);
    }

    @Test
    public void testGetInsuranceEmployeeInfoByEmployeeId_returnsLatest() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmInsuranceEmployeeInfoDO firstEmployeeInfo = randomInsuranceEmployeeInfoDO(employeeId)
                .setSocialSecurityNumber("SB-OLD");
        insuranceEmployeeInfoMapper.insert(firstEmployeeInfo);
        HrmInsuranceEmployeeInfoDO lastEmployeeInfo = randomInsuranceEmployeeInfoDO(employeeId)
                .setSocialSecurityNumber("SB-NEW");
        insuranceEmployeeInfoMapper.insert(lastEmployeeInfo);

        // 调用
        HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoService
                .getInsuranceEmployeeInfoByEmployeeId(employeeId);

        // 断言
        assertEquals(lastEmployeeInfo.getId(), employeeInfo.getId());
    }

    @Test
    public void testValidateInsuranceEmployeeExists_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> insuranceEmployeeInfoService.validateInsuranceEmployeeInfoExists(id),
                INSURANCE_EMPLOYEE_INFO_NOT_EXISTS);
    }

    @Test
    public void testUpdateEmployeeScheme_create() {
        // 准备参数
        Long employeeId = randomLongId();
        Long schemeId = randomLongId();
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId);
        HrmInsuranceSchemeDO scheme = randomPojo(
                HrmInsuranceSchemeDO.class, o -> o.setId(schemeId));
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);
        when(insuranceSchemeService.validateSchemeExists(schemeId)).thenReturn(scheme);
        when(insuranceSchemeService.getScheme(schemeId)).thenReturn(scheme);

        // 调用
        insuranceEmployeeInfoService.updateEmployeeScheme(employeeId, schemeId);

        // 断言
        HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoMapper
                .selectByEmployeeId(employeeId);
        assertEquals(schemeId, employeeInfo.getSchemeId());
        assertFalse(employeeInfo.getFirstSocialSecurity());
        assertFalse(employeeInfo.getFirstAccumulationFund());
    }

    @Test
    public void testUpdateEmployeeScheme_update() {
        // mock 数据
        Long employeeId = randomLongId();
        insuranceEmployeeInfoMapper.insert(
                randomInsuranceEmployeeInfoDO(employeeId).setSchemeId(randomLongId()));
        Long schemeId = randomLongId();
        HrmInsuranceSchemeDO scheme = randomPojo(
                HrmInsuranceSchemeDO.class, o -> o.setId(schemeId));
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId))
                .thenReturn(randomEligibleEmployee(employeeId));
        when(insuranceSchemeService.validateSchemeExists(schemeId)).thenReturn(scheme);

        // 调用
        insuranceEmployeeInfoService.updateEmployeeScheme(employeeId, schemeId);

        // 断言
        assertEquals(schemeId, insuranceEmployeeInfoMapper
                .selectByEmployeeId(employeeId).getSchemeId());
    }

    @Test
    public void testUpdateInsuranceEmployeeInfoSchemeIdBySchemeId() {
        // mock 数据
        Long oldSchemeId = randomLongId();
        HrmInsuranceEmployeeInfoDO migratedEmployeeInfo = randomInsuranceEmployeeInfoDO(randomLongId())
                .setSchemeId(oldSchemeId);
        insuranceEmployeeInfoMapper.insert(migratedEmployeeInfo);
        HrmInsuranceEmployeeInfoDO retainedEmployeeInfo = randomInsuranceEmployeeInfoDO(randomLongId())
                .setSchemeId(randomLongId());
        insuranceEmployeeInfoMapper.insert(retainedEmployeeInfo);
        // 准备参数
        Long newSchemeId = randomLongId();

        // 调用
        insuranceEmployeeInfoService.updateInsuranceEmployeeInfoSchemeIdBySchemeId(
                oldSchemeId, newSchemeId);

        // 断言
        assertEquals(newSchemeId, insuranceEmployeeInfoMapper.selectById(migratedEmployeeInfo.getId())
                .getSchemeId());
        assertEquals(retainedEmployeeInfo.getSchemeId(),
                insuranceEmployeeInfoMapper.selectById(retainedEmployeeInfo.getId()).getSchemeId());
    }

    @Test
    public void testUpdateEmployeeScheme_employeeStatusIllegal() {
        // 准备参数
        Long employeeId = randomLongId();
        Long schemeId = randomLongId();
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId)
                .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus());
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);

        // 调用，并断言异常
        assertServiceException(() -> insuranceEmployeeInfoService.updateEmployeeScheme(employeeId, schemeId),
                INSURANCE_EMP_STATUS_ILLEGAL);
    }

    @Test
    public void testUpdateEmployeeScheme_pendingEntryIllegal() {
        // 准备参数
        Long employeeId = randomLongId();
        Long schemeId = randomLongId();
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus());
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);

        // 调用，并断言异常
        assertServiceException(() -> insuranceEmployeeInfoService.updateEmployeeScheme(employeeId, schemeId),
                INSURANCE_EMP_STATUS_ILLEGAL);
    }

    @Test
    public void testUpdateEmployeeScheme_leftIllegal() {
        // 准备参数
        Long employeeId = randomLongId();
        Long schemeId = randomLongId();
        HrmEmployeeDO employee = randomEligibleEmployee(employeeId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus());
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId)).thenReturn(employee);

        // 调用，并断言异常
        assertServiceException(() -> insuranceEmployeeInfoService.updateEmployeeScheme(employeeId, schemeId),
                INSURANCE_EMP_STATUS_ILLEGAL);
    }

    @Test
    public void testUpdateSocialSecurityStartMonthIfAbsent() {
        // 准备参数
        Long employeeId = randomLongId();
        LocalDateTime startMonth = LocalDateTime.of(2026, 1, 18, 13, 45, 20);
        // mock 方法
        when(employeeService.validateEmployeeExistsForUpdate(employeeId))
                .thenReturn(randomEligibleEmployee(employeeId));

        // 调用
        insuranceEmployeeInfoService.updateSocialSecurityStartMonthIfAbsent(employeeId, startMonth);
        insuranceEmployeeInfoService.updateSocialSecurityStartMonthIfAbsent(employeeId, startMonth.plusMonths(1));

        // 断言
        HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoMapper
                .selectByEmployeeId(employeeId);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), employeeInfo.getSocialSecurityStartMonth());
        assertFalse(employeeInfo.getFirstSocialSecurity());
        assertFalse(employeeInfo.getFirstAccumulationFund());
    }

    @Test
    public void testGetInsuranceEmployeeInfoMap() {
        // mock 数据
        Long firstSchemeId = randomLongId();
        Long secondSchemeId = randomLongId();
        HrmInsuranceEmployeeInfoDO firstInsuranceEmployeeInfo = randomInsuranceEmployeeInfoDO(randomLongId())
                .setSchemeId(firstSchemeId);
        insuranceEmployeeInfoMapper.insert(firstInsuranceEmployeeInfo);
        HrmInsuranceEmployeeInfoDO secondInsuranceEmployeeInfo = randomInsuranceEmployeeInfoDO(randomLongId())
                .setSchemeId(secondSchemeId);
        insuranceEmployeeInfoMapper.insert(secondInsuranceEmployeeInfo);
        insuranceEmployeeInfoMapper.insert(randomInsuranceEmployeeInfoDO(randomLongId()).setSchemeId(firstSchemeId));

        // 调用
        Map<Long, HrmInsuranceEmployeeInfoDO> insuranceEmployeeInfoMap = insuranceEmployeeInfoService
                .getInsuranceEmployeeInfoMap(Arrays.asList(firstInsuranceEmployeeInfo.getEmployeeId(),
                        secondInsuranceEmployeeInfo.getEmployeeId()));
        // 断言
        assertEquals(2, insuranceEmployeeInfoMap.size());
        assertTrue(insuranceEmployeeInfoService
                .getInsuranceEmployeeInfoMap(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testGetInsuranceEmployeeInfoCountMapBySchemeIds() {
        // mock 数据
        Long firstSchemeId = randomLongId();
        Long secondSchemeId = randomLongId();
        HrmInsuranceEmployeeInfoDO firstEmployeeInfo =
                randomInsuranceEmployeeInfoDO(randomLongId()).setSchemeId(firstSchemeId);
        insuranceEmployeeInfoMapper.insert(firstEmployeeInfo);
        HrmInsuranceEmployeeInfoDO secondEmployeeInfo =
                randomInsuranceEmployeeInfoDO(randomLongId()).setSchemeId(secondSchemeId);
        insuranceEmployeeInfoMapper.insert(secondEmployeeInfo);
        HrmInsuranceEmployeeInfoDO thirdEmployeeInfo =
                randomInsuranceEmployeeInfoDO(randomLongId()).setSchemeId(firstSchemeId);
        insuranceEmployeeInfoMapper.insert(thirdEmployeeInfo);
        Map<Long, HrmEmployeeDO> employeeMap = new HashMap<>();
        employeeMap.put(firstEmployeeInfo.getEmployeeId(), randomEligibleEmployee(firstEmployeeInfo.getEmployeeId()));
        employeeMap.put(secondEmployeeInfo.getEmployeeId(), randomEligibleEmployee(secondEmployeeInfo.getEmployeeId()));
        employeeMap.put(thirdEmployeeInfo.getEmployeeId(), randomEligibleEmployee(thirdEmployeeInfo.getEmployeeId()));
        when(employeeService.getEmployeeMap(org.mockito.ArgumentMatchers.anyCollection())).thenReturn(employeeMap);
        // 准备参数
        List<Long> schemeIds = Arrays.asList(firstSchemeId, secondSchemeId);

        // 调用
        Map<Long, Long> countMap =
                insuranceEmployeeInfoService.getInsuranceEmployeeInfoCountMapBySchemeIds(schemeIds);

        // 断言
        assertEquals(2L, countMap.get(firstSchemeId));
        assertEquals(1L, countMap.get(secondSchemeId));
        assertTrue(insuranceEmployeeInfoService
                .getInsuranceEmployeeInfoCountMapBySchemeIds(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testGetInsuranceEmployeeInfoCountMap_ignoresDeletedEmployee() {
        // mock 数据
        Long schemeId = randomLongId();
        HrmInsuranceEmployeeInfoDO employeeInfo = randomInsuranceEmployeeInfoDO(randomLongId())
                .setSchemeId(schemeId);
        insuranceEmployeeInfoMapper.insert(employeeInfo);
        when(employeeService.getEmployeeMap(org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(Collections.emptyMap());

        // 调用
        Map<Long, Long> countMap = insuranceEmployeeInfoService
                .getInsuranceEmployeeInfoCountMapBySchemeIds(Collections.singleton(schemeId));

        // 断言
        assertEquals(0L, countMap.getOrDefault(schemeId, 0L));
    }

    // ========== 随机对象 ==========

    private static HrmInsuranceEmployeeInfoSaveReqVO randomInsuranceEmployeeInfoReqVO(
            Long employeeId, Long schemeId) {
        return randomPojo(HrmInsuranceEmployeeInfoSaveReqVO.class, o -> {
            o.setId(null).setEmployeeId(employeeId).setSchemeId(schemeId);
            o.setFirstSocialSecurity(true).setFirstAccumulationFund(false);
            o.setSocialSecurityStartMonth(LocalDateTime.of(2026, 1, 1, 0, 0));
        });
    }

    private static HrmInsuranceEmployeeInfoDO randomInsuranceEmployeeInfoDO(Long employeeId) {
        return randomPojo(HrmInsuranceEmployeeInfoDO.class, o -> {
            o.setId(null).setEmployeeId(employeeId).setDeleted(false);
            o.setFirstSocialSecurity(true).setFirstAccumulationFund(false);
            o.setSocialSecurityStartMonth(LocalDateTime.of(2026, 1, 1, 0, 0));
        });
    }

    private static HrmEmployeeDO randomEligibleEmployee(Long employeeId) {
        return randomPojo(HrmEmployeeDO.class, o -> o.setId(employeeId)
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
    }

}
