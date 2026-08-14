package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.taxrule.HrmSalaryTaxRuleSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryTaxRuleMapper;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_TAX_RULE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_TAX_RULE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_TAX_RULE_USED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalaryTaxRuleServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryTaxRuleServiceImpl.class)
public class HrmSalaryTaxRuleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryTaxRuleServiceImpl salaryTaxRuleService;

    @Resource
    private HrmSalaryTaxRuleMapper salaryTaxRuleMapper;

    @MockBean
    private HrmSalaryGroupService salaryGroupService;

    @Test
    public void testCreateSalaryTaxRule_success() {
        // 准备参数
        HrmSalaryTaxRuleSaveReqVO reqVO = randomSalaryTaxRuleSaveReqVO();

        // 调用
        Long salaryTaxRuleId = salaryTaxRuleService.createSalaryTaxRule(reqVO);

        // 断言
        assertNotNull(salaryTaxRuleId);
        assertPojoEquals(reqVO, salaryTaxRuleMapper.selectById(salaryTaxRuleId), "id");
    }

    @Test
    public void testCreateSalaryTaxRule_nameDuplicate() {
        // mock 数据
        HrmSalaryTaxRuleDO dbSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(dbSalaryTaxRule);
        // 准备参数
        HrmSalaryTaxRuleSaveReqVO reqVO = randomSalaryTaxRuleSaveReqVO(
                o -> o.setName(dbSalaryTaxRule.getName()));

        // 调用，并断言异常
        assertServiceException(() -> salaryTaxRuleService.createSalaryTaxRule(reqVO),
                SALARY_TAX_RULE_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateSalaryTaxRule_usedSuccess() {
        // mock 数据
        HrmSalaryTaxRuleDO dbSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(dbSalaryTaxRule);
        when(salaryGroupService.getSalaryGroupCountByTaxRuleId(dbSalaryTaxRule.getId())).thenReturn(1L);
        // 准备参数
        HrmSalaryTaxRuleSaveReqVO reqVO = randomSalaryTaxRuleSaveReqVO(o -> o
                .setId(dbSalaryTaxRule.getId()).setType(HrmSalaryTaxTypeEnum.NONE.getType()).setTaxEnabled(false)
                .setThreshold(new BigDecimal("0.00")).setDecimalScale(null).setCycleType(null));

        // 调用
        salaryTaxRuleService.updateSalaryTaxRule(reqVO);

        // 断言
        HrmSalaryTaxRuleDO salaryTaxRule = salaryTaxRuleMapper.selectById(dbSalaryTaxRule.getId());
        assertPojoEquals(reqVO, salaryTaxRule);
        assertNull(salaryTaxRule.getDecimalScale());
        assertNull(salaryTaxRule.getCycleType());
    }

    @Test
    public void testUpdateSalaryTaxRule_notExists() {
        // 准备参数
        HrmSalaryTaxRuleSaveReqVO reqVO = randomSalaryTaxRuleSaveReqVO(
                o -> o.setId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> salaryTaxRuleService.updateSalaryTaxRule(reqVO),
                SALARY_TAX_RULE_NOT_EXISTS);
    }

    @Test
    public void testDeleteSalaryTaxRule_success() {
        // mock 数据
        HrmSalaryTaxRuleDO dbSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(dbSalaryTaxRule);
        // mock 方法
        when(salaryGroupService.getSalaryGroupCountByTaxRuleId(dbSalaryTaxRule.getId())).thenReturn(0L);

        // 调用
        salaryTaxRuleService.deleteSalaryTaxRule(dbSalaryTaxRule.getId());

        // 断言
        assertNull(salaryTaxRuleMapper.selectById(dbSalaryTaxRule.getId()));
    }

    @Test
    public void testDeleteSalaryTaxRule_used() {
        // mock 数据
        HrmSalaryTaxRuleDO dbSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(dbSalaryTaxRule);
        // mock 方法
        when(salaryGroupService.getSalaryGroupCountByTaxRuleId(dbSalaryTaxRule.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> salaryTaxRuleService.deleteSalaryTaxRule(dbSalaryTaxRule.getId()),
                SALARY_TAX_RULE_USED);
    }

    @Test
    public void testGetSalaryTaxRule() {
        // mock 数据
        HrmSalaryTaxRuleDO dbSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(dbSalaryTaxRule);

        // 调用
        HrmSalaryTaxRuleDO salaryTaxRule = salaryTaxRuleService.getSalaryTaxRule(dbSalaryTaxRule.getId());

        // 断言
        assertPojoEquals(dbSalaryTaxRule, salaryTaxRule);
    }

    @Test
    public void testGetSalaryTaxRuleList() {
        // mock 数据
        HrmSalaryTaxRuleDO firstSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(firstSalaryTaxRule);
        HrmSalaryTaxRuleDO secondSalaryTaxRule = randomSalaryTaxRuleDO();
        salaryTaxRuleMapper.insert(secondSalaryTaxRule);

        // 调用
        List<HrmSalaryTaxRuleDO> salaryTaxRules = salaryTaxRuleService.getSalaryTaxRuleList();
        Map<Long, HrmSalaryTaxRuleDO> salaryTaxRuleMap = salaryTaxRuleService.getSalaryTaxRuleMap(
                Arrays.asList(firstSalaryTaxRule.getId(), secondSalaryTaxRule.getId()));

        // 断言
        assertEquals(Arrays.asList(firstSalaryTaxRule, secondSalaryTaxRule), salaryTaxRules);
        assertPojoEquals(firstSalaryTaxRule, salaryTaxRuleMap.get(firstSalaryTaxRule.getId()));
        assertEquals(2, salaryTaxRuleMap.size());
    }

    // ========== 随机对象 ==========

    private HrmSalaryTaxRuleDO randomSalaryTaxRuleDO(Consumer<HrmSalaryTaxRuleDO>... consumers) {
        return randomPojo(HrmSalaryTaxRuleDO.class, o -> {
            o.setId(null).setName(randomString()).setType(HrmSalaryTaxTypeEnum.SALARY.getType()).setTaxEnabled(true)
                    .setThreshold(new BigDecimal("5000.00")).setDecimalScale(2)
                    .setCycleType(HrmSalaryTaxCycleTypeEnum.DECEMBER_TO_NOVEMBER.getType());
            for (Consumer<HrmSalaryTaxRuleDO> consumer : consumers) {
                consumer.accept(o);
            }
        });
    }

    private HrmSalaryTaxRuleSaveReqVO randomSalaryTaxRuleSaveReqVO(
            Consumer<HrmSalaryTaxRuleSaveReqVO>... consumers) {
        return randomPojo(HrmSalaryTaxRuleSaveReqVO.class, o -> {
            o.setId(null).setName(randomString()).setType(HrmSalaryTaxTypeEnum.SALARY.getType()).setTaxEnabled(true)
                    .setThreshold(new BigDecimal("6000.00")).setDecimalScale(2)
                    .setCycleType(HrmSalaryTaxCycleTypeEnum.JANUARY_TO_DECEMBER.getType());
            for (Consumer<HrmSalaryTaxRuleSaveReqVO> consumer : consumers) {
                consumer.accept(o);
            }
        });
    }

}
