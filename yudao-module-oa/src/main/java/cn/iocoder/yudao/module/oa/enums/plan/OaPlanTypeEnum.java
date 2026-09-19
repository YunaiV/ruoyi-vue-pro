package cn.iocoder.yudao.module.oa.enums.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 工作计划类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaPlanTypeEnum implements ArrayValuable<Integer> {

    DAY(1, "日计划"),
    WEEK(2, "周计划"),
    MONTH(3, "月计划");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaPlanTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 名称
     */
    private final String name;

    public static OaPlanTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
