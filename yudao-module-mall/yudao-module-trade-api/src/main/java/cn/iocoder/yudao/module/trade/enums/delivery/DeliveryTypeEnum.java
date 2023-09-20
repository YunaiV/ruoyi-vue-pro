package cn.iocoder.yudao.module.trade.enums.delivery;

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
public enum DeliveryTypeEnum implements IntArrayValuable {

    EXPRESS(1, "快递发货"),
    PICK_UP(2, "用户自提"),;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DeliveryTypeEnum::getType).toArray();

    /**
     * 配送方式
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
