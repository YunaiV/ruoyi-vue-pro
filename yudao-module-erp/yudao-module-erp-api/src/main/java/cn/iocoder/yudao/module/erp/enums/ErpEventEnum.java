package cn.iocoder.yudao.module.erp.enums;

import lombok.Getter;

@Getter
public enum ErpEventEnum {
    // 审核事件
    INIT("审核初始化"),
    SUBMIT_FOR_REVIEW("提交审核"),
    AGREE("审核通过"),
    REJECT("审核不通过"),
    WITHDRAW_REVIEW("反审核"),

    // 关闭状态相关事件
    OFF_INIT("开关初始化"),
    ACTIVATE("开启"),
    MANUAL_CLOSE("手动关闭"),
    AUTO_CLOSE("自动关闭"),

    // 采购相关事件
    START_PURCHASE("开始采购"),
    PURCHASE_IN_PROGRESS("采购进行中"),
    PURCHASE_COMPLETE("采购完成"),
    PURCHASE_CANCEL("采购取消"),

    // 入库状态相关事件
    INIT_STORAGE("入库初始化"),
    ADD_TO_STORAGE("添加库存"),
    REMOVE_FROM_STORAGE("减少库存"),
//    STOCK_ADJUSTMENT("库存调整"),


    ;
    private final String desc;

    ErpEventEnum(String desc) {
        this.desc = desc;
    }
}
