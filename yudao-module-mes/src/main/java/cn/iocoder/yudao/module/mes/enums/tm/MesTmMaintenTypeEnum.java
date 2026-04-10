package cn.iocoder.yudao.module.mes.enums.tm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 保养维护类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesTmMaintenTypeEnum implements ArrayValuable<Integer> {

    REGULAR(1, "定期维护"),
    USAGE(2, "按使用次数维护");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesTmMaintenTypeEnum::getType).toArray(Integer[]::new);

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
