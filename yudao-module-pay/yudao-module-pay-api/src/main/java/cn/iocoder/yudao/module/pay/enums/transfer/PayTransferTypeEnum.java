package cn.iocoder.yudao.module.pay.enums.transfer;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 转账类型枚举
 *
 * @author jason
 */
@AllArgsConstructor
@Getter
public enum PayTransferTypeEnum implements ArrayValuable<Integer> {

    ALIPAY_BALANCE(1, "支付宝余额"),
    WX_BALANCE(2, "微信余额"),
    BANK_CARD(3, "银行卡"),
    WALLET_BALANCE(4, "钱包余额");

    public interface WxPay {
    }

    public interface Alipay {
    }

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PayTransferTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PayTransferTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
