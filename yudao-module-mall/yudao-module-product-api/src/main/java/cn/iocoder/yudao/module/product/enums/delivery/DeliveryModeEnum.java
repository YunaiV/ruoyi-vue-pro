package cn.iocoder.yudao.module.product.enums.delivery;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 配送方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum DeliveryModeEnum implements IntArrayValuable {

    SHOP_DELIVERY(1, "商家配送"),
    USER_PICK_UP(2, "用户自提");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DeliveryModeEnum::getMode).toArray();

    /**
     * 配送方式
     */
    private final Integer mode;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
