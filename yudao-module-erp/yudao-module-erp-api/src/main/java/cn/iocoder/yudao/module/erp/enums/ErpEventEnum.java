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
    ORDER_ADJUSTMENT("订购数量调整"),
    ORDER_CANCEL("放弃采购"),


    // 采购事件
    START_PURCHASE("采购初始化"),
    PURCHASE_ADJUSTMENT("采购单修改"),
    PURCHASE_COMPLETE("采购完成"),
    ACCEPTANCE_PASS("验收通过"),
    ACCEPTANCE_FAIL("验收不通过"),
    PURCHASE_FAILED("采购失败"),
    PURCHASE_CANCELLED("取消采购订单"),

    // 入库事件
    STORAGE_INIT("入库初始化"),
    STOCK_ADJUSTMENT("库存调整"),
    STORAGE_COMPLETE("入库完成"),

    // 执行事件
    /**
     * 执行初始化：
     * 1. 触发时机：采购订单创建并且审核通过后
     * 2. 前置状态：无
     * 3. 后置状态：PENDING（待执行）
     */
    EXECUTION_INIT("执行初始化"),

    /**
     * 开始执行：
     * 1. 触发时机：采购人员开始实际采购活动
     * 2. 前置状态：PENDING（待执行）
     * 3. 后置状态：IN_PROGRESS（执行中）
     * 4. 业务行为：开始联系供应商、发起询价等
     */
    START_EXECUTION("开始执行"),

    /**
     * 执行完成：
     * 1. 触发时机：采购任务完成
     * 2. 前置状态：IN_PROGRESS（执行中）
     * 3. 后置状态：COMPLETED（已完成）
     * 4. 业务行为：供应商已确认、商品已发货等
     */
    COMPLETE_EXECUTION("执行完成"),

    /**
     * 暂停执行：
     * 1. 触发时机：需要临时暂停采购活动
     * 2. 前置状态：IN_PROGRESS（执行中）
     * 3. 后置状态：PAUSED（已暂停）
     * 4. 业务行为：等待供应商报价、等待预算审批等
     */
    PAUSE_EXECUTION("暂停执行"),

    /**
     * 恢复执行：
     * 1. 触发时机：暂停的采购活动可以继续
     * 2. 前置状态：PAUSED（已暂停）
     * 3. 后置状态：IN_PROGRESS（执行中）
     * 4. 业务行为：收到供应商报价、预算审批通过等
     */
    RESUME_EXECUTION("恢复执行"),

    /**
     * 取消执行：
     * 1. 触发时机：采购订单需要取消
     * 2. 前置状态：PENDING、IN_PROGRESS、PAUSED
     * 3. 后置状态：CANCELLED（已取消）
     * 4. 业务行为：供应商无法供货、采购需求取消等
     */
    CANCEL_EXECUTION("取消执行"),

    /**
     * 执行失败：
     * 1. 触发时机：采购过程中遇到无法继续的问题
     * 2. 前置状态：IN_PROGRESS（执行中）
     * 3. 后置状态：FAILED（执行失败）
     * 4. 业务行为：供应商拒绝供货、价格谈判失败等
     * 5. 后续处理：可以重新发起执行
     */
    EXECUTION_FAILED("执行失败"),


    PAYMENT_INIT("支付初始化"),
    PARTIAL_PAYMENT("部分支付"), 
    COMPLETE_PAYMENT("完成支付"),
    PAYMENT_FAILED("支付失败")
    ;
    private final String desc;

    ErpEventEnum(String desc) {
        this.desc = desc;
    }

}
