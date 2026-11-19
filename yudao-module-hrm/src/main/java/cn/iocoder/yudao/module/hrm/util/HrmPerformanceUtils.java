package cn.iocoder.yudao.module.hrm.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;

/**
 * HRM 绩效工具类
 *
 * 绩效模板、计划、评分和详情查询共用的完成率计分与录入阶段规则
 *
 * @author 芋道源码
 */
@UtilityClass
public class HrmPerformanceUtils {

    /**
     * 纯数字：非负整数或小数，不含正负号、千分位、百分号和科学计数法
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    /**
     * 解析完成率指标的目标值
     *
     * @param value 目标值文本
     * @return 大于 0 的目标值；不合法时返回 null
     */
    public static BigDecimal parseTargetValue(String value) {
        BigDecimal number = parseNumber(value);
        return number != null && number.signum() > 0 ? number : null;
    }

    /**
     * 解析完成率指标的实际值
     *
     * @param value 实际值文本
     * @return 不小于 0 的实际值；不合法时返回 null
     */
    public static BigDecimal parseActualValue(String value) {
        return parseNumber(value);
    }

    /**
     * 计算正向完成率得分：min(实际值 / 目标值, 1) × 满分，保留两位小数
     *
     * @param targetValue 目标值，大于 0
     * @param actualValue 实际值，不小于 0
     * @param fullScore 满分，即统一评分上限
     * @return 指标得分
     */
    public static BigDecimal calculateCompletionRateScore(BigDecimal targetValue, BigDecimal actualValue,
                                                          BigDecimal fullScore) {
        if (actualValue.compareTo(targetValue) >= 0) {
            return fullScore.setScale(2, RoundingMode.HALF_UP);
        }
        // 先乘后除，只在最终结果做一次舍入，避免完成率中间值的精度损失
        return actualValue.multiply(fullScore).divide(targetValue, 2, RoundingMode.HALF_UP);
    }

    /**
     * 获得完成率实际值的录入阶段：按阶段顺序的首个有效评分阶段
     *
     * 说明：计划中无法解析评分人的阶段不会生成考核阶段，因此这里的评分阶段均为有效阶段
     *
     * @param reviewStages 员工绩效考核的评分阶段列表
     * @return 录入阶段；没有评分阶段时返回 null
     */
    public static HrmPerformanceAssessmentStageDO getActualValueStage(
            Collection<HrmPerformanceAssessmentStageDO> reviewStages) {
        if (CollUtil.isEmpty(reviewStages)) {
            return null;
        }
        return reviewStages.stream().filter(stage -> stage.getSort() != null)
                .min(Comparator.comparing(HrmPerformanceAssessmentStageDO::getSort)
                        // 与评分阶段查询的排序一致：sort 相同时按编号确定唯一的录入阶段
                        .thenComparing(HrmPerformanceAssessmentStageDO::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    /**
     * 判断评分阶段当前能否录入完成率实际值
     *
     * 仅录入阶段待处理、且其余评分阶段均未处理时可以录入；
     * 申诉或结果审核退回时若其他阶段已有评分或并行待评分，实际值保持冻结，避免各阶段得分依据不一致。
     *
     * @param stage 评分阶段
     * @param reviewStages 员工绩效考核的评分阶段列表
     * @return 是否可以录入
     */
    public static boolean isActualValueEditable(HrmPerformanceAssessmentStageDO stage,
                                                Collection<HrmPerformanceAssessmentStageDO> reviewStages) {
        HrmPerformanceAssessmentStageDO actualValueStage = getActualValueStage(reviewStages);
        if (stage == null || actualValueStage == null
                || ObjUtil.notEqual(actualValueStage.getId(), stage.getId())
                || ObjUtil.notEqual(stage.getStatus(), HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())) {
            return false;
        }
        return !anyMatch(reviewStages, item -> ObjUtil.notEqual(item.getId(), stage.getId())
                && ObjUtil.notEqual(item.getStatus(), HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()));
    }

    private static BigDecimal parseNumber(String value) {
        String text = StrUtil.trim(value);
        return ReUtil.isMatch(NUMBER_PATTERN, text) ? new BigDecimal(text) : null;
    }

}
