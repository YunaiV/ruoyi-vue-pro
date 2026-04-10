package cn.iocoder.yudao.module.mes.enums.dv;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 设备保养明细结果枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesDvMaintenStatusEnum implements ArrayValuable<Integer> {

    NORMAL(1, "正常"),
    ABNORMAL(2, "异常");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesDvMaintenStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
