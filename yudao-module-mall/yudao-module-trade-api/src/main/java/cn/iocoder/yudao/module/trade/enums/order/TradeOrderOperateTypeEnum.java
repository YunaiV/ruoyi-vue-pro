package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单操作类型的枚举
 *
 * @author 陈賝
 * @since 2023/7/6 15:31
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderOperateTypeEnum {

    MEMBER_CREATE(1, "用户下单"),
    MEMBER_RECEIVE(30, "用户已收货"),
    MEMBER_COMMENT(31, "用户评价"),
    MEMBER_CANCEL(40, "手动取消订单"),
    SYSTEM_CANCEL(41, "系统取消订单"),
    // 42 预留：管理员取消订单
    MEMBER_DELETE(43, "删除订单"),
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型
     */
    private final String content;

}
