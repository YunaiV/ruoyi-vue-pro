package cn.iocoder.yudao.module.srm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 单据来源描述枚举
 */
@RequiredArgsConstructor
@Getter
public enum SrmPurchaseOrderSourceEnum {

    //"WEB录入"
    WEB_ENTRY("WEB录入"),
    //"ERP采购"
    ERP_PURCHASE("ERP采购"),
    //"ERP销售"
    ERP_SALES("ERP销售"),
    //"ERP库存"
    ERP_STOCK("ERP库存"),
    //"ERP销售退货"
    ERP_SALES_RETURN("ERP销售退货"),
    //"ERP采购退货"
    ERP_PURCHASE_RETURN("ERP采购退货"),
    //"ERP调拨"
    ERP_STOCK_MOVE("ERP调拨");

    private final String desc;

}
