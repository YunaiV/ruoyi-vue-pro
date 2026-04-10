package cn.iocoder.yudao.module.mes.enums.qc;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 缺陷等级枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesQcDefectLevelEnum implements ArrayValuable<Integer> {

    CRITICAL(1, "致命"),
    MAJOR(2, "严重"),
    MINOR(3, "轻微");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesQcDefectLevelEnum::getType).toArray(Integer[]::new);

    /**
     * 等级值
     */
    private final Integer type;
    /**
     * 等级名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
