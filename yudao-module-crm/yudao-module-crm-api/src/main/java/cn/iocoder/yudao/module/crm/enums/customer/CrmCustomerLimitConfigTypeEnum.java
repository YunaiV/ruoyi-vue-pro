package cn.iocoder.yudao.module.crm.enums.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
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
    CUSTOMER_OWNER_LIMIT(1, "拥有客户数限制"),
    /**
     * 锁定客户数限制
     */
    CUSTOMER_LOCK_LIMIT(2, "锁定客户数限制"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmCustomerLimitConfigTypeEnum::getType).toArray();

    /**
     * 状态
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    public static String getNameByType(Integer type) {
        CrmCustomerLimitConfigTypeEnum typeEnum = CollUtil.findOne(CollUtil.newArrayList(CrmCustomerLimitConfigTypeEnum.values()),
                item -> ObjUtil.equal(item.type, type));
        return typeEnum == null ? null : typeEnum.getName();
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
