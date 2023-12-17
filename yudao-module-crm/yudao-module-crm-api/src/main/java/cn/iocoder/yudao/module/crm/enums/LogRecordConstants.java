package cn.iocoder.yudao.module.crm.enums;

// TODO 芋艿：操作日志；看看这个类怎么搞个好点的规范；
/**
 * CRM 操作日志枚举
 *
 * @author HUIHUI
 */
public interface LogRecordConstants {

    //======================= 客户模块类型 =======================
    // TODO puhui999: 确保模块命名方式为 module + 子模块名称的方式。统一定义模块名称是为了方便查询各自记录的操作日志，列如说：查询客户【张三的操作日志】就可以 module + bizId
    String CRM_LEADS = "CRM 线索";
    String CRM_CUSTOMER = "CRM 客户";
    String CRM_CONTACT = "CRM 联系人";
    String CRM_BUSINESS = "CRM 商机";
    String CRM_CONTRACT = "CRM 合同";
    String CRM_PRODUCT = "CRM 产品";
    String CRM_RECEIVABLE = "CRM 回款";
    String CRM_RECEIVABLE_PLAN = "CRM 回款计划";

    //======================= 客户转移操作日志 =======================

    String TRANSFER_CUSTOMER_LOG_SUCCESS = "把客户【{{#crmCustomer.name}}】的负责人从【{getAdminUserById{#crmCustomer.ownerUserId}}】变更为了【{getAdminUserById{#reqVO.newOwnerUserId}}】";
    String TRANSFER_CUSTOMER_LOG_FAIL = ""; // TODO @puhui999：这个可以删除哈，一般不搞失败的日志

}
