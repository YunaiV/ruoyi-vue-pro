package cn.iocoder.yudao.module.tms.enums;

import lombok.Getter;

@Getter
public enum TmsEventEnum {
    // 审核事件
    AUDIT_INIT("审核初始化"), SUBMIT_FOR_REVIEW("提交审核"), AGREE("审核通过"), REJECT("审核不通过"), WITHDRAW_REVIEW("反审核"),

    // 关闭事件
    OFF_INIT("开关初始化"), ACTIVATE("开启"), MANUAL_CLOSE("手动关闭"), AUTO_CLOSE("自动关闭"), CANCEL_DELETE("关闭撤销"),

    // 订购事件
    ORDER_INIT("订购初始化"), ORDER_ADJUSTMENT("订购数量调整"), ORDER_CANCEL("放弃采购"),

    ;
    private final String desc;

    TmsEventEnum(String desc) {
        this.desc = desc;
    }

}
