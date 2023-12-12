package cn.iocoder.yudao.module.crm.enums;

/**
 * CRM 操作日志枚举
 *
 * @author HUIHUI
 */
public interface LogRecordConstants {

    String WHO = "【{getAdminUserById{#userId}}】";

    //======================= 客户转移操作日志 =======================

    String TRANSFER_CUSTOMER_LOG_TYPE = "客户转移";
    String TRANSFER_CUSTOMER_LOG_SUCCESS = WHO + "把客户【{{#crmCustomer.name}}】负责人【{getAdminUserById{#crmCustomer.ownerUserId}}】转移给了【{getAdminUserById{#reqVO.newOwnerUserId}}】";
    String TRANSFER_CUSTOMER_LOG_FAIL = "";

}
