package cn.iocoder.yudao.module.trade.enums.aftersale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 售后操作类型的枚举
 *
 * @author 陈賝
 * @since 2023/6/13 13:53
 */
@RequiredArgsConstructor
@Getter
public enum AfterSaleOperateTypeEnum {

    MEMBER_CREATE(10, "会员申请退款"),
    ADMIN_AGREE_APPLY(11, "商家同意退款"),
    ADMIN_DISAGREE_APPLY(12, "商家拒绝退款"),
    MEMBER_DELIVERY(20, "会员填写退货物流信息，快递公司：{deliveryName}，快递单号：{logisticsNo}"),
    ADMIN_AGREE_RECEIVE(21, "商家收货"),
    ADMIN_DISAGREE_RECEIVE(22, "商家拒绝收货，原因：{reason}"),
    ADMIN_REFUND(30, "商家退款"),
    MEMBER_CANCEL(40, "会员取消退款"),
    ;

    /**
     * 操作类型
     */
    private final Integer type;
    /**
     * 操作描述
     */
    private final String content;

}
