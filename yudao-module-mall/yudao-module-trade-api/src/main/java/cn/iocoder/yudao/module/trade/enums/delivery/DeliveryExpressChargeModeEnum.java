package cn.iocoder.yudao.module.trade.enums.delivery;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 快递配送计费方式枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum DeliveryExpressChargeModeEnum implements ArrayValuable<Integer> {

    COUNT(1, "按件"),
    WEIGHT(2,"按重量"),
    VOLUME(3, "按体积");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(DeliveryExpressChargeModeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String desc;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static DeliveryExpressChargeModeEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(chargeMode -> chargeMode.getType().equals(value), DeliveryExpressChargeModeEnum.values());
    }

}
