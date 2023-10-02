package cn.iocoder.yudao.module.trade.enums.aftersale;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;

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
public enum AfterSaleStatusEnum implements IntArrayValuable {

    /**
     * 【申请售后】
     */
    APPLY(10,"申请中", "会员申请退款"), // 有赞的状态提示：退款申请待商家处理
    /**
     * 卖家通过售后；【商品待退货】
     */
    SELLER_AGREE(20, "卖家通过", "商家同意退款"), // 有赞的状态提示：请退货并填写物流信息
    /**
     * 买家已退货，等待卖家收货；【商家待收货】
     */
    BUYER_DELIVERY(30,"待卖家收货", "会员填写退货物流信息"), // 有赞的状态提示：退货退款申请待商家处理
    /**
     * 卖家已收货，等待平台退款；等待退款【等待退款】
     */
    WAIT_REFUND(40, "等待平台退款", "商家收货"), // 有赞的状态提示：无（有赞无该状态）
    /**
     * 完成退款【退款成功】
     */
    COMPLETE(50, "完成", "商家确认退款"), // 有赞的状态提示：退款成功
    /**
     * 【买家取消】
     */
    BUYER_CANCEL(61, "买家取消售后", "会员取消退款"), // 有赞的状态提示：退款关闭
    /**
     * 卖家拒绝售后；商家拒绝【商家拒绝】
     */
    SELLER_DISAGREE(62,"卖家拒绝", "商家拒绝退款"), // 有赞的状态提示：商家不同意退款申请
    /**
     * 卖家拒绝收货，终止售后；【商家拒收货】
     */
    SELLER_REFUSE(63,"卖家拒绝收货", "商家拒绝收货"), // 有赞的状态提示：商家拒绝收货，不同意退款
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AfterSaleStatusEnum::getStatus).toArray();

    /**
     * 进行中的售后状态
     *
     * 不包括已经结束的状态
     */
    public static final Collection<Integer> APPLYING_STATUSES = Arrays.asList(
            APPLY.getStatus(),
            SELLER_AGREE.getStatus(),
            BUYER_DELIVERY.getStatus(),
            WAIT_REFUND.getStatus()
    );

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

    public static AfterSaleStatusEnum valueOf(Integer status) {
        return firstMatch(value -> value.getStatus().equals(status), values());
    }

}
