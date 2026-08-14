package cn.iocoder.yudao.module.hrm.service.performance.config;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessmenttemplate.HrmPerformanceAssessmentTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.config.HrmPerformanceAssessmentTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link HrmPerformanceAssessmentTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformanceAssessmentTemplateServiceImpl.class)
public class HrmPerformanceAssessmentTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmPerformanceAssessmentTemplateServiceImpl assessmentTemplateService;

    @Resource
    private HrmPerformanceAssessmentTemplateMapper assessmentTemplateMapper;

    @MockBean
    private HrmPerformancePlanService performancePlanService;

    @Test
    public void testCreateAssessmentTemplate_success() {
        // 准备参数
        HrmPerformanceAssessmentTemplateSaveReqVO reqVO =
                createAssessmentTemplateReqVO("季度考核模板");

        // 调用
        Long templateId = assessmentTemplateService.createPerformanceAssessmentTemplate(reqVO);

        // 断言
        assertNotNull(templateId);
        HrmPerformanceAssessmentTemplateDO template = assessmentTemplateMapper.selectById(templateId);
        assertEquals(reqVO.getName(), template.getName());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), template.getStatus());
        assertEquals(2, template.getDimensionCount());
        assertEquals(3, template.getQuotaCount());
        assertEquals(2, template.getDimensions().size());
        assertEquals(2, template.getDimensions().get(0).getQuotas().size());
    }

    @Test
    public void testCreateAssessmentTemplate_nameDuplicate() {
        // mock 数据
        assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        // 准备参数
        HrmPerformanceAssessmentTemplateSaveReqVO reqVO =
                createAssessmentTemplateReqVO("季度考核模板");

        // 调用，并断言异常
        assertServiceException(() -> assessmentTemplateService.createPerformanceAssessmentTemplate(
                        reqVO),
                PERFORMANCE_ASSESSMENT_TEMPLATE_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateAssessmentTemplate_success() {
        // mock 数据
        Long templateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        // 准备参数
        HrmPerformanceAssessmentTemplateSaveReqVO reqVO =
                createAssessmentTemplateReqVO("年度考核模板");
        reqVO.setId(templateId);
        reqVO.setDimensions(Collections.singletonList(reqVO.getDimensions().get(0)));
        reqVO.getDimensions().get(0).setWeight(BigDecimal.valueOf(100));

        // 调用
        assessmentTemplateService.updatePerformanceAssessmentTemplate(reqVO);

        // 断言
        HrmPerformanceAssessmentTemplateDO oldTemplate = assessmentTemplateMapper.selectById(templateId);
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), oldTemplate.getStatus());
        assertEquals("季度考核模板", oldTemplate.getName());
        HrmPerformanceAssessmentTemplateDO template = assessmentTemplateMapper.selectByName(reqVO.getName());
        assertNotNull(template);
        assertNotEquals(templateId, template.getId());
        assertEquals(reqVO.getName(), template.getName());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), template.getStatus());
        assertEquals(1, template.getDimensions().size());
        assertEquals(2, template.getDimensions().get(0).getQuotas().size());
        assertEquals(1, template.getDimensionCount());
        assertEquals(2, template.getQuotaCount());
    }

    @Test
    public void testUpdateAssessmentTemplate_notExists() {
        // 准备参数
        HrmPerformanceAssessmentTemplateSaveReqVO reqVO =
                createAssessmentTemplateReqVO("年度考核模板").setId(randomLongId());

        // 调用，并断言异常
        assertServiceException(() -> assessmentTemplateService.updatePerformanceAssessmentTemplate(reqVO),
                PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testUpdateAssessmentTemplate_disabled() {
        // mock 数据
        Long templateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        assessmentTemplateMapper.updateById(new HrmPerformanceAssessmentTemplateDO()
                .setId(templateId).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        // 准备参数
        HrmPerformanceAssessmentTemplateSaveReqVO reqVO =
                createAssessmentTemplateReqVO("年度考核模板").setId(templateId);

        // 调用，并断言异常
        assertServiceException(() -> assessmentTemplateService.updatePerformanceAssessmentTemplate(reqVO),
                PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testUpdateAssessmentTemplate_usedByPlan() {
        // mock 数据
        Long templateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        // 准备参数
        HrmPerformanceAssessmentTemplateSaveReqVO reqVO =
                createAssessmentTemplateReqVO("年度考核模板").setId(templateId);

        // mock 方法：模拟模板已经被计划引用
        when(performancePlanService.getPerformancePlanCountByAssessmentTemplateId(templateId)).thenReturn(1L);

        // 调用
        assessmentTemplateService.updatePerformanceAssessmentTemplate(reqVO);

        // 断言：既有计划继续引用旧版本，新版本使用新的模板编号
        HrmPerformanceAssessmentTemplateDO oldTemplate = assessmentTemplateMapper.selectById(templateId);
        assertEquals("季度考核模板", oldTemplate.getName());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), oldTemplate.getStatus());
        HrmPerformanceAssessmentTemplateDO newTemplate = assessmentTemplateMapper.selectByName(reqVO.getName());
        assertNotNull(newTemplate);
        assertNotEquals(templateId, newTemplate.getId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), newTemplate.getStatus());
    }

    @Test
    public void testDeleteAssessmentTemplate_success() {
        // mock 数据
        Long templateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));

        // 调用
        assessmentTemplateService.deletePerformanceAssessmentTemplate(templateId);

        // 断言
        assertNull(assessmentTemplateMapper.selectById(templateId));
    }

    @Test
    public void testDeleteAssessmentTemplate_notExists() {
        // 准备参数
        Long templateId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> assessmentTemplateService.deletePerformanceAssessmentTemplate(templateId),
                PERFORMANCE_ASSESSMENT_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteAssessmentTemplate_usedByPlan() {
        // mock 数据
        Long templateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        when(performancePlanService.getPerformancePlanCountByAssessmentTemplateId(templateId)).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> assessmentTemplateService.deletePerformanceAssessmentTemplate(templateId),
                PERFORMANCE_DATA_ILLEGAL);
    }

    @Test
    public void testDeleteAssessmentTemplateList_success() {
        // mock 数据
        Long firstTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        Long secondTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("年度考核模板"));
        // 准备参数
        List<Long> templateIds = Arrays.asList(firstTemplateId, secondTemplateId);

        // 调用
        assessmentTemplateService.deletePerformanceAssessmentTemplateList(templateIds);

        // 断言
        assertNull(assessmentTemplateMapper.selectById(firstTemplateId));
        assertNull(assessmentTemplateMapper.selectById(secondTemplateId));
    }

    @Test
    public void testGetAssessmentTemplatePage() {
        // mock 数据
        Long oldTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("年度考核模板"));
        Long newTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("年度管理层考核模板"));
        Long disabledTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("已停用年度模板"));
        assessmentTemplateMapper.updateById(new HrmPerformanceAssessmentTemplateDO()
                .setId(disabledTemplateId).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        updateAssessmentTemplateTime(oldTemplateId, LocalDateTime.of(2026, 1, 1, 0, 0));
        updateAssessmentTemplateTime(newTemplateId, LocalDateTime.of(2027, 1, 1, 0, 0));
        // 准备参数
        HrmPerformanceAssessmentTemplatePageReqVO pageReqVO = new HrmPerformanceAssessmentTemplatePageReqVO();
        pageReqVO.setName("年度");

        // 调用
        PageResult<HrmPerformanceAssessmentTemplateDO> pageResult =
                assessmentTemplateService.getPerformanceAssessmentTemplatePage(pageReqVO);

        // 断言
        assertEquals(2, pageResult.getTotal());
        assertEquals(newTemplateId, pageResult.getList().get(0).getId());
        assertEquals(oldTemplateId, pageResult.getList().get(1).getId());
    }

    @Test
    public void testGetAssessmentTemplateListByStatus() {
        // mock 数据
        Long oldTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("季度考核模板"));
        Long newTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("年度考核模板"));
        Long disabledTemplateId = assessmentTemplateService.createPerformanceAssessmentTemplate(
                createAssessmentTemplateReqVO("停用模板"));
        assessmentTemplateMapper.updateById(new HrmPerformanceAssessmentTemplateDO()
                .setId(disabledTemplateId).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        updateAssessmentTemplateTime(oldTemplateId, LocalDateTime.of(2026, 1, 1, 0, 0));
        updateAssessmentTemplateTime(newTemplateId, LocalDateTime.of(2027, 1, 1, 0, 0));

        // 调用
        List<HrmPerformanceAssessmentTemplateDO> templates =
                assessmentTemplateService.getPerformanceAssessmentTemplateListByStatus(
                        CommonStatusEnum.ENABLE.getStatus());

        // 断言
        assertEquals(2, templates.size());
        assertEquals(newTemplateId, templates.get(0).getId());
        assertEquals(oldTemplateId, templates.get(1).getId());
    }

    // ========== 随机对象 ==========

    private void updateAssessmentTemplateTime(Long id, LocalDateTime updateTime) {
        assessmentTemplateMapper.update(null, new LambdaUpdateWrapper<HrmPerformanceAssessmentTemplateDO>()
                .set(HrmPerformanceAssessmentTemplateDO::getUpdateTime, updateTime)
                .eq(HrmPerformanceAssessmentTemplateDO::getId, id));
    }

    private HrmPerformanceAssessmentTemplateSaveReqVO createAssessmentTemplateReqVO(String name) {
        return randomPojo(HrmPerformanceAssessmentTemplateSaveReqVO.class, reqVO -> reqVO.setId(null)
                .setName(name).setIllustrate("用于季度/年度考核")
                .setScoreCalculation(1).setUpperLimitType(1).setUpperLimitScore(BigDecimal.valueOf(100))
                .setDimensions(Arrays.asList(
                        dimension("业绩", 70, Arrays.asList(
                                quota("目标达成率", 60), quota("关键项目", 40))),
                        dimension("协作", 30,
                                Collections.singletonList(quota("跨团队协作", 100))))));
    }

    private HrmPerformanceAssessmentTemplateSaveReqVO.Dimension dimension(
            String name, int weight, List<HrmPerformanceAssessmentTemplateSaveReqVO.Quota> quotas) {
        return randomPojo(HrmPerformanceAssessmentTemplateSaveReqVO.Dimension.class, dimension ->
                dimension.setName(name).setQuotaType(1).setWeight(BigDecimal.valueOf(weight))
                        .setRemark(null).setAllowEdit(true).setQuotas(quotas));
    }

    private HrmPerformanceAssessmentTemplateSaveReqVO.Quota quota(String name, int weight) {
        return randomPojo(HrmPerformanceAssessmentTemplateSaveReqVO.Quota.class, quota ->
                quota.setName(name).setIllustrate(name + "说明").setStandard("按完成质量评分")
                        .setWeight(BigDecimal.valueOf(weight)).setScoreType(1));
    }

}
