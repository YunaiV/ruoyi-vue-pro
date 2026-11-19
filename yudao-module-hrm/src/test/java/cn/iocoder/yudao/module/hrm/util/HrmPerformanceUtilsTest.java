package cn.iocoder.yudao.module.hrm.util;

import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HrmPerformanceUtils} 的单元测试类
 *
 * @author 芋道源码
 */
public class HrmPerformanceUtilsTest {

    private static final Integer NOT_PROCESSED = HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus();
    private static final Integer PENDING = HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus();
    private static final Integer PROCESSED = HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus();

    @Test
    public void testParseTargetValue() {
        assertEquals(new BigDecimal("100"), HrmPerformanceUtils.parseTargetValue(" 100 "));
        assertEquals(new BigDecimal("0.5"), HrmPerformanceUtils.parseTargetValue("0.5"));
        // 目标值必须大于 0，且只接受纯数字
        for (String value : Arrays.asList(null, "", "  ", "0", "0.00", "-1", "+1", "1e3", "100%", "1,000", "1.", ".5", "abc")) {
            assertNull(HrmPerformanceUtils.parseTargetValue(value), String.valueOf(value));
        }
    }

    @Test
    public void testParseActualValue() {
        assertEquals(new BigDecimal("0"), HrmPerformanceUtils.parseActualValue("0"));
        assertEquals(new BigDecimal("12.345"), HrmPerformanceUtils.parseActualValue("12.345"));
        // 实际值不能为负数，也不接受文本
        for (String value : Arrays.asList(null, "", "-0.01", "95%", "约 100")) {
            assertNull(HrmPerformanceUtils.parseActualValue(value), String.valueOf(value));
        }
    }

    @Test
    public void testCalculateCompletionRateScore() {
        BigDecimal fullScore = new BigDecimal("100");
        // 部分完成：75 / 100 × 100
        assertEquals(new BigDecimal("75.00"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("100"), new BigDecimal("75"), fullScore));
        // 超额完成按 1 封顶
        assertEquals(new BigDecimal("100.00"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("100"), new BigDecimal("150"), fullScore));
        // 实际值为 0
        assertEquals(new BigDecimal("0.00"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("100"), BigDecimal.ZERO, fullScore));
        // 除不尽时只在结果保留两位小数：1 / 3 × 100 = 33.33；2 / 3 × 100 = 66.67
        assertEquals(new BigDecimal("33.33"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("3"), BigDecimal.ONE, fullScore));
        assertEquals(new BigDecimal("66.67"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("3"), new BigDecimal("2"), fullScore));
        // 满分取统一评分上限
        assertEquals(new BigDecimal("90.00"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("80"), new BigDecimal("80"), new BigDecimal("90")));
        assertEquals(new BigDecimal("45.00"), HrmPerformanceUtils.calculateCompletionRateScore(
                new BigDecimal("80"), new BigDecimal("40"), new BigDecimal("90")));
    }

    @Test
    public void testIsActualValueEditable_firstStagePending() {
        HrmPerformanceAssessmentStageDO first = buildStage(1L, 1, PENDING);
        HrmPerformanceAssessmentStageDO second = buildStage(2L, 2, NOT_PROCESSED);
        List<HrmPerformanceAssessmentStageDO> stages = Arrays.asList(second, first);

        assertEquals(first, HrmPerformanceUtils.getActualValueStage(stages));
        assertTrue(HrmPerformanceUtils.isActualValueEditable(first, stages));
    }

    @Test
    public void testIsActualValueEditable_laterStageReadonly() {
        HrmPerformanceAssessmentStageDO first = buildStage(1L, 1, PROCESSED);
        HrmPerformanceAssessmentStageDO second = buildStage(2L, 2, PENDING);
        List<HrmPerformanceAssessmentStageDO> stages = Arrays.asList(first, second);

        assertFalse(HrmPerformanceUtils.isActualValueEditable(second, stages));
    }

    @Test
    public void testIsActualValueEditable_rejectedBackToFirstStage() {
        // 驳回到首个评分阶段时，后续阶段已重置为未处理，允许修正实际值
        HrmPerformanceAssessmentStageDO first = buildStage(1L, 1, PENDING);
        HrmPerformanceAssessmentStageDO second = buildStage(2L, 2, NOT_PROCESSED);
        HrmPerformanceAssessmentStageDO third = buildStage(3L, 3, NOT_PROCESSED);

        assertTrue(HrmPerformanceUtils.isActualValueEditable(first, Arrays.asList(first, second, third)));
    }

    @Test
    public void testIsActualValueEditable_reopenedWithOtherStageScored() {
        // 申诉 / 结果审核仅退回首个阶段时，其他阶段已有评分，实际值保持冻结
        HrmPerformanceAssessmentStageDO first = buildStage(1L, 1, PENDING);
        HrmPerformanceAssessmentStageDO processed = buildStage(2L, 2, PROCESSED);
        assertFalse(HrmPerformanceUtils.isActualValueEditable(first, Arrays.asList(first, processed)));

        // 多个阶段并行待评分时，同样保持冻结
        HrmPerformanceAssessmentStageDO pending = buildStage(2L, 2, PENDING);
        assertFalse(HrmPerformanceUtils.isActualValueEditable(first, Arrays.asList(first, pending)));
    }

    @Test
    public void testGetActualValueStage_sameSortUseSmallerId() {
        // 排序相同时按编号确定录入阶段，与评分阶段查询的排序一致
        HrmPerformanceAssessmentStageDO larger = buildStage(5L, 1, PENDING);
        HrmPerformanceAssessmentStageDO smaller = buildStage(3L, 1, NOT_PROCESSED);

        assertEquals(smaller, HrmPerformanceUtils.getActualValueStage(Arrays.asList(larger, smaller)));
        assertFalse(HrmPerformanceUtils.isActualValueEditable(larger, Arrays.asList(larger, smaller)));
    }

    @Test
    public void testIsActualValueEditable_notPendingOrEmpty() {
        HrmPerformanceAssessmentStageDO first = buildStage(1L, 1, PROCESSED);
        assertFalse(HrmPerformanceUtils.isActualValueEditable(first, Collections.singletonList(first)));
        assertFalse(HrmPerformanceUtils.isActualValueEditable(first, Collections.emptyList()));
        assertNull(HrmPerformanceUtils.getActualValueStage(Collections.emptyList()));
    }

    private HrmPerformanceAssessmentStageDO buildStage(Long id, Integer sort, Integer status) {
        HrmPerformanceAssessmentStageDO stage = new HrmPerformanceAssessmentStageDO();
        stage.setId(id);
        stage.setSort(sort);
        stage.setStatus(status);
        return stage;
    }

}
