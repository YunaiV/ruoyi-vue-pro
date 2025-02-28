package cn.iocoder.yudao.module.erp.enums;

import lombok.Getter;

@Getter
public enum ErpEventEnum {
    //审核事件
    INIT(0, "审核状态初始化"),
    SUBMIT_FOR_REVIEW(1, "提交审核"),
    AGREE(2, "审核通过"),
    REJECT(3, "审核不通过"),
    WITHDRAW_REVIEW(4, "撤销审核"),

    // 关闭状态相关事件
    OFF_INIT(7, "开关状态初始化"),
    ACTIVATE(6, "开启"),
    MANUAL_CLOSE(8,"手动关闭"),
    AUTO_CLOSE(9,"自动关闭"),

    ;
    private final Integer status;
    private final String desc;

    ErpEventEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
