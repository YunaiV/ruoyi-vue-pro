package cn.iocoder.yudao.module.trade.enums.aftersale;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

import static cn.hutool.core.util.ArrayUtil.firstMatch;

/**
 * 售后状态的枚举
 *
 * <a href="https://www.processon.com/view/link/63731a270e3e742ce7b7c194">状态流转</a>
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum TradeAfterSaleStatusEnum implements IntArrayValuable {

    APPLY(10,"申请中", // 【申请售后】
            "会员申请退款"),
    SELLER_AGREE(20, "卖家通过", // 卖家通过售后；【商品待退货】
            "商家同意退款"),
    BUYER_DELIVERY(30,"待卖家收货", // 买家已退货，等待卖家收货；【商家待收货】
            "会员填写退货物流信息"),
    WAIT_REFUND(40, "等待平台退款", // 卖家已收货，等待平台退款；等待退款【等待退款】
            "商家收货"),
    COMPLETE(50, "完成", // 完成退款【退款成功】
            "商家确认退款"),

    BUYER_CANCEL(61, "买家取消售后", // 【买家取消】
            "会员取消退款"),
    SELLER_DISAGREE(62,"卖家拒绝", // 卖家拒绝售后；商家拒绝【商家拒绝】
            "商家拒绝退款"),
    SELLER_REFUSE(63,"卖家拒绝收货", // 卖家拒绝收货，终止售后；【商家拒收货】
            "商家拒绝收货"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeAfterSaleStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;
    /**
     * 操作内容
     *
     * 目的：记录售后日志的内容
     */
    private final String content;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static TradeAfterSaleStatusEnum valueOf(Integer status) {
        return firstMatch(value -> value.getStatus().equals(status), values());
    }

}
