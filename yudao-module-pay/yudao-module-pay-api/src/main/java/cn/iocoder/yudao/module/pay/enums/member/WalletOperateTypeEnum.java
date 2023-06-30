package cn.iocoder.yudao.module.pay.enums.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包操作类型枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum WalletOperateTypeEnum {
    TOP_UP_INC(1, "充值增加"),
    ORDER_DEC(2, "订单消费扣除");
    // TODO 其它类型

    private final Integer type;

    private final String desc;
}
