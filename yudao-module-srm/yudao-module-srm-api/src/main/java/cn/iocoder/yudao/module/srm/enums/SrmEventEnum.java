package cn.iocoder.yudao.module.srm.enums;

import lombok.Getter;

@Getter
public enum SrmEventEnum {
    // 审核事件
    AUDIT_INIT("审核初始化"),
    SUBMIT_FOR_REVIEW("提交审核"),
    AGREE("审核通过"),
    REJECT("审核不通过"),
    WITHDRAW_REVIEW("反审核"),

    // 关闭事件
    OFF_INIT("开关初始化"),
    ACTIVATE("开启"),
    MANUAL_CLOSE("手动关闭"),
    AUTO_CLOSE("自动关闭"),
    CANCEL_DELETE("关闭撤销"),
//    SELF_INSPECTION("申请单主子表开关自检"),

    // 订购事件
    ORDER_INIT("订购初始化"),
    ORDER_ADJUSTMENT("订购数量调整"),
    ORDER_CANCEL("放弃采购"),


//    // 采购事件
//    START_PURCHASE("采购初始化"),
//    PURCHASE_ADJUSTMENT("采购单修改"),
//    PURCHASE_COMPLETE("采购完成"),
//    ACCEPTANCE_PASS("验收通过"),
//    ACCEPTANCE_FAIL("验收不通过"),
//    PURCHASE_FAILED("采购失败"),
//    PURCHASE_CANCELLED("取消采购订单"),

    // 执行事件
    EXECUTION_INIT("执行初始化"),
    START_EXECUTION("开始执行"),
    COMPLETE_EXECUTION("执行完成"),
    PAUSE_EXECUTION("暂停执行"),
    RESUME_EXECUTION("恢复执行"),
    CANCEL_EXECUTION("取消执行"),
    EXECUTION_FAILED("执行失败"),

    //付款事件
    PAYMENT_INIT("付款初始化"),
    // PARTIAL_PAYMENT("部分付款"),
    COMPLETE_PAYMENT("完成付款"),
    CANCEL_PAYMENT("取消付款"),
    PAYMENT_EXCEPTION("付款异常"),
    PAYMENT_ADJUSTMENT("付款调整"),

    //退款事件
    RETURN_INIT("退款初始化"),
    RETURN_ADJUSTMENT("退款调整"),
    RETURN_CANCEL("取消退款"),
    RETURN_EXCEPTION("退款异常"),
    //退款完成
    RETURN_COMPLETE("退款完成"),


    //入库事件
    STORAGE_INIT("入库初始化"),
    STOCK_ADJUSTMENT("库存调整"),
    //    PARTIAL_STORAGE("部分入库"),
//    COMPLETE_STORAGE("完成入库"),
    CANCEL_STORAGE("取消入库"),
//    STORAGE_EXCEPTION("入库异常"),
    ;
    private final String desc;

    SrmEventEnum(String desc) {
        this.desc = desc;
    }

}
