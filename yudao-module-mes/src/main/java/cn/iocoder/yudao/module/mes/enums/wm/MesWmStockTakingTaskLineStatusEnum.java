package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 盘点任务行状态枚举
 */
@Getter
@AllArgsConstructor
public enum MesWmStockTakingTaskLineStatusEnum implements ArrayValuable<Integer> {

    NORMAL(1, "正常"),
    GAIN(2, "盘盈"),
    LOSS(3, "盘亏");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(MesWmStockTakingTaskLineStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
