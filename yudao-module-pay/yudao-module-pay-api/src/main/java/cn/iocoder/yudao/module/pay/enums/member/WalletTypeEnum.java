package cn.iocoder.yudao.module.pay.enums.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包交易大分类
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum WalletTypeEnum {
    RECHARGE(1, "充值"),
    EXPENSE(2, "消费");

    private final Integer type;

    private final String desc;
}
