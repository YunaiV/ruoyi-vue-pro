package cn.iocoder.yudao.module.crm.enums.operatelog;

/**
 * functionName 常量枚举
 * 方便别的模块调用
 *
 * @author HUIHUI
 */
// TODO @puhui999：这个枚举，还是放在对应的 Function 里好。主要考虑，和 Function 实现可以更近一点哈
public interface CrmParseFunctionNameConstants {

    String GET_CONTACT_BY_ID = "getContactById"; // 获取联系人信息
    String GET_CUSTOMER_BY_ID = "getCustomerById"; // 获取客户信息
    String GET_CUSTOMER_INDUSTRY = "getCustomerIndustry"; // 获取客户行业信息
    String GET_CUSTOMER_LEVEL = "getCustomerLevel"; // 获取客户级别
    String GET_CUSTOMER_SOURCE = "getCustomerSource"; // 获取客户来源
    String GET_CONTRACT_BY_ID = "getContractById"; // 获取合同信息

}