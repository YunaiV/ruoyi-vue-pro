package cn.iocoder.yudao.module.mes.enums;

/**
 * MES 字典类型的枚举类
 *
 * @author 芋道源码
 */
public interface DictTypeConstants {

    // ========== 基础数据 (MD) ==========
    String MES_MD_ITEM_OR_PRODUCT = "mes_md_item_or_product"; // MES 物料产品标识
    String MES_MD_AUTO_CODE_PART_TYPE = "mes_md_auto_code_part_type"; // MES 分段类型
    String MES_MD_AUTO_CODE_CYCLE_METHOD = "mes_md_auto_code_cycle_method"; // MES 循环方式
    String MES_MD_AUTO_CODE_PADDED_METHOD = "mes_md_auto_code_padded_method"; // MES 补齐方式
    String MES_CLIENT_TYPE = "mes_client_type"; // MES 客户类型
    String MES_VENDOR_LEVEL = "mes_vendor_level"; // MES 供应商级别

    // ========== 生产计划与执行 (PRO) ==========
    String MES_PRO_WORK_ORDER_STATUS = "mes_pro_work_order_status"; // MES 生产工单状态
    String MES_PRO_WORK_ORDER_SOURCE_TYPE = "mes_pro_work_order_source_type"; // MES 工单来源类型
    String MES_PRO_WORK_ORDER_TYPE = "mes_pro_work_order_type"; // MES 工单类型
    String MES_PRO_LINK_TYPE = "mes_pro_link_type"; // MES 工序关系类型
    String MES_TIME_UNIT_TYPE = "mes_time_unit_type"; // MES 时间单位
    String MES_PRO_ANDON_STATUS = "mes_pro_andon_status"; // MES 安灯处置状态
    String MES_PRO_ANDON_LEVEL = "mes_pro_andon_level"; // MES 安灯级别
    String MES_PRO_WORK_RECORD_TYPE = "mes_pro_work_record_type"; // MES 上下工状态类型
    String MES_PRO_FEEDBACK_STATUS = "mes_pro_feedback_status"; // MES 生产报工状态
    String MES_PRO_FEEDBACK_TYPE = "mes_pro_feedback_type"; // MES 生产报工类型
    String MES_PRO_FEEDBACK_CHANNEL = "mes_pro_feedback_channel"; // MES 生产报工途径
    String MES_PRO_TASK_STATUS = "mes_pro_task_status"; // MES 任务状态

    // ========== 仓储条码与管理 (WM) ==========
    String MES_BARCODE_FORMAT = "mes_barcode_format"; // MES 条码格式
    String MES_BARCODE_BIZ_TYPE = "mes_barcode_biz_type"; // MES 条码业务类型
    String MES_WM_TRANSACTION_TYPE = "mes_wm_transaction_type"; // MES 库存事务类型
    String MES_WM_QUALITY_STATUS = "mes_wm_quality_status"; // MES 质量状态（待检/合格/不合格）

    // ========== 仓储入库 (WM Receipt) ==========
    String MES_WM_ARRIVAL_NOTICE_STATUS = "mes_wm_arrival_notice_status"; // MES 到货通知单状态
    String MES_WM_ITEM_RECEIPT_STATUS = "mes_wm_item_receipt_status"; // MES 采购入库单状态
    String MES_WM_PRODUCT_PRODUCE_STATUS = "mes_wm_product_produce_status"; // MES 生产入库单状态
    String MES_WM_MISC_RECEIPT_TYPE = "mes_wm_misc_receipt_type"; // MES 杂项入库类型
    String MES_WM_MISC_RECEIPT_STATUS = "mes_wm_misc_receipt_status"; // MES 杂项入库状态
    String MES_WM_OUTSOURCE_RECEIPT_STATUS = "mes_wm_outsource_receipt_status"; // MES 委外入库状态
    String MES_WM_PRODUCT_RECEIPT_STATUS = "mes_wm_product_receipt_status"; // MES 产品入库状态
    String MES_WM_RETURN_ISSUE_TYPE = "mes_wm_return_issue_type"; // MES 生产退料类型
    String MES_WM_RETURN_ISSUE_STATUS = "mes_wm_return_issue_status"; // MES 生产退料单状态
    String MES_WM_RETURN_SALES_STATUS = "mes_wm_return_sales_status"; // MES 销售退货状态

    // ========== 仓储出库 (WM Issue) ==========
    String MES_WM_MISC_ISSUE_TYPE = "mes_wm_misc_issue_type"; // MES 杂项出库类型
    String MES_WM_MISC_ISSUE_STATUS = "mes_wm_misc_issue_status"; // MES 杂项出库单状态
    String MES_WM_SALES_NOTICE_STATUS = "mes_wm_sales_notice_status"; // MES 发货通知单状态
    String MES_WM_PRODUCT_SALES_STATUS = "mes_wm_product_sales_status"; // MES 销售出库单状态
    String MES_WM_OUTSOURCE_ISSUE_STATUS = "mes_wm_outsource_issue_status"; // MES 委外出库状态
    String MES_WM_PRODUCT_ISSUE_STATUS = "mes_wm_product_issue_status"; // MES 生产领料状态
    String MES_WM_RETURN_VENDOR_STATUS = "mes_wm_return_vendor_status"; // MES 供应商退货单状态

    // ========== 仓储移库与盘点 (WM Transfer) ==========
    String MES_WM_TRANSFER_STATUS = "mes_wm_transfer_status"; // MES 转移单状态
    String MES_WM_TRANSFER_TYPE = "mes_wm_transfer_type"; // MES 转移单类型
    String MES_WM_STOCK_TAKING_PLAN_PARAM_TYPE = "mes_wm_stock_taking_plan_param_type"; // MES 盘点参数值类型
    String MES_WM_STOCK_TAKING_TYPE = "mes_wm_stock_taking_type"; // MES 盘点类型
    String MES_WM_STOCK_TAKING_TASK_STATUS = "mes_wm_stock_taking_task_status"; // MES 盘点任务状态
    String MES_WM_STOCK_TAKING_TASK_LINE_STATUS = "mes_wm_stock_taking_task_line_status"; // MES 盘点任务明细状态
    
    // ========== 其它仓储 (WM Misc) ==========
    String MES_WM_PACKAGE_STATUS = "mes_wm_package_status"; // MES 打包状态
    String MES_WM_ITEM_CONSUME_STATUS = "mes_wm_item_consume_status"; // MES 物料消耗状态

    // ========== 质量管理 (QC) ==========
    String MES_INDICATOR_TYPE = "mes_indicator_type"; // MES 检测项类型
    String MES_QC_RESULT_TYPE = "mes_qc_result_type"; // MES 质检结果值类型
    String MES_DEFECT_LEVEL = "mes_defect_level"; // MES 缺陷等级
    String MES_QC_TYPE = "mes_qc_type"; // MES 检测种类（IQC/IPQC/OQC/RQC）
    String MES_IPQC_TYPE = "mes_ipqc_type"; // MES IPQC 检验类型
    String MES_ORDER_STATUS = "mes_order_status"; // MES 单据状态（IQC/IPQC/OQC/RQC 通用）
    String MES_QC_CHECK_RESULT = "mes_qc_check_result"; // MES 检测结果
    String MES_QC_SOURCE_DOC_TYPE = "mes_qc_source_doc_type"; // MES 来源单据类型

    // ========== 设备管理 (DV) ==========
    String MES_DV_MACHINERY_STATUS = "mes_dv_machinery_status"; // MES 设备状态
    String MES_DV_SUBJECT_TYPE = "mes_dv_subject_type"; // MES 点检保养项目类型
    String MES_DV_CYCLE_TYPE = "mes_dv_cycle_type"; // MES 点检保养周期类型
    String MES_DV_CHECK_PLAN_STATUS = "mes_dv_check_plan_status"; // MES 点检保养方案状态
    String MES_MAINTEN_RECORD_STATUS = "mes_mainten_record_status"; // MES 保养记录状态
    String MES_MAINTEN_STATUS = "mes_mainten_status"; // MES 保养结果
    String MES_DV_CHECK_RECORD_STATUS = "mes_dv_check_record_status"; // MES 点检记录状态
    String MES_DV_CHECK_RESULT = "mes_dv_check_result"; // MES 点检结果
    String MES_DV_REPAIR_STATUS = "mes_dv_repair_status"; // MES 维修工单状态
    String MES_DV_REPAIR_RESULT = "mes_dv_repair_result"; // MES 维修结果

    // ========== 排班日历 (CAL) ==========
    String MES_CAL_HOLIDAY_TYPE = "mes_cal_holiday_type"; // MES 假期类型
    String MES_CAL_SHIFT_TYPE = "mes_cal_shift_type"; // MES 轮班方式
    String MES_CAL_SHIFT_METHOD = "mes_cal_shift_method"; // MES 倒班方式
    String MES_CAL_CALENDAR_TYPE = "mes_cal_calendar_type"; // MES 班组类型
    String MES_CAL_PLAN_STATUS = "mes_cal_plan_status"; // MES 排班计划状态

    // ========== 工具管理 (TM) ==========
    String MES_TM_TOOL_STATUS = "mes_tm_tool_status"; // MES 工具状态
    String MES_TM_MAINTEN_TYPE = "mes_tm_mainten_type"; // MES 保养维护类型
}
