package cn.iocoder.yudao.module.erp.enums;


public interface ErpStateMachines {
    //采购申请主表开关状态机
    String PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME = "purchaseRequestOffStateMachine";
    //采购申请审核状态机
    String PURCHASE_REQUEST_STATE_MACHINE_NAME = "purchaseRequestAuditStateMachine";
    //采购主表采购状态机
    String PURCHASE_ORDER_STATE_MACHINE_NAME = "purchaseOrderStateMachine";
    //采购申请子项开关状态机
    String PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME = "purchaseRequestItemOffStateMachine";
    //采购子项采购状态机
    String PURCHASE_ORDER_ITEM_STATE_MACHINE_NAME = "purchaseOrderItemStateMachine";
}
