package cn.iocoder.yudao.module.pay.enums.member;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 钱包明细查询类型
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum WalletTransactionQueryTypeEnum implements IntArrayValuable  {
    RECHARGE(1, "充值"),
    EXPENSE(2, "消费");

    private final Integer type;

    private final String desc;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(WalletTransactionQueryTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static WalletTransactionQueryTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(o -> o.getType().equals(type), values());
    }
}
