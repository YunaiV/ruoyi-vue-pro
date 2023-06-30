package cn.iocoder.yudao.module.pay.enums.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包交易大类枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum WalletTransactionGategoryEnum {
    TOP_UP(1, "充值"),
    SPENDING(2, "支出");

    /**
     * 分类
     */
    private final Integer category;

    /**
     * 说明
     */
    private final String desc;
}
