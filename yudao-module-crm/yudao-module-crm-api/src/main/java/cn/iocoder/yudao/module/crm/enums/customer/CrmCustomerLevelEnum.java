package cn.iocoder.yudao.module.crm.enums.customer;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 客户等级
 *
 * @author Wanwan
 */
@Getter
@AllArgsConstructor
public enum CrmCustomerLevelEnum implements IntArrayValuable {

    IMPORTANT(1, "A（重点客户）"),
    GENERAL(2, "B（普通客户）"),
    LOW_PRIORITY(3, "C（非优先客户）");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmCustomerLevelEnum::getLevel).toArray();

    /**
     * 状态
     */
    private final Integer level;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
