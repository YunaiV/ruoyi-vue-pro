package cn.iocoder.yudao.module.mes.enums.md.autocode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MES 编码规则代码枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesMdAutoCodeRuleCodeEnum {

    MD_ITEM_CODE("MD_ITEM_CODE", "物料编码"),
    MD_VENDOR_CODE("MD_VENDOR_CODE", "供应商编码"),
    MD_CLIENT_CODE("MD_CLIENT_CODE", "客户编码"),
    MD_WORKSTATION_CODE("MD_WORKSTATION_CODE", "工作站编码"),
    TM_TOOL_TYPE_CODE("TM_TOOL_TYPE_CODE", "工具类型编码"),
    WM_ARRIVAL_NOTICE_CODE("WM_ARRIVAL_NOTICE_CODE", "到货通知单编码"),
    WM_ITEM_RECEIPT_CODE("WM_ITEM_RECEIPT_CODE", "采购入库单编码"),
    WM_RETURN_VENDOR_CODE("WM_RETURN_VENDOR_CODE", "采购退货单编码"),
    WM_RETURN_ISSUE_CODE("WM_RETURN_ISSUE_CODE", "生产退料单编码"),
    WM_SN_CODE("WM_SN_CODE", "SN 码"),
    WM_PACKAGE_CODE("WM_PACKAGE_CODE", "装箱单编码"),
    WM_BATCH_CODE("WM_BATCH_CODE", "批次编码"),
    PRO_TASK_CODE("PRO_TASK_CODE", "生产任务编码"),
    QC_IQC_CODE("QC_IQC_CODE", "来料检验单编码"),
    QC_IPQC_CODE("QC_IPQC_CODE", "过程检验单编码"),
    QC_OQC_CODE("QC_OQC_CODE", "出货检验单编码"),
    QC_RQC_CODE("QC_RQC_CODE", "退货检验单编码"),
    WM_WAREHOUSE_CODE("WM_WAREHOUSE_CODE", "仓库编码"),
    WM_LOCATION_CODE("WM_LOCATION_CODE", "库区编码"),
    WM_AREA_CODE("WM_AREA_CODE", "库位编码"),
    WM_PRODUCT_SALES_CODE("WM_PRODUCT_SALES_CODE", "销售出库单编码"),
    WM_MISC_RECEIPT_CODE("WM_MISC_RECEIPT_CODE", "杂项入库单编码"),
    WM_STOCK_TAKING_PLAN_CODE("WM_STOCK_TAKING_PLAN_CODE", "盘点方案编码"),
    WM_STOCK_TAKING_CODE("WM_STOCK_TAKING_CODE", "盘点任务编码"),
    TRANSFER_CODE("TRANSFER_CODE", "转移调拨单编码"),
    WM_OUTSOURCE_ISSUE_CODE("WM_OUTSOURCE_ISSUE_CODE", "外协发料单编码"),
    DV_MACHINERY_CODE("DV_MACHINERY_CODE", "设备编码"),
    DV_MACHINERY_TYPE_CODE("DV_MACHINERY_TYPE_CODE", "设备类型编码"),
    DV_SUBJECT_CODE("DV_SUBJECT_CODE", "点检保养项目编码"),
    DV_REPAIR_CODE("DV_REPAIR_CODE", "维修单编码"),
    WM_SALES_NOTICE_CODE("WM_SALES_NOTICE_CODE", "发货通知单编码"),
    WM_RETURN_SALES_CODE("WM_RETURN_SALES_CODE", "销售退货单编码"),
    WM_MISC_ISSUE_CODE("WM_MISC_ISSUE_CODE", "杂项出库单编码"),
    PRO_ROUTE_CODE("PRO_ROUTE_CODE", "工艺路线编码"),
    PRO_FEEDBACK_CODE("PRO_FEEDBACK_CODE", "生产报工单编码"),
    PRO_WORK_ORDER_CODE("PRO_WORK_ORDER_CODE", "生产工单编码"),
    QC_DEFECT_CODE("QC_DEFECT_CODE", "缺陷类型编码"),
    QC_INDICATOR_RESULT_CODE("QC_INDICATOR_RESULT_CODE", "样品检验结果编码"),
    WM_OUTSOURCE_RECEIPT_CODE("WM_OUTSOURCE_RECEIPT_CODE", "外协入库单编码");

    /**
     * 规则代码
     */
    private final String code;
    /**
     * 规则名称
     */
    private final String name;

}
