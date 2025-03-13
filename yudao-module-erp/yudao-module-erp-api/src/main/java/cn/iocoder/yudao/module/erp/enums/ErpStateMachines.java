package cn.iocoder.yudao.module.erp.enums;


public interface ErpStateMachines {
    // ========== ERP 采购申请单 ==========
    //采购申请主表开关状态机
    String PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME = "purchaseRequestOffStateMachine";
    //采购申请审核状态机
    String PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME = "purchaseRequestAuditStateMachine";
    //采购主表采购状态机
    String PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME = "purchaseRequestOrderStateMachine";
    //采购申请子项开关状态机
    String PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME = "purchaseRequestItemOffStateMachine";
    //采购子项采购状态机
    String PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME = "purchaseRequestItemStateMachine";

    // ========== ERP 采购订单 ==========
    //订单开关状态机
    String PURCHASE_ORDER_OFF_STATE_MACHINE_NAME = "purchaseOrderOffStateMachine";
    //订单审核状态机
    String PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME = "purchaseOrderAuditStateMachine";
    //订单执行状态机
    String PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME = "purchaseOrderExecutionStateMachine";
    //订单入库状态机
    String PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME = "purchaseOrderStorageStateMachine";
    //订单付款状态机
    String PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME = "purchaseOrderPaymentStateMachine";
    //订单采购状态机
    String PURCHASE_ORDER_PURCHASE_STATE_MACHINE_NAME = "purchaseOrderPurchaseStateMachine";
    // ========== ERP 采购订单子项 ==========
    //订单子项开关状态机
    String PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME = "purchaseOrderItemOffStateMachine";
    //订单子项执行状态机
    String PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME = "purchaseOrderItemExecutionStateMachine";
    //订单子项入库状态机
    String PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME = "purchaseOrderItemStorageStateMachine";
    //订单子项付款状态机
    String PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME = "purchaseOrderItemPaymentStateMachine";
    //订单子项采购状态机
    String PURCHASE_ORDER_ITEM_PURCHASE_STATE_MACHINE_NAME = "purchaseOrderItemPurchaseStateMachine";

    // ========== ERP 入库单 ==========

    // ========== ERP 出库单 ==========
    // ========== ERP 付款单 ==========
}
