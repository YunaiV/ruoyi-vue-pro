package cn.iocoder.yudao.module.oa.enums.vehicle;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 车辆台账状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaVehicleStatusEnum implements ArrayValuable<Integer> {

    IDLE(0, "空闲"),
    DISABLED(1, "停用"),
    IN_USE(2, "使用中");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaVehicleStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 车辆台账状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    public static OaVehicleStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
