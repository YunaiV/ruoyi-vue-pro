package cn.iocoder.yudao.module.crm.enums.customer;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 客户限制配置规则类型
 *
 * @author Wanwan
 */
@Getter
@AllArgsConstructor
public enum CrmCustomerLimitConfigTypeEnum implements IntArrayValuable {

    /**
     * 拥有客户数限制
     */
    CUSTOMER_QUANTITY_LIMIT(1, "拥有客户数限制"),
    /**
     * 锁定客户数限制
     */
    CUSTOMER_LOCK_LIMIT(2, "锁定客户数限制"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmCustomerLimitConfigTypeEnum::getCode).toArray();

    /**
     * 状态
     */
    private final Integer code;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
