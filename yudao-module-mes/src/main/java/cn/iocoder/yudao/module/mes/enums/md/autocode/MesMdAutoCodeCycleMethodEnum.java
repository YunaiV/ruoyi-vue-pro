package cn.iocoder.yudao.module.mes.enums.md.autocode;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 编码规则循环方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesMdAutoCodeCycleMethodEnum implements ArrayValuable<Integer> {

    YEAR(1, "按年"),
    MONTH(2, "按月"),
    DAY(3, "按天"),
    HOUR(4, "按小时"),
    MINUTE(5, "按分钟"),
    INPUT_CHAR(10, "按传入字符");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesMdAutoCodeCycleMethodEnum::getMethod).toArray(Integer[]::new);

    /**
     * 方式
     */
    private final Integer method;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
