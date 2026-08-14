package cn.iocoder.yudao.module.hrm.enums.performance.assessment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工绩效考核动作类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceAssessmentActionTypeEnum implements ArrayValuable<Integer> {

    FILL_QUOTA(1, "提交绩效指标", "填写并提交了绩效指标"), // 填写考核指标
    CONFIRM_TARGET(2, "确认绩效目标", "确认了绩效目标%s"), // 确认考核目标
    REJECT_TARGET(3, "驳回绩效目标", "驳回了绩效目标%s"), // 驳回考核目标
    SCORE(4, "提交绩效评分", "填写并提交了评分，阶段得分：%s%s"), // 绩效评分
    REJECT_SCORE(5, "驳回绩效评分", "驳回到【%s】，原因：%s"), // 驳回绩效评分
    CONFIRM_RESULT(6, "确认绩效结果", "确认了绩效结果%s"), // 确认绩效结果
    SUBMIT_APPEAL(7, "提交绩效申诉", "提交了绩效申诉，原因：%s"), // 提交绩效申诉
    PASS_RESULT_AUDIT(8, "通过绩效结果审核", "通过了绩效结果审核%s"), // 通过结果审核
    REJECT_RESULT_AUDIT(9, "驳回绩效结果审核", "驳回了绩效结果审核%s"), // 驳回结果审核
    PASS_APPEAL(10, "通过绩效申诉", "通过了绩效申诉%s"), // 通过绩效申诉
    REJECT_APPEAL(11, "驳回绩效申诉", "驳回了绩效申诉%s"), // 驳回绩效申诉
    APPEAL_TIMEOUT_PASS(12, "自动通过绩效申诉", "通过了绩效申诉%s"), // 超时自动通过申诉
    APPEAL_TIMEOUT_REJECT(13, "自动驳回绩效申诉", "驳回了绩效申诉%s"), // 超时自动驳回申诉
    TERMINATE(14, "终止绩效考核", "终止了绩效考核"); // 终止绩效考核

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceAssessmentActionTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 动作类型
     */
    private final Integer type;
    /**
     * 动作标题
     */
    private final String title;
    /**
     * 动作内容模板
     */
    private final String contentTemplate;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceAssessmentActionTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    /**
     * 格式化动作内容
     *
     * @param args 内容参数
     * @return 动作内容
     */
    public String formatContent(Object... args) {
        return String.format(contentTemplate, args);
    }

}
