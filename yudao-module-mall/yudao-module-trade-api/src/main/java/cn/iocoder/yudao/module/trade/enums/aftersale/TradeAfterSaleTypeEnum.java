package cn.iocoder.yudao.module.trade.enums.aftersale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易售后 - 类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeAfterSaleTypeEnum {

    REFUND(10, "退款"),
    RETURN_AND_REFUND(20, "退货退款");

    /**
     * 状态值
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

}
