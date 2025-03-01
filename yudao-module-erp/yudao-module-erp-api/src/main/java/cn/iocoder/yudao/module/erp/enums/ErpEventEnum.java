package cn.iocoder.yudao.module.erp.enums;

import lombok.Getter;

@Getter
public enum ErpEventEnum {
    // 审核事件
    INIT("审核状态初始化"),
    SUBMIT_FOR_REVIEW("提交审核"),
    AGREE("审核通过"),
    REJECT("审核不通过"),
    WITHDRAW_REVIEW("反审核"),

    // 关闭状态相关事件
    OFF_INIT("开关状态初始化"),
    ACTIVATE("开启"),
    MANUAL_CLOSE("手动关闭"),
    AUTO_CLOSE("自动关闭");

    private final String desc;

    ErpEventEnum(String desc) {
        this.desc = desc;
    }
}
