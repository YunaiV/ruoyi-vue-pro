package cn.iocoder.yudao.module.crm.enums.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * CRM 业务类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum CrmBizTypeEnum implements IntArrayValuable {

    CRM_CLUE(1, "线索"),
    CRM_CUSTOMER(2, "客户"),
    CRM_CONTACT(3, "联系人"),
    CRM_BUSINESS(4, "商机"),
    CRM_CONTRACT(5, "合同"),
    CRM_PRODUCT(6, "产品"),
    CRM_RECEIVABLE(7, "回款"),
    CRM_RECEIVABLE_PLAN(8, "回款计划")
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CrmBizTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static String getNameByType(Integer type) {
        CrmBizTypeEnum typeEnum = CollUtil.findOne(CollUtil.newArrayList(CrmBizTypeEnum.values()),
                item -> ObjUtil.equal(item.type, type));
        return typeEnum == null ? null : typeEnum.getName();
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
