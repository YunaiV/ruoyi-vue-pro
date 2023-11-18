package cn.iocoder.yudao.module.crm.framework.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Crm 类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum CrmBizTypeEnum implements IntArrayValuable {

    CRM_PERMISSION(0, "团队"), // CrmPermissionController 中使用
    CRM_LEADS(1, "线索"),
    CRM_CUSTOMER(2, "客户"),
    CRM_CONTACTS(3, "联系人"),
    CRM_BUSINESS(5, "商机"),
    CRM_CONTRACT(6, "合同");

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
