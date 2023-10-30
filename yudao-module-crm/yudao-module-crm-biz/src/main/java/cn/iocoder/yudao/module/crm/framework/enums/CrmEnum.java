package cn.iocoder.yudao.module.crm.framework.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Crm 类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum CrmEnum {

    CRM_LEADS(1, "线索"),
    CRM_CUSTOMER(2, "客户"),
    CRM_CONTACTS(3, "联系人"),
    CRM_BUSINESS(5, "商机"),
    CRM_CONTRACT(6, "合同");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    public static String getNameByType(Integer type) {
        for (CrmEnum crmEnum : CrmEnum.values()) {
            if (ObjUtil.equal(crmEnum.type, type)) {
                return crmEnum.name;
            }
        }
        return "";
    }

}
