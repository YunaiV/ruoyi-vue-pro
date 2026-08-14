package cn.iocoder.yudao.module.hrm.service.performance.config;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.resulttemplate.HrmPerformanceResultTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.config.HrmPerformanceResultTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getFirst;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_RESULT_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link HrmPerformanceResultTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmPerformanceResultTemplateServiceImpl.class)
public class HrmPerformanceResultTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmPerformanceResultTemplateServiceImpl resultTemplateService;

    @Resource
    private HrmPerformanceResultTemplateMapper resultTemplateMapper;

    @MockitoBean
    private HrmPerformancePlanService performancePlanService;

    @Test
    public void testGetResultTemplate_notExists() {
        assertNull(resultTemplateService.getPerformanceResultTemplate(1L));
    }

    @Test
    public void testCreateResultTemplate_success() {
        // 准备参数
        HrmPerformanceResultTemplateSaveReqVO createReqVO = randomResultTemplateSaveReqVO("默认等级模板");

        // 调用
        Long templateId = resultTemplateService.createPerformanceResultTemplate(createReqVO);

        // 断言
        HrmPerformanceResultTemplateDO template = resultTemplateMapper.selectById(templateId);
        assertEquals("默认等级模板", template.getName());
        assertEquals(BeanUtils.toBean(createReqVO.getLevels(), Level.class), template.getLevels());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), template.getStatus());
    }

    @Test
    public void testCreateResultTemplate_nameDuplicate() {
        // mock 数据
        HrmPerformanceResultTemplateDO template = randomResultTemplateDO(
                "默认等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template);

        // 准备参数
        HrmPerformanceResultTemplateSaveReqVO createReqVO = randomResultTemplateSaveReqVO(template.getName());

        // 调用，并断言异常
        assertServiceException(() -> resultTemplateService.createPerformanceResultTemplate(createReqVO),
                PERFORMANCE_RESULT_TEMPLATE_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateResultTemplate_success() {
        // mock 数据
        HrmPerformanceResultTemplateDO template = randomResultTemplateDO(
                "默认等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template);

        // 准备参数
        HrmPerformanceResultTemplateSaveReqVO updateReqVO =
                randomResultTemplateSaveReqVO(template.getName());
        updateReqVO.setId(template.getId());
        updateReqVO.setLevels(Arrays.asList(
                randomSaveLevel("A", 80, 100, 1.2),
                randomSaveLevel("B", 0, 79.99, 0.8)));

        // 调用
        resultTemplateService.updatePerformanceResultTemplate(updateReqVO);

        // 断言
        HrmPerformanceResultTemplateDO oldTemplate = resultTemplateMapper.selectById(template.getId());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), oldTemplate.getStatus());
        assertEquals("默认等级模板", oldTemplate.getName());
        HrmPerformanceResultTemplateDO newTemplate = resultTemplateMapper.selectByName(updateReqVO.getName());
        assertNotNull(newTemplate);
        assertNotEquals(template.getId(), newTemplate.getId());
        assertEquals(BeanUtils.toBean(updateReqVO.getLevels(), Level.class), newTemplate.getLevels());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), newTemplate.getStatus());
    }

    @Test
    public void testUpdateResultTemplate_nameDuplicate() {
        // mock 数据
        HrmPerformanceResultTemplateDO template01 = randomResultTemplateDO(
                "季度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template01);
        HrmPerformanceResultTemplateDO template02 = randomResultTemplateDO(
                "年度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template02);

        // 准备参数
        HrmPerformanceResultTemplateSaveReqVO updateReqVO =
                randomResultTemplateSaveReqVO(template02.getName());
        updateReqVO.setId(template01.getId());

        // 调用，并断言异常
        assertServiceException(() -> resultTemplateService.updatePerformanceResultTemplate(updateReqVO),
                PERFORMANCE_RESULT_TEMPLATE_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateResultTemplate_notExists() {
        // 准备参数
        HrmPerformanceResultTemplateSaveReqVO updateReqVO = randomResultTemplateSaveReqVO("绩效等级模板");
        updateReqVO.setId(1L);

        // 调用，并断言异常
        assertServiceException(() -> resultTemplateService.updatePerformanceResultTemplate(updateReqVO),
                PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testUpdateResultTemplate_disabled() {
        // mock 数据
        HrmPerformanceResultTemplateDO template = randomResultTemplateDO(
                "默认等级模板", CommonStatusEnum.DISABLE.getStatus());
        resultTemplateMapper.insert(template);
        // 准备参数
        HrmPerformanceResultTemplateSaveReqVO updateReqVO = randomResultTemplateSaveReqVO("新等级模板");
        updateReqVO.setId(template.getId());

        // 调用，并断言异常
        assertServiceException(() -> resultTemplateService.updatePerformanceResultTemplate(updateReqVO),
                PERFORMANCE_RESULT_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteResultTemplate_success() {
        // mock 数据
        HrmPerformanceResultTemplateDO template = randomResultTemplateDO(
                "默认等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template);

        // 调用
        resultTemplateService.deletePerformanceResultTemplate(template.getId());

        // 断言
        assertNull(resultTemplateMapper.selectById(template.getId()));
    }

    @Test
    public void testDeleteResultTemplate_usedByPlan() {
        // mock 数据
        HrmPerformanceResultTemplateDO template = randomResultTemplateDO(
                "默认等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template);

        // mock 方法
        when(performancePlanService.getPerformancePlanCountByResultTemplateId(template.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> resultTemplateService.deletePerformanceResultTemplate(template.getId()),
                PERFORMANCE_DATA_ILLEGAL);
        assertEquals(template.getName(), resultTemplateMapper.selectById(template.getId()).getName());
    }

    @Test
    public void testDeleteResultTemplateList_success() {
        // mock 数据
        HrmPerformanceResultTemplateDO template01 = randomResultTemplateDO(
                "季度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template01);
        HrmPerformanceResultTemplateDO template02 = randomResultTemplateDO(
                "年度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template02);

        // 调用
        resultTemplateService.deletePerformanceResultTemplateList(
                Arrays.asList(template01.getId(), template02.getId()));

        // 断言
        assertNull(resultTemplateMapper.selectById(template01.getId()));
        assertNull(resultTemplateMapper.selectById(template02.getId()));
    }

    @Test
    public void testDeleteResultTemplateList_usedByPlan() {
        // mock 数据
        HrmPerformanceResultTemplateDO template01 = randomResultTemplateDO(
                "季度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template01);
        HrmPerformanceResultTemplateDO template02 = randomResultTemplateDO(
                "年度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template02);

        // mock 方法
        when(performancePlanService.getPerformancePlanCountByResultTemplateId(template02.getId())).thenReturn(1L);

        // 调用，并断言异常：批量删除失败时，已处理的模板也不能被删除
        assertServiceException(() -> resultTemplateService.deletePerformanceResultTemplateList(
                Arrays.asList(template01.getId(), template02.getId())), PERFORMANCE_DATA_ILLEGAL);
        assertEquals(template01.getName(), resultTemplateMapper.selectById(template01.getId()).getName());
        assertEquals(template02.getName(), resultTemplateMapper.selectById(template02.getId()).getName());
    }

    @Test
    public void testGetResultTemplatePage() {
        // mock 数据
        HrmPerformanceResultTemplateDO template01 = randomResultTemplateDO(
                "季度等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(template01);
        HrmPerformanceResultTemplateDO template02 = randomResultTemplateDO(
                "季度停用等级模板", CommonStatusEnum.DISABLE.getStatus());
        resultTemplateMapper.insert(template02);

        // 准备参数
        HrmPerformanceResultTemplatePageReqVO pageReqVO = new HrmPerformanceResultTemplatePageReqVO();
        pageReqVO.setName("季度");

        // 调用
        PageResult<HrmPerformanceResultTemplateDO> pageResult =
                resultTemplateService.getPerformanceResultTemplatePage(pageReqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(template01.getId(), getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetResultTemplateList() {
        // mock 数据
        HrmPerformanceResultTemplateDO enableTemplate = randomResultTemplateDO(
                "启用等级模板", CommonStatusEnum.ENABLE.getStatus());
        resultTemplateMapper.insert(enableTemplate);
        HrmPerformanceResultTemplateDO disableTemplate = randomResultTemplateDO(
                "停用等级模板", CommonStatusEnum.DISABLE.getStatus());
        resultTemplateMapper.insert(disableTemplate);
        updateResultTemplateTime(enableTemplate.getId(), LocalDateTime.of(2027, 1, 1, 0, 0));
        updateResultTemplateTime(disableTemplate.getId(), LocalDateTime.of(2026, 1, 1, 0, 0));

        // 调用
        List<HrmPerformanceResultTemplateDO> enableTemplates =
                resultTemplateService.getPerformanceResultTemplateList(CommonStatusEnum.ENABLE.getStatus());
        List<HrmPerformanceResultTemplateDO> allTemplates =
                resultTemplateService.getPerformanceResultTemplateList(null);

        // 断言
        assertEquals(1, enableTemplates.size());
        assertEquals(enableTemplate.getId(), getFirst(enableTemplates).getId());
        assertEquals(2, allTemplates.size());
        assertEquals(enableTemplate.getId(), getFirst(allTemplates).getId());
    }

    // ========== 随机对象 ==========

    private void updateResultTemplateTime(Long id, LocalDateTime updateTime) {
        resultTemplateMapper.update(null, new LambdaUpdateWrapper<HrmPerformanceResultTemplateDO>()
                .set(HrmPerformanceResultTemplateDO::getUpdateTime, updateTime)
                .eq(HrmPerformanceResultTemplateDO::getId, id));
    }

    private HrmPerformanceResultTemplateSaveReqVO randomResultTemplateSaveReqVO(String name) {
        return randomPojo(HrmPerformanceResultTemplateSaveReqVO.class, reqVO -> reqVO.setId(null)
                .setName(name).setLevels(Arrays.asList(
                        randomSaveLevel("A", 90, 100, 1.2),
                        randomSaveLevel("B", 80, 89.99, 1.1),
                        randomSaveLevel("C", 0, 79.99, 0.8))));
    }

    private HrmPerformanceResultTemplateDO randomResultTemplateDO(String name, Integer status) {
        return randomPojo(HrmPerformanceResultTemplateDO.class, template -> template.setId(null)
                .setName(name).setStatus(status).setLevels(Arrays.asList(
                        randomLevel("A", 90, 100, 1.2),
                        randomLevel("B", 80, 89.99, 1.1),
                        randomLevel("C", 0, 79.99, 0.8))).setCreator("1"));
    }

    private Level randomLevel(String name, double minScore, double maxScore, double coefficient) {
        return randomPojo(Level.class, level -> level.setName(name)
                .setMinScore(BigDecimal.valueOf(minScore)).setMaxScore(BigDecimal.valueOf(maxScore))
                .setCoefficient(BigDecimal.valueOf(coefficient)));
    }

    private HrmPerformanceResultTemplateSaveReqVO.Level randomSaveLevel(
            String name, double minScore, double maxScore, double coefficient) {
        return randomPojo(HrmPerformanceResultTemplateSaveReqVO.Level.class, level -> level.setName(name)
                .setMinScore(BigDecimal.valueOf(minScore)).setMaxScore(BigDecimal.valueOf(maxScore))
                .setCoefficient(BigDecimal.valueOf(coefficient)));
    }

}
