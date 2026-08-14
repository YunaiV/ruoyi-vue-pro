package cn.iocoder.yudao.module.hrm.enums.performance.assessment;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效确认结果枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceConfirmationResultEnum implements ArrayValuable<Integer> {

    REJECT(0, "驳回"),
    PASS(1, "通过");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceConfirmationResultEnum::getResult).toArray(Integer[]::new);

    /**
     * 结果
     */
    private final Integer result;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceConfirmationResultEnum valueOf(Integer result) {
        return ArrayUtil.firstMatch(item -> item.getResult().equals(result), values());
    }

}
