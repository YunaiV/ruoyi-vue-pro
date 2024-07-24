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

    //======================= 订阅消息模版 =======================

    // TODO @puhui999：建议 TRADE_AFTER_SALE_CHANGE
    String ORDER_AFTERSALE_CHANGE = "售后进度通知";

    // TODO @puhui999：是不是改成 PAY_WALLET_CHANGE 放在 PAY 模块
    String MONEY_CHANGE = "充值成功通知";

}
