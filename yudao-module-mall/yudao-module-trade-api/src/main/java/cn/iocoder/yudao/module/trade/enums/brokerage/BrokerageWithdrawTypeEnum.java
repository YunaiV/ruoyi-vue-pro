package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 佣金提现类型枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageWithdrawTypeEnum implements IntArrayValuable {

    WALLET(1, "钱包"),
    BANK(2, "银行卡"),
    WECHAT(3, "微信"), // 手动打款
    ALIPAY(4, "支付宝"),
    WECHAT_API(5, "微信零钱"), // 自动打款，通过微信转账 API
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BrokerageWithdrawTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    /**
     * 是否通过支付平台的 API 打款
     *
     * @param type 类型
     * @return 是否
     */
    public static boolean isApi(Integer type) {
        return WECHAT_API.getType().equals(type);
    }

}
