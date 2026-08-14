package cn.iocoder.yudao.module.hrm.enums.performance.assessment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效申诉超期处理动作枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceAppealTimeoutActionEnum implements ArrayValuable<Integer> {

    REJECT(1, "自动拒绝"),
    APPROVE(2, "自动通过");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceAppealTimeoutActionEnum::getAction).toArray(Integer[]::new);

    /**
     * 动作
     */
    private final Integer action;
    /**
     * 名称
     */
    private final String name;
    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceAppealTimeoutActionEnum valueOf(Integer action) {
        return ArrayUtil.firstMatch(item -> item.getAction().equals(action), values());
    }

}
