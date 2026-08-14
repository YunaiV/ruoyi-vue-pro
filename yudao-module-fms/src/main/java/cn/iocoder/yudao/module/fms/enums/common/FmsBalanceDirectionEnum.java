package cn.iocoder.yudao.module.fms.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * FMS 余额方向枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsBalanceDirectionEnum {

    DEBIT("借"),
    CREDIT("贷"),
    FLAT("平");

    /**
     * 名字
     */
    private final String name;

    public static FmsBalanceDirectionEnum valueOf(BigDecimal balance) {
        return balance.signum() > 0 ? DEBIT : balance.signum() < 0 ? CREDIT : FLAT;
    }

}
