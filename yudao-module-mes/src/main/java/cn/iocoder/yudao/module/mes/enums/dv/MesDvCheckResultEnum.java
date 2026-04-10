package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 设备点检结果枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvCheckResultEnum implements ArrayValuable<Integer> {

    NORMAL(1, "正常"),
    ABNORMAL(2, "异常");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvCheckResultEnum::getResult).toArray(Integer[]::new);

    /**
     * 结果值
     */
    private final Integer result;
    /**
     * 结果名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
