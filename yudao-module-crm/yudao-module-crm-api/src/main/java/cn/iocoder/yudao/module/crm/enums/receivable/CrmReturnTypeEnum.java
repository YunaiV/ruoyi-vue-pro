package cn.iocoder.yudao.module.crm.enums.receivable;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 回款方式枚举
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum CrmReturnTypeEnum implements IntArrayValuable {

    // 支票
    CHECK(1, "支票"),
    // 现金
    CASH(2, "现金"),
    // 邮政汇款
    POSTAL_REMITTANCE(3, "邮政汇款"),
    // 电汇
    TELEGRAPHIC_TRANSFER(4, "电汇"),
    // 网上转账
    ONLINE_TRANSFER(5, "网上转账"),
    // 支付宝
    ALIPAY(6, "支付宝"),
    // 微信支付
    WECHAT_PAY(7, "微信支付"),
    // 其他
    OTHER(8, "其它");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmReturnTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
