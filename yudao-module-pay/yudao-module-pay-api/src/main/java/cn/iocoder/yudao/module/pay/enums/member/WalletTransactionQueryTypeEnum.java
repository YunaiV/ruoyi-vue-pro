package cn.iocoder.yudao.module.pay.enums.member;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO @jason：可以简化，直接 PageVO 定义两个 Integer；
/**
 * 钱包流水查询类型
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum WalletTransactionQueryTypeEnum implements IntArrayValuable  {

    RECHARGE(1, "充值"),
    EXPENSE(2, "消费");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(WalletTransactionQueryTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static WalletTransactionQueryTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(o -> o.getType().equals(type), values());
    }

}
