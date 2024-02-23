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
public enum CrmReceivableReturnTypeEnum implements IntArrayValuable {

    CHECK(1, "支票"),
    CASH(2, "现金"),
    POSTAL_REMITTANCE(3, "邮政汇款"),
    TELEGRAPHIC_TRANSFER(4, "电汇"),
    ONLINE_TRANSFER(5, "网上转账"),
    ALIPAY(6, "支付宝"),
    WECHAT_PAY(7, "微信支付"),
    OTHER(8, "其它");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmReceivableReturnTypeEnum::getType).toArray();

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
