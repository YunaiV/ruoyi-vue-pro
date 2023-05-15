package cn.iocoder.yudao.module.trade.enums.delivery;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum DeliveryExpressChargeModeEnum implements IntArrayValuable {
    BY_PIECE(1, "按件"),
    BY_WEIGHT(2,"按重量"),
    BY_VOLUME(3, "按体积");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DeliveryExpressChargeModeEnum::getType).toArray();
    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String desc;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
