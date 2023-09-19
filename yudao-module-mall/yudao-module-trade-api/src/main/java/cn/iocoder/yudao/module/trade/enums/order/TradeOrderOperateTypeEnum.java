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
    TEST(2, "用户({nickname})做了({thing})"),
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
