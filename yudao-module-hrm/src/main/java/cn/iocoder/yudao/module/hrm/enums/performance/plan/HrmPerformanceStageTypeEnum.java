package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * HRM 绩效阶段类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceStageTypeEnum implements ArrayValuable<Integer> {

    NOT_STARTED(0, "未开始"),
    FILL_QUOTA(1, "员工填写"),
    TARGET_CONFIRM(2, "目标确认"),
    SELF_SCORE(3, "自评"),
    OTHER_SCORE(4, "他人评分"),
    RESULT_AUDIT(5, "结果审核"),
    RESULT_CONFIRM(6, "结果确认"),
    APPEAL_CONFIRM(7, "申诉确认"),
    ARCHIVED(8, "归档"),
    EXECUTING(9, "执行中"),
    END(10, "结束");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceStageTypeEnum::getType).toArray(Integer[]::new);

    public static final List<Integer> REVIEW_TYPES = Collections.unmodifiableList(Arrays.asList(
            SELF_SCORE.getType(), OTHER_SCORE.getType()));

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceStageTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
