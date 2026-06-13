package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 点检保养方案类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvCheckPlanTypeEnum implements ArrayValuable<Integer> {

    CHECK(1, "点检"),
    MAINTENANCE(2, "保养");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvCheckPlanTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
