package cn.iocoder.yudao.module.erp.enums;

import lombok.Getter;

@Getter
public enum ErpEventEnum {
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
//    SELF_INSPECTION("申请单主子表开关自检"),

    // 订购事件
    ORDER_INIT("订购初始化"),
    ORDER_GOODS("订购商品调整"),
    ORDER_CANCEL("放弃订购"),
    ORDER_COMPLETE("订购完成"),


    // 采购事件
    START_PURCHASE("开始采购"),
    PURCHASE_COMPLETE("采购完成"),
    ACCEPTANCE_PASS("验收通过"),
    ACCEPTANCE_FAIL("验收不通过"),
    PURCHASE_FAILED("采购失败"),
    PURCHASE_CANCELLED("采购取消"),

    // 入库事件
    STORAGE_INIT("入库初始化"),
    STOCK_ADJUSTMENT("库存调整"),
    ADD_STOCK("添加库存"),
    REDUCE_STOCK("减少库存"),


    ;
    private final String desc;

    ErpEventEnum(String desc) {
        this.desc = desc;
    }

    /*
    * 待采购	开始采购	采购申请单审核通过后，采购人员开始执行采购操作。
      采购中	采购完成	采购过程完成，所有商品已采购完毕。
      待验收	验收通过	物品到货后进行验收，物品合格可以进入下一个环节。
      验收不通过	物品验收未通过，需要重新处理或退货。
      已验收	入库	物品验收通过后，正式入库。
      已入库	采购完成	物品入库完成，采购流程结束。
      采购失败	重新采购	采购失败后，重新发起采购操作。
      采购取消	重新发起采购	采购取消后，重新开始采购流程。
    * */
}
