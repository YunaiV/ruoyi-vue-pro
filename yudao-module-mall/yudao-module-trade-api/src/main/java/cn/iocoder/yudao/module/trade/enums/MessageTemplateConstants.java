package cn.iocoder.yudao.module.trade.enums;

// TODO @芋艿：枚举
/**
 * 通知模板枚举类
 *
 * @author HUIHUI
 */
public interface MessageTemplateConstants {

    String ORDER_DELIVERY = "order_delivery"; // 短信模版编号

    String BROKERAGE_WITHDRAW_AUDIT_APPROVE = "brokerage_withdraw_audit_approve"; // 佣金提现（审核通过）
    String BROKERAGE_WITHDRAW_AUDIT_REJECT = "brokerage_withdraw_audit_reject"; // 佣金提现（审核不通过）

    //======================= 小程序订阅消息模版 =======================

    String TRADE_AFTER_SALE_CHANGE = "售后进度通知";

}
