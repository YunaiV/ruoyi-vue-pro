package cn.iocoder.yudao.module.srm.enums;

public interface SrmStateMachines {

    // ========== SRM 采购申请单 ==========
    String PURCHASE_REQUEST_OFF_STATE_MACHINE = "purchase-request-main-off";
    String PURCHASE_REQUEST_AUDIT_STATE_MACHINE = "purchase-request-main-audit";
    String PURCHASE_REQUEST_ORDER_STATE_MACHINE = "purchase-request-main-order";
    String PURCHASE_REQUEST_STORAGE_STATE_MACHINE = "purchase-request-main-storage";

    // ========== SRM 采购申请单子表 ==========
    String PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME = "purchase-request-item-off";
    String PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME = "purchase-request-item-order";
    String PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME = "purchase-request-item-storage";

    // ========== SRM 采购订单 ==========
    String PURCHASE_ORDER_OFF_STATE_MACHINE_NAME = "purchase-order-main-off";
    String PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME = "purchase-order-main-audit";
    String PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME = "purchase-order-main-execution";
    String PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME = "purchase-order-main-storage";
    String PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME = "purchase-order-main-payment";
    String PURCHASE_ORDER_PURCHASE_STATE_MACHINE_NAME = "purchase-order-main-purchase";

    // ========== SRM 采购订单明细 ==========
    String PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME = "purchase-order-item-off";
    String PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME = "purchase-order-item-execution";
    String PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME = "purchase-order-item-storage";
    String PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME = "purchase-order-item-payment";
    String PURCHASE_ORDER_ITEM_PURCHASE_STATE_MACHINE_NAME = "purchase-order-item-purchase";
    String PURCHASE_ORDER_ITEM_RETURN_STATE_MACHINE_NAME = "purchase-order-item-return";

    // ========== SRM 到货单主项 ==========
    String PURCHASE_IN_AUDIT_STATE_MACHINE = "purchase-in-main-audit";
    String PURCHASE_IN_PAYMENT_STATE_MACHINE = "purchase-in-main-payment";
    String PURCHASE_IN_STORAGE_STATE_MACHINE = "purchase-in-main-storage";

    // ========== SRM 到货单明细 ==========
    String PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE = "purchase-in-item-payment";
    String PURCHASE_IN_ITEM_STORAGE_STATE_MACHINE = "purchase-in-item-storage";
    String PURCHASE_OUT_ITEM_STORAGE_STATE_MACHINE = "purchase-out-item-storage";

    // ========== SRM 退货单 ==========
    String PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME = "purchase-return-main-audit";
    String PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME = "purchase-return-main-refund";
    String PURCHASE_RETURN_OUT_STORAGE_STATE_MACHINE_NAME = "purchase-return-main-out-storage";

    // ========== SRM 退货单明细 ==========
    String PURCHASE_RETURN_ITEM_REFUND_STATE_MACHINE_NAME = "purchase-return-item-refund";
    String PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME = "purchase-return-item-out-storage";
}
