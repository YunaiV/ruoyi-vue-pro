package cn.iocoder.yudao.module.hrm.enums.performance.assessment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工绩效考核阶段状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceAssessmentStageStatusEnum implements ArrayValuable<Integer> {

    NOT_PROCESSED(0, "未处理"),
    PROCESSED(1, "已处理"),
    PENDING(2, "待处理"),
    REJECTED(3, "已驳回"),
    PROCESSING(4, "处理中"),
    APPEALED(5, "已申诉");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceAssessmentStageStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceAssessmentStageStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
