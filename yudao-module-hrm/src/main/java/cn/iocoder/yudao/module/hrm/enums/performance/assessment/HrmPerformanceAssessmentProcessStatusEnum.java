package cn.iocoder.yudao.module.hrm.enums.performance.assessment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工绩效处理状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceAssessmentProcessStatusEnum implements ArrayValuable<Integer> {

    PROCESSING(1, "进行中"),
    FINISHED(2, "已完成");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceAssessmentProcessStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static HrmPerformanceAssessmentProcessStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
