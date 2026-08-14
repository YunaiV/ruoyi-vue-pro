package cn.iocoder.yudao.module.fms.enums.closing;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 结转取数时间枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsClosingTimeTypeEnum implements ArrayValuable<Integer> {

    PERIOD_END(1, "期末"),
    PERIOD_BEGIN(2, "期初"),
    YEAR_BEGIN(3, "年初");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsClosingTimeTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsClosingTimeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
