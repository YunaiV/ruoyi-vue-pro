package cn.iocoder.yudao.module.pay.enums.demo;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 示例提现单的类型枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum PayDemoWithdrawTypeEnum implements ArrayValuable<Integer> {

    WECHAT(2, "微信"),
    ALIPAY(1, "支付宝"),
    WALLET(3, "钱包"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PayDemoWithdrawTypeEnum::getType).toArray(Integer[]::new);

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

}
