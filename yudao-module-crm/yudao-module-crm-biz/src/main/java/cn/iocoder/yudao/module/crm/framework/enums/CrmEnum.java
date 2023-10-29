package cn.iocoder.yudao.module.crm.framework.enums;

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
    CRM_PRODUCT(4, "产品"),
    CRM_BUSINESS(5, "商机"),
    CRM_CONTRACT(6, "合同"),
    CRM_RECEIVABLES(7, "回款"),
    CRM_RECEIVABLES_PLAN(8, "回款计划"),
    CRM_CUSTOMER_POOL(9, "客户公海");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

}
