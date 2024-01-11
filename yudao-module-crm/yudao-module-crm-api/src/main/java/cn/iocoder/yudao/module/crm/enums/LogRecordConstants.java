package cn.iocoder.yudao.module.crm.enums;

/**
 * CRM 操作日志枚举
 * 目的：统一管理，也减少 Service 里各种“复杂”字符串
 *
 * @author HUIHUI
 */
public interface LogRecordConstants {

    // ======================= CRM_LEADS 线索 =======================

    String CRM_LEADS_TYPE = "CRM 线索";

    // ======================= CRM_CUSTOMER 客户 =======================

    String CRM_CUSTOMER_TYPE = "CRM 客户";
    String CRM_CUSTOMER_CREATE_SUB_TYPE = "创建客户";
    String CRM_CUSTOMER_CREATE_SUCCESS = "创建了客户{{#customer.name}}";
    String CRM_CUSTOMER_UPDATE_SUB_TYPE = "更新客户";
    String CRM_CUSTOMER_UPDATE_SUCCESS = "更新了客户【{{#customerName}}】: {_DIFF{#updateReqVO}}";
    String CRM_CUSTOMER_DELETE_SUB_TYPE = "删除客户";
    String CRM_CUSTOMER_DELETE_SUCCESS = "删除了客户【{{#customerName}}】";
    String CRM_CUSTOMER_TRANSFER_SUB_TYPE = "转移客户";
    String CRM_CUSTOMER_TRANSFER_SUCCESS = "将客户【{{#crmCustomer.name}}】的负责人从【{getAdminUserById{#crmCustomer.ownerUserId}}】变更为了【{getAdminUserById{#reqVO.newOwnerUserId}}】";
    String CRM_CUSTOMER_LOCK_SUB_TYPE = "{{#crmCustomer.lockStatus ? '解锁客户' : '锁定客户'}}";
    String CRM_CUSTOMER_LOCK_SUCCESS = "{{#crmCustomer.lockStatus ? '将客户【' + #crmCustomer.name + '】解锁' : '将客户【' + #crmCustomer.name + '】锁定'}}";
    String CRM_CUSTOMER_POOL_SUB_TYPE = "客户放入公海";
    String CRM_CUSTOMER_POOL_SUCCESS = "将客户【{{#customerName}}】放入了公海";
    String CRM_CUSTOMER_RECEIVE_SUB_TYPE = "{{#ownerUserName != null ? '分配客户' : '领取客户'}}";
    String CRM_CUSTOMER_RECEIVE_SUCCESS = "{{#ownerUserName != null ? '将客户【' + #customer.name + '】分配给【' + #ownerUserName + '】' : '领取客户【' + #customer.name + '】'}}";

    // ======================= CRM_CUSTOMER_LIMIT_CONFIG 客户限制配置 =======================

    String CRM_CUSTOMER_LIMIT_CONFIG_TYPE = "CRM 客户限制配置";
    String CRM_CUSTOMER_LIMIT_CONFIG_CREATE_SUB_TYPE = "创建客户限制配置";
    String CRM_CUSTOMER_LIMIT_CONFIG_CREATE_SUCCESS = "创建了【{{#limitType}}】类型的客户限制配置";
    String CRM_CUSTOMER_LIMIT_CONFIG_UPDATE_SUB_TYPE = "更新客户限制配置";
    String CRM_CUSTOMER_LIMIT_CONFIG_UPDATE_SUCCESS = "更新了客户限制配置: {_DIFF{#updateReqVO}}";
    String CRM_CUSTOMER_LIMIT_CONFIG_DELETE_SUB_TYPE = "删除客户限制配置";
    String CRM_CUSTOMER_LIMIT_CONFIG_DELETE_SUCCESS = "删除了【{{#limitType}}】类型的客户限制配置";

    // ======================= CRM_CUSTOMER_POOL_CONFIG 客户公海规则 =======================

    String CRM_CUSTOMER_POOL_CONFIG_TYPE = "CRM 客户公海规则";
    String CRM_CUSTOMER_POOL_CONFIG_SUB_TYPE = "{{#isPoolConfigUpdate ? '更新客户公海规则' : '创建客户公海规则'}}";
    String CRM_CUSTOMER_POOL_CONFIG_SUCCESS = "{{#isPoolConfigUpdate ? '更新了客户公海规则' : '创建了客户公海规则'}}";

    // ======================= CRM_CONTACT 联系人 =======================

    String CRM_CONTACT_TYPE = "CRM 联系人";

    // ======================= CRM_BUSINESS 商机 =======================

    String CRM_BUSINESS_TYPE = "CRM 商机";

    // ======================= CRM_CONTRACT 合同 =======================

    String CRM_CONTRACT_TYPE = "CRM 合同";

    // ======================= CRM_PRODUCT 产品 =======================

    String CRM_PRODUCT_TYPE = "CRM 产品";
    String CRM_PRODUCT_CREATE_SUB_TYPE = "创建产品";
    String CRM_PRODUCT_CREATE_SUCCESS = "创建了产品【{{#createReqVO.name}}】";
    String CRM_PRODUCT_UPDATE_SUB_TYPE = "更新产品";
    String CRM_PRODUCT_UPDATE_SUCCESS = "更新了产品【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String CRM_PRODUCT_DELETE_SUB_TYPE = "删除产品";
    String CRM_PRODUCT_DELETE_SUCCESS = "删除了产品【{{#product.name}}】";

    // ======================= CRM_PRODUCT_CATEGORY 产品分类 =======================

    String CRM_PRODUCT_CATEGORY_TYPE = "CRM 产品分类";
    String CRM_PRODUCT_CATEGORY_CREATE_SUB_TYPE = "创建产品分类";
    String CRM_PRODUCT_CATEGORY_CREATE_SUCCESS = "创建了产品分类【{{#createReqVO.name}}】";
    String CRM_PRODUCT_CATEGORY_UPDATE_SUB_TYPE = "更新产品分类";
    String CRM_PRODUCT_CATEGORY_UPDATE_SUCCESS = "更新了产品分类【{{#updateReqVO.name}}】: {_DIFF{#updateReqVO}}";
    String CRM_PRODUCT_CATEGORY_DELETE_SUB_TYPE = "删除产品分类";
    String CRM_PRODUCT_CATEGORY_DELETE_SUCCESS = "删除了产品分类【{{#productCategory.name}}】";

    // ======================= CRM_RECEIVABLE 回款 =======================

    String CRM_RECEIVABLE_TYPE = "CRM 回款";

    // ======================= CRM_RECEIVABLE_PLAN 回款计划 =======================

    String CRM_RECEIVABLE_PLAN_TYPE = "CRM 回款计划";

}
