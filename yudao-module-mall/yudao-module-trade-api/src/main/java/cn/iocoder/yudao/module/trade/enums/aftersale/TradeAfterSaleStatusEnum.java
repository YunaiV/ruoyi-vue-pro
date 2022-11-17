package cn.iocoder.yudao.module.trade.enums.aftersale;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后状态的枚举
 *
 * <a href="https://www.processon.com/view/link/63731a270e3e742ce7b7c194">状态流转</a>
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum TradeAfterSaleStatusEnum {

    APPLY(10,"申请中"),
    SELLER_AGREE(20, "卖家通过"), // 卖家通过售后
    BUYER_DELIVERY(30,"待卖家收货"), // 买家已退货，等待卖家收货
    WAIT_REFUND(40, "等待平台退款"), // 卖家已收货，等待平台退款
    COMPLETE(50, "完成"), // 完成退款

    BUYER_CANCEL(61, "买家取消售后"),
    SELLER_DISAGREE(62,"卖家拒绝"), // 卖家拒绝售后
    SELLER_REFUSE(63,"卖家拒绝收货"), // 卖家拒绝收货，终止售后
    ;

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
