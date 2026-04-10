package cn.iocoder.yudao.module.mes.enums.pro;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 时间单位枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesTimeUnitTypeEnum implements ArrayValuable<String> {

    MINUTE("MINUTE", "分钟"),
    HOUR("HOUR", "小时"),
    DAY("DAY", "天");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MesTimeUnitTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型值
     */
    private final String type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
