package cn.iocoder.yudao.module.trade.enums.aftersale;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易售后 - 类型
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum AfterSaleTypeEnum implements ArrayValuable<Integer> {

    IN_SALE(10, "售中退款"), // 交易完成前买家申请退款
    AFTER_SALE(20, "售后退款"); // 交易完成后买家申请退款

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AfterSaleTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
