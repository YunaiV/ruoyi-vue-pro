package cn.iocoder.yudao.module.hrm.service.insurance.config;

import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeProjectSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.scheme.HrmInsuranceSchemeSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.config.HrmInsuranceSchemeProjectMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.config.HrmInsuranceSchemeMapper;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceSchemeTypeEnum;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_AREA_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_PROJECT_EMPTY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_PROJECT_TYPE_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_SCHEME_USED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmInsuranceSchemeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmInsuranceSchemeServiceImpl.class)
public class HrmInsuranceSchemeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmInsuranceSchemeServiceImpl insuranceSchemeService;

    @Resource
    private HrmInsuranceSchemeMapper insuranceSchemeMapper;
    @Resource
    private HrmInsuranceSchemeProjectMapper insuranceSchemeProjectMapper;

    @MockitoBean
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @MockitoBean
    private HrmInsuranceMonthEmployeeRecordService insuranceMonthEmployeeRecordService;

    @Test
    public void testCreateScheme_success() {
        // 准备参数
        HrmInsuranceSchemeSaveReqVO reqVO = createProportionSchemeReqVO("深圳标准方案");

        // 调用
        Long schemeId = insuranceSchemeService.createScheme(reqVO);

        // 断言
        assertNotNull(schemeId);
        assertEquals(reqVO.getName(), insuranceSchemeMapper.selectById(schemeId).getName());
        assertEquals(reqVO.getName(), insuranceSchemeService.getScheme(schemeId).getName());
        List<HrmInsuranceSchemeProjectDO> projects =
                insuranceSchemeProjectMapper.selectListBySchemeId(schemeId);
        assertEquals(2, projects.size());
        assertAmount(1600, projects.get(0).getCorporateAmount());
        assertAmount(800, projects.get(0).getPersonalAmount());
    }

    @Test
    public void testUpdateScheme_success() {
        // mock 数据
        Long schemeId = insuranceSchemeService.createScheme(
                createProportionSchemeReqVO("深圳标准方案"));
        List<HrmInsuranceSchemeProjectDO> projects =
                insuranceSchemeProjectMapper.selectListBySchemeId(schemeId);
        Long retainedProjectId = projects.get(0).getId();
        Long deletedProjectId = projects.get(1).getId();
        // 准备参数
        HrmInsuranceSchemeSaveReqVO reqVO = createAmountSchemeReqVO("北京固定金额方案");
        reqVO.setId(schemeId);
        HrmInsuranceSchemeProjectSaveReqVO retainedProject =
                reqVO.getProjectList().get(0).setId(retainedProjectId);
        reqVO.setProjectList(Arrays.asList(retainedProject,
                project(HrmInsuranceProjectTypeEnum.MEDICAL_INSURANCE.getType(),
                        "医疗保险", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.valueOf(500), BigDecimal.valueOf(200))));

        // 调用
        insuranceSchemeService.updateScheme(reqVO);

        // 断言
        assertNull(insuranceSchemeMapper.selectById(schemeId));
        assertNull(insuranceSchemeProjectMapper.selectById(retainedProjectId));
        assertNull(insuranceSchemeProjectMapper.selectById(deletedProjectId));
        HrmInsuranceSchemeDO newScheme = insuranceSchemeMapper.selectByName(reqVO.getName());
        assertNotNull(newScheme);
        assertNotEquals(schemeId, newScheme.getId());
        List<HrmInsuranceSchemeProjectDO> updatedProjects =
                insuranceSchemeProjectMapper.selectListBySchemeId(newScheme.getId());
        assertEquals(2, updatedProjects.size());
        HrmInsuranceSchemeProjectDO amountProject = CollUtil.findOne(updatedProjects,
                project -> HrmInsuranceProjectTypeEnum.PENSION_INSURANCE.getType().equals(project.getType()));
        assertNotNull(amountProject);
        assertNotEquals(retainedProjectId, amountProject.getId());
        assertAmount(900, amountProject.getCorporateAmount());
        assertAmount(300, amountProject.getPersonalAmount());
        HrmInsuranceSchemeProjectDO createdProject = CollUtil.findOne(updatedProjects,
                project -> HrmInsuranceProjectTypeEnum.MEDICAL_INSURANCE.getType().equals(project.getType()));
        assertNotNull(createdProject);
        assertAmount(500, createdProject.getCorporateAmount());
        assertEquals(HrmInsuranceSchemeTypeEnum.AMOUNT.getType(),
                newScheme.getType());
        verify(insuranceEmployeeInfoService).updateInsuranceEmployeeInfoSchemeIdBySchemeId(
                schemeId, newScheme.getId());
        verify(insuranceMonthEmployeeRecordService).updateInsuranceMonthEmployeeRecordSchemeIdBySchemeId(
                schemeId, newScheme.getId());
    }

    @Test
    public void testDeleteScheme_success() {
        // mock 数据
        Long schemeId = insuranceSchemeService.createScheme(
                createProportionSchemeReqVO("深圳标准方案"));

        // 调用
        insuranceSchemeService.deleteScheme(schemeId);

        // 断言
        assertNull(insuranceSchemeMapper.selectById(schemeId));
        assertEquals(0, insuranceSchemeProjectMapper.selectListBySchemeId(schemeId).size());
    }

    @Test
    public void testCreateScheme_nameDuplicate() {
        // mock 数据
        insuranceSchemeService.createScheme(createProportionSchemeReqVO("重复方案"));

        // 调用，并断言异常
        assertServiceException(() -> insuranceSchemeService.createScheme(
                createProportionSchemeReqVO("重复方案")), INSURANCE_SCHEME_NAME_DUPLICATE);
    }

    @Test
    public void testGetSchemeListByAreaId_success() {
        // mock 数据
        insuranceSchemeService.createScheme(createProportionSchemeReqVO("深圳比例方案A"));
        insuranceSchemeService.createScheme(createProportionSchemeReqVO("深圳比例方案B"));
        insuranceSchemeService.createScheme(createAmountSchemeReqVO("北京金额方案"));

        // 调用，并断言异常
        List<HrmInsuranceSchemeDO> schemeList = insuranceSchemeService.getSchemeListByAreaId(440300);
        assertEquals(2, schemeList.size());
        assertEquals(3, insuranceSchemeService.getSchemeList().size());
    }

    @Test
    public void testCreateScheme_onlyProvidentFund() {
        // 准备参数
        HrmInsuranceSchemeSaveReqVO reqVO = createProportionSchemeReqVO("仅公积金方案");
        reqVO.setProjectList(Collections.singletonList(project(
                HrmInsuranceProjectTypeEnum.PROVIDENT_FUND.getType(), "公积金",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7), BigDecimal.valueOf(7),
                BigDecimal.ZERO, BigDecimal.ZERO)));

        // 调用，并断言异常
        assertServiceException(() -> insuranceSchemeService.createScheme(reqVO),
                INSURANCE_SCHEME_PROJECT_EMPTY);
    }

    @Test
    public void testCreateScheme_projectTypeDuplicate() {
        // 准备参数
        HrmInsuranceSchemeSaveReqVO reqVO = createProportionSchemeReqVO("重复项目方案");
        reqVO.setProjectList(Arrays.asList(
                project(HrmInsuranceProjectTypeEnum.PENSION_INSURANCE.getType(), "养老保险",
                        BigDecimal.valueOf(10000), BigDecimal.valueOf(16),
                        BigDecimal.valueOf(8), BigDecimal.ZERO, BigDecimal.ZERO),
                project(HrmInsuranceProjectTypeEnum.PENSION_INSURANCE.getType(), "重复养老保险",
                        BigDecimal.valueOf(8000), BigDecimal.valueOf(12),
                        BigDecimal.valueOf(6), BigDecimal.ZERO, BigDecimal.ZERO)));

        // 调用，并断言异常
        assertServiceException(() -> insuranceSchemeService.createScheme(reqVO),
                INSURANCE_SCHEME_PROJECT_TYPE_DUPLICATE);
    }

    @Test
    public void testCreateScheme_normalizesDistrictToCity() {
        // 准备参数
        HrmInsuranceSchemeSaveReqVO reqVO = createProportionSchemeReqVO("南山标准方案")
                .setAreaId(440305);

        // 调用
        Long schemeId = insuranceSchemeService.createScheme(reqVO);

        // 断言
        assertEquals(440300, insuranceSchemeMapper.selectById(schemeId).getAreaId());
    }

    @Test
    public void testCreateScheme_provinceAreaIllegal() {
        // 准备参数
        HrmInsuranceSchemeSaveReqVO reqVO = createProportionSchemeReqVO("广东标准方案")
                .setAreaId(440000);

        // 调用，并断言异常
        assertServiceException(() -> insuranceSchemeService.createScheme(reqVO),
                INSURANCE_SCHEME_AREA_INVALID);
    }

    @Test
    public void testDeleteScheme_usedByEmployee() {
        // mock 数据
        Long schemeId = insuranceSchemeService.createScheme(createProportionSchemeReqVO("使用中方案"));
        // mock 方法
        when(insuranceEmployeeInfoService.getInsuranceEmployeeInfoCountBySchemeId(schemeId)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> insuranceSchemeService.deleteScheme(schemeId), INSURANCE_SCHEME_USED);
    }

    @Test
    public void testDeleteScheme_usedByMonthRecord() {
        // mock 数据
        Long schemeId = insuranceSchemeService.createScheme(createProportionSchemeReqVO("使用中方案"));
        // mock 方法
        when(insuranceMonthEmployeeRecordService.getMonthEmployeeRecordCountBySchemeId(schemeId)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> insuranceSchemeService.deleteScheme(schemeId), INSURANCE_SCHEME_USED);
    }

    // ========== 随机对象 ==========

    private HrmInsuranceSchemeSaveReqVO createProportionSchemeReqVO(String name) {
        HrmInsuranceSchemeSaveReqVO reqVO = new HrmInsuranceSchemeSaveReqVO();
        reqVO.setName(name).setAreaId(440300).setHouseholdType("深户")
                .setType(HrmInsuranceSchemeTypeEnum.PROPORTION.getType());
        reqVO.setProjectList(Arrays.asList(
                project(1, "养老保险", BigDecimal.valueOf(10000), BigDecimal.valueOf(16), BigDecimal.valueOf(8),
                        BigDecimal.ZERO, BigDecimal.ZERO),
                project(10, "公积金", BigDecimal.valueOf(10000), BigDecimal.valueOf(7), BigDecimal.valueOf(7),
                        BigDecimal.ZERO, BigDecimal.ZERO)));
        return reqVO;
    }

    private HrmInsuranceSchemeSaveReqVO createAmountSchemeReqVO(String name) {
        HrmInsuranceSchemeSaveReqVO reqVO = new HrmInsuranceSchemeSaveReqVO();
        reqVO.setName(name).setAreaId(110100).setHouseholdType("城镇")
                .setType(HrmInsuranceSchemeTypeEnum.AMOUNT.getType());
        reqVO.setProjectList(Collections.singletonList(project(1, "养老保险", BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(900), BigDecimal.valueOf(300))));
        return reqVO;
    }

    private static HrmInsuranceSchemeProjectSaveReqVO project(
            Integer type, String name, BigDecimal baseAmount,
            BigDecimal corporateRate, BigDecimal personalRate,
            BigDecimal corporateAmount, BigDecimal personalAmount) {
        HrmInsuranceSchemeProjectSaveReqVO project = new HrmInsuranceSchemeProjectSaveReqVO();
        project.setType(type);
        project.setName(name);
        project.setBaseAmount(baseAmount);
        project.setCorporateRate(corporateRate);
        project.setPersonalRate(personalRate);
        project.setCorporateAmount(corporateAmount);
        project.setPersonalAmount(personalAmount);
        return project;
    }

    private static void assertAmount(int expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual));
    }

}
