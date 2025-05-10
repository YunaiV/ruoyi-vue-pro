package cn.iocoder.yudao.module.trade.enums.brokerage;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
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
public enum BrokerageWithdrawTypeEnum implements ArrayValuable<Integer> {

    WALLET(1, "钱包"),
    BANK(2, "银行卡"),
    WECHAT_QR(3, "微信收款码"), // 手动打款
    ALIPAY_QR(4, "支付宝收款码"), // 手动打款
    WECHAT_API(5, "微信零钱"), // 自动打款，通过微信转账 API
    ALIPAY_API(6, "支付宝余额"), // 自动打款，通过支付宝转账 API
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BrokerageWithdrawTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 是否通过支付平台的 API 打款
     *
     * @param type 类型
     * @return 是否
     */
    public static boolean isApi(Integer type) {
        return ObjectUtils.equalsAny(type, WALLET.getType(), ALIPAY_API.getType(), WECHAT_API.getType());
    }

}
