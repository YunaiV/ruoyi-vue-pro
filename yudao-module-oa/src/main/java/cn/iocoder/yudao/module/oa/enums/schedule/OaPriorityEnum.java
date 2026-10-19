package cn.iocoder.yudao.module.oa.enums.schedule;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * OA 优先级枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaPriorityEnum implements ArrayValuable<Integer> {

    NORMAL(1, "一般"),
    IMPORTANT(2, "重要"),
    URGENT(3, "紧急");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaPriorityEnum::getPriority).toArray(Integer[]::new);

    /**
     * 优先级
     */
    private final Integer priority;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
