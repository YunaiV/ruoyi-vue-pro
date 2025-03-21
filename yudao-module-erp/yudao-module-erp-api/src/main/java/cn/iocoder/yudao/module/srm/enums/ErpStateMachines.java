package cn.iocoder.yudao.module.srm.enums;


public interface ErpStateMachines {
    // ========== ERP 采购申请单 ==========
    //采购申请主表开关状态机
    String PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME = "purchaseRequestOff";
    //采购申请审核状态机
    String PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME = "purchaseRequestAudit";
    //采购主表采购状态机
    String PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME = "purchaseRequestOrder";
    //采购主表入库状态机
    String PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME = "purchaseRequestStorage";
    // ========== ERP 采购申请单子表 ==========
    //采购申请子项开关状态机
    String PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME = "purchaseRequestItemOff";
    //采购子项采购状态机
    String PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME = "purchaseRequestItem";
    //采购申请项入库状态机
    String PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME = "purchaseRequestItemStorage";

    // ========== ERP 采购订单 ==========
    //订单开关状态机
    String PURCHASE_ORDER_OFF_STATE_MACHINE_NAME = "purchaseOrderOff";
    //订单审核状态机
    String PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME = "purchaseOrderAudit";
    //订单执行状态机
    String PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME = "purchaseOrderExecution";
    //订单入库状态机
    String PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME = "purchaseOrderStorage";
    //订单付款状态机
    String PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME = "purchaseOrderPayment";
    //订单采购状态机
    String PURCHASE_ORDER_PURCHASE_STATE_MACHINE_NAME = "purchaseOrderPurchase";
    // ========== ERP 采购订单子项 ==========
    //订单子项开关状态机
    String PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME = "purchaseOrderItemOff";
    //订单子项执行状态机
    String PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME = "purchaseOrderItemExecution";
    //订单子项入库状态机
    String PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME = "purchaseOrderItemStorage";
    //订单子项付款状态机
    String PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME = "purchaseOrderItemPayment";
    //订单子项采购状态机
    String PURCHASE_ORDER_ITEM_PURCHASE_STATE_MACHINE_NAME = "purchaseOrderItemPurchase";

    // ========== ERP 入库单主项 ==========
    String PURCHASE_IN_AUDIT_STATE_MACHINE = "purchaseInAudit";
    String PURCHASE_IN_PAYMENT_STATE_MACHINE = "purchaseInPay";
    // ========== ERP 入库单子项 ==========
    String PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE = "purchaseInItemPay";
    // ========== ERP 退货单 ==========
    //审核
    String PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME = "purchaseReturnAudit";
    //退款
    String PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME = "purchaseReturnRefund";
    // ========== ERP 退货单子项 ==========
    String PURCHASE_RETURN_ITEM_REFUND_STATE_MACHINE_NAME = "purchaseReturnItemRefund";


}
