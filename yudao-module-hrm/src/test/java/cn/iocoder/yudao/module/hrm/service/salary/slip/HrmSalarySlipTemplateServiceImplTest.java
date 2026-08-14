package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateOptionVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template.HrmSalarySlipTemplateSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip.HrmSalarySlipTemplateMapper;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateOptionTypeEnum;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_DATA_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalarySlipTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({HrmSalarySlipTemplateServiceImpl.class,
        HrmSalarySlipTemplateServiceImplTest.TenantTestConfiguration.class})
public class HrmSalarySlipTemplateServiceImplTest extends BaseDbUnitTest {

    private static final Long TENANT_ID = 2L;

    @TestConfiguration(proxyBeanMethods = false)
    static class TenantTestConfiguration {

        @Bean
        static BeanPostProcessor tenantMybatisPlusInterceptorPostProcessor() {
            return new BeanPostProcessor() {

                @Override
                public Object postProcessBeforeInitialization(Object bean, String beanName) {
                    if (bean instanceof MybatisPlusInterceptor) {
                        TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(
                                new TenantDatabaseInterceptor(new TenantProperties()));
                        MyBatisUtils.addInterceptor((MybatisPlusInterceptor) bean, inner, 0);
                    }
                    return bean;
                }

            };
        }

    }

    @Resource
    private HrmSalarySlipTemplateServiceImpl salarySlipTemplateService;
    @Resource
    private HrmSalarySlipTemplateMapper salarySlipTemplateMapper;

    @MockitoBean
    private HrmSalaryOptionService salaryOptionService;

    @BeforeEach
    public void setUpTenantContext() {
        TenantContextHolder.setTenantId(TENANT_ID);
    }

    @AfterEach
    public void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    public void testCreateSalarySlipTemplate_success() {
        // 准备参数
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(null);
            o.setName("研发工资条");
            o.setHideEmpty(null);
            o.setOptions(Collections.singletonList(createTemplateOption("基本工资", 10101, 1)));
        });

        // 调用
        Long templateId = salarySlipTemplateService.createSalarySlipTemplate(reqVO);

        // 断言
        assertNotNull(templateId);
        HrmSalarySlipTemplateDO template = salarySlipTemplateService.getSalarySlipTemplate(templateId);
        assertEquals("研发工资条", template.getName());
        assertFalse(template.getHideEmpty());
        assertFalse(template.getDefaultStatus());
        assertEquals(2, template.getOptions().size());
        assertTrue(template.getOptions().stream().anyMatch(
                option -> Objects.equals(option.getCode(), HrmSalaryOptionCodeEnum.REAL_PAY.getCode())
                        && Boolean.FALSE.equals(option.getHidden())));

    }

    @Test
    public void testUpdateSalarySlipTemplate_success() {
        // mock 数据
        Long templateId = salarySlipTemplateService.createSalarySlipTemplate(
                randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
                    o.setId(null).setName("研发工资条").setHideEmpty(false);
                    o.setOptions(Collections.singletonList(
                            createTemplateOption("基本工资", 10101, 1)));
                }));
        // 准备参数
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(templateId);
            o.setName("研发工资条（新版）");
            o.setHideEmpty(true);
            o.setOptions(Collections.singletonList(
                    createTemplateOption("应发工资", HrmSalaryOptionCodeEnum.EXPECTED_PAY.getCode(), 2)));
        });

        // 调用
        salarySlipTemplateService.updateSalarySlipTemplate(reqVO);

        // 断言
        HrmSalarySlipTemplateDO template =
                salarySlipTemplateService.getSalarySlipTemplate(templateId);
        assertEquals("研发工资条（新版）", template.getName());
        assertTrue(template.getOptions().stream().anyMatch(
                option -> Objects.equals(option.getCode(), HrmSalaryOptionCodeEnum.EXPECTED_PAY.getCode())));
        assertTrue(template.getOptions().stream().anyMatch(
                option -> Objects.equals(option.getCode(), HrmSalaryOptionCodeEnum.REAL_PAY.getCode())));
        assertEquals(1, salarySlipTemplateService.getSalarySlipTemplateList().size());
    }

    @Test
    public void testDeleteSalarySlipTemplate_success() {
        // mock 数据
        Long templateId = salarySlipTemplateService.createSalarySlipTemplate(
                randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
                    o.setId(null).setName("研发工资条").setHideEmpty(false);
                    o.setOptions(Collections.singletonList(
                            createTemplateOption("基本工资", 10101, 1)));
                }));

        // 调用
        salarySlipTemplateService.deleteSalarySlipTemplate(templateId);

        // 断言
        assertNull(salarySlipTemplateMapper.selectById(templateId));
    }

    @Test
    public void testDeleteSalarySlipTemplate_default() {
        // mock 数据
        HrmSalarySlipTemplateDO template = randomPojo(HrmSalarySlipTemplateDO.class, o -> {
            o.setId(null).setName("默认工资条模板").setHideEmpty(false).setDefaultStatus(true);
            o.setOptions(Collections.emptyList());
        });
        salarySlipTemplateMapper.insert(template);

        // 调用，并断言异常
        assertServiceException(() -> salarySlipTemplateService.deleteSalarySlipTemplate(template.getId()),
                SALARY_DATA_ILLEGAL);
    }

    @Test
    public void testCreateSalarySlipTemplate_category_success() {
        // 准备参数
        HrmSalarySlipTemplateOptionVO category = createTemplateOption("固定工资", -1, 1);
        category.setType(HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType());
        HrmSalarySlipTemplateOptionVO item = createTemplateOption("基本工资", 10101, 2);
        item.setParentCode(category.getCode());
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(null);
            o.setName("分类工资条");
            o.setHideEmpty(false);
            o.setOptions(Arrays.asList(category, item));
        });

        // 调用
        Long templateId = salarySlipTemplateService.createSalarySlipTemplate(reqVO);

        // 断言
        HrmSalarySlipTemplateDO template = salarySlipTemplateMapper.selectById(templateId);
        assertEquals(2, template.getOptions().size());
        assertEquals("固定工资", template.getOptions().get(0).getName());
        assertEquals(1, template.getOptions().get(0).getChildren().size());
        assertEquals("基本工资", template.getOptions().get(0).getChildren().get(0).getName());
    }

    @Test
    public void testCreateSalarySlipTemplate_orphanItem() {
        // 准备参数
        HrmSalarySlipTemplateOptionVO item = createTemplateOption("基本工资", 10101, 1);
        item.setParentCode(-1);
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(null);
            o.setOptions(Collections.singletonList(item));
        });

        // 调用，并断言异常
        assertServiceException(() -> salarySlipTemplateService.createSalarySlipTemplate(reqVO),
                SALARY_DATA_ILLEGAL);
    }

    @Test
    public void testCreateSalarySlipTemplate_nestedCategory() {
        // 准备参数
        HrmSalarySlipTemplateOptionVO category = createTemplateOption("固定工资", -1, 1);
        category.setType(HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType());
        category.setParentCode(-2);
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(null);
            o.setOptions(Collections.singletonList(category));
        });

        // 调用，并断言异常
        assertServiceException(() -> salarySlipTemplateService.createSalarySlipTemplate(reqVO),
                SALARY_DATA_ILLEGAL);
    }

    @Test
    public void testCreateSalarySlipTemplate_duplicateCode() {
        // 准备参数
        HrmSalarySlipTemplateOptionVO firstItem = createTemplateOption("基本工资", 10101, 1);
        HrmSalarySlipTemplateOptionVO secondItem = createTemplateOption("重复基本工资", 10101, 2);
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(null);
            o.setOptions(Arrays.asList(firstItem, secondItem));
        });

        // 调用，并断言异常
        assertServiceException(() -> salarySlipTemplateService.createSalarySlipTemplate(reqVO),
                SALARY_DATA_ILLEGAL);
    }

    @Test
    public void testGetSalarySlipTemplate_defaultDynamicOptions() {
        // mock 数据
        HrmSalarySlipTemplateDO defaultTemplate = randomPojo(HrmSalarySlipTemplateDO.class, o -> {
            o.setId(null).setName("默认工资条模板").setHideEmpty(false).setDefaultStatus(true);
            o.setOptions(Collections.emptyList());
        });
        salarySlipTemplateMapper.insert(defaultTemplate);
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Arrays.asList(
                createSalaryOption(HrmSalaryOptionCodeEnum.EXPECTED_PAY.getCode(), 210, "应发工资"),
                createSalaryOption(HrmSalaryOptionCodeEnum.PERSONAL_TAX.getCode(), 230, "个人所得税"),
                createSalaryOption(HrmSalaryOptionCodeEnum.REAL_PAY.getCode(), 240, "实发工资"),
                createSalaryOption(10101, 10, "基本工资"),
                createSalaryOption(20101, 20, "奖金")));

        // 调用
        HrmSalarySlipTemplateDO template = salarySlipTemplateService.getSalarySlipTemplateList().stream()
                .filter(item -> Boolean.TRUE.equals(item.getDefaultStatus()))
                .findFirst().orElseThrow(AssertionError::new);

        // 断言
        assertEquals(2, template.getOptions().size());
        assertEquals("基本项", template.getOptions().get(0).getName());
        assertEquals(3, template.getOptions().get(0).getChildren().size());
        assertEquals("明细项", template.getOptions().get(1).getName());
        assertEquals(2, template.getOptions().get(1).getChildren().size());
    }

    @Test
    public void testGetSalarySlipTemplateList_fallbackGlobalDefaultForTenant() {
        // mock 数据：平台默认模板固定保存到 tenant_id = 0，租户只创建自己的自定义模板
        HrmSalarySlipTemplateDO globalDefaultTemplate = randomPojo(HrmSalarySlipTemplateDO.class, o -> {
            o.setId(null).setName("平台默认工资条模板").setHideEmpty(false).setDefaultStatus(true);
            o.setOptions(Collections.emptyList());
        });
        TenantUtils.executeIgnore(() -> salarySlipTemplateMapper.insert(globalDefaultTemplate));
        HrmSalarySlipTemplateDO tenantTemplate = randomPojo(HrmSalarySlipTemplateDO.class, o -> {
            o.setId(null).setName("租户自定义工资条模板").setHideEmpty(false).setDefaultStatus(false);
            o.setOptions(Collections.emptyList());
        });
        salarySlipTemplateMapper.insert(tenantTemplate);
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Collections.emptyList());

        // 调用
        List<HrmSalarySlipTemplateDO> templates =
                salarySlipTemplateService.getSalarySlipTemplateList();

        // 断言：普通查询被租户隔离，但 Service 可只读回退平台默认模板
        assertNull(salarySlipTemplateMapper.selectById(globalDefaultTemplate.getId()));
        assertEquals(globalDefaultTemplate.getId(),
                TenantUtils.executeIgnore(salarySlipTemplateMapper::selectGlobalDefaultTemplate).getId());
        assertEquals(globalDefaultTemplate.getId(), salarySlipTemplateService
                .getSalarySlipTemplate(globalDefaultTemplate.getId()).getId());
        assertEquals(2, templates.size());
        assertEquals(globalDefaultTemplate.getId(), templates.get(0).getId());
        assertEquals(tenantTemplate.getId(), templates.get(1).getId());
    }

    @Test
    public void testGetSalarySlipTemplate_customAppendUnclassifiedOptions() {
        // mock 数据
        when(salaryOptionService.getSalaryOptionList(false)).thenReturn(Arrays.asList(
                createSalaryOption(10101, 10, "基本工资"),
                createSalaryOption(10102, 10, "岗位工资"),
                createSalaryOption(HrmSalaryOptionCodeEnum.REAL_PAY.getCode(), 240, "实发工资")));
        HrmSalarySlipTemplateSaveReqVO reqVO = randomPojo(HrmSalarySlipTemplateSaveReqVO.class, o -> {
            o.setId(null);
            o.setName("自定义工资条");
            o.setHideEmpty(false);
            o.setOptions(Collections.singletonList(createTemplateOption("基本工资", 10101, 1)));
        });
        Long templateId = salarySlipTemplateService.createSalarySlipTemplate(reqVO);

        // 调用
        HrmSalarySlipTemplateDO template = salarySlipTemplateService.getSalarySlipTemplate(templateId);

        // 断言
        HrmSalarySlipTemplateDO.Option unclassifiedOption = template.getOptions().stream()
                .filter(option -> Objects.equals(option.getName(), "未分类科目"))
                .findFirst().orElseThrow(AssertionError::new);
        assertEquals(1, unclassifiedOption.getChildren().size());
        assertEquals(Integer.valueOf(10102), unclassifiedOption.getChildren().get(0).getCode());
    }

    // ========== 随机对象 ==========

    private HrmSalaryOptionDO createSalaryOption(Integer code, Integer parentCode, String name) {
        return HrmSalaryOptionDO.builder().code(code).parentCode(parentCode).name(name).build();
    }

    private HrmSalarySlipTemplateOptionVO createTemplateOption(String name, Integer code, Integer sort) {
        return randomPojo(HrmSalarySlipTemplateOptionVO.class, o -> {
            o.setName(name);
            o.setType(2);
            o.setCode(code);
            o.setRemark(null);
            o.setParentCode(null);
            o.setHidden(null);
            o.setSort(sort);
        });
    }

}
