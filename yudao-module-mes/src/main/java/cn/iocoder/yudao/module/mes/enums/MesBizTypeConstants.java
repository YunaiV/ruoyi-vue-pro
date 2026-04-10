package cn.iocoder.yudao.module.mes.enums;

/**
 * MES 业务类型常量
 *
 * 集中管理业务类型枚举的编号，按业务域分段。
 * 各枚举类引用此处常量，避免硬编码数字。（也避免冲突！！！）
 *
 * @author 芋道源码
 */
public final class MesBizTypeConstants {

    private MesBizTypeConstants() {}

    // ========== QC 质检模块 [1, 100) ==========

    public static final int QC_IQC = 1; // 来料检验：MesQcIqcDO
    public static final int QC_IPQC = 2; // 过程检验：MesQcIpqcDO
    public static final int QC_OQC = 3; // 出货检验：MesQcOqcDO
    public static final int QC_RQC = 4; // 退货检验：MesQcRqcDO

    // ========== WM 仓库模块 [100, 200) ==========

    public static final int WM_ARRIVAL_NOTICE = 100; // 到货通知单：MesWmArrivalNoticeDO
    public static final int WM_WAREHOUSE = 102; // 仓库：MesWmWarehouseDO
    public static final int WM_LOCATION = 103; // 库位：MesWmLocationDO
    public static final int WM_AREA = 104; // 库位：MesWmStorageAreaDO
    public static final int WM_PACKAGE = 105; // 装箱单：MesWmPackageDO
    public static final int WM_STOCK = 106; // 库存：MesWmMaterialStockDO
    public static final int WM_BATCH = 107; // 批次：MesWmBatchDO
    public static final int WM_TRANSACTION = 108; // 库存事务流水：MesWmTransactionDO
    public static final int WM_ITEM_RECEIPT_IN = 110; // 采购入库：MesWmItemReceiptDO
    public static final int WM_TRANSFER_OUT = 111; // 调拨出库：MesWmTransferDO
    public static final int WM_TRANSFER_IN = 112; // 调拨入库：MesWmTransferDO
    public static final int WM_MISC_ISSUE = 113; // 其他出库：MesWmMiscIssueDO
    public static final int WM_MISC_RECPT = 114; // 其他入库：MesWmMiscReceiptDO
    public static final int WM_ISSUE = 115; // 生产领料：MesWmProductIssueDO
    public static final int WM_RETURN_ISSUE = 116; // 生产退料：MesWmReturnIssueDO
    public static final int WM_PRODUCT_RECPT = 117; // 生产入库：MesWmProductReceiptDO
    public static final int WM_PRODUCT_SALES = 118; // 销售出库：MesWmProductSalesDO
    public static final int WM_RETURN_SALES = 119; // 销售退货入库：MesWmReturnSalesDO
    public static final int WM_OUTSOURCE_ISSUE = 120; // 外协发料：MesWmOutsourceIssueDO
    public static final int WM_OUTSOURCE_RECPT = 121; // 外协入库：MesWmOutsourceReceiptDO
    public static final int WM_ITEM_CONSUME = 122; // 生产消耗：MesWmItemConsumeDO
    public static final int WM_PRODUCT_PRODUCE = 123; // 产品产出：MesWmProductProduceDO
    public static final int WM_RETURN_VENDOR = 124; // 供应商退货出库：MesWmReturnVendorDO

    // ========== CAL 排班模块 [200, 300) ==========

    // TODO @芋艿【暂时忽略】：MesProWorkOrderTypeEnum
    // TODO @芋艿【暂时忽略】：MesProWorkOrderSourceTypeEnum

    // ========== PRO 生产模块 [300, 400) ==========

    public static final int PRO_CARD = 300; // 流转卡：MesProCardDO
    public static final int PRO_WORKORDER = 301; // 工单：MesProWorkOrderDO
    public static final int PRO_TRANS_ORDER = 302; // 流转单：MesProTransOrderDO
    public static final int PRO_TASK = 303; // 生产任务：MesProTaskDO
    public static final int PRO_FEEDBACK = 304; // 生产报工：MesProFeedbackDO

    // ========== DV 设备模块 [400, 500) ==========

    public static final int DV_MACHINERY = 400; // 设备：MesDvMachineryDO

    // ========== TM 工装夹具模块 [500, 600) ==========

    public static final int TM_TOOL = 500; // 工装：MesToolDO

    // ========== MD 主数据模块 [600, 700) ==========

    public static final int MD_ITEM = 600; // 物料：MesMdItemDO
    public static final int MD_VENDOR = 601; // 供应商：MesMdVendorDO
    public static final int MD_WORKSTATION = 602; // 工作站：MesMdWorkstationDO
    public static final int MD_WORKSHOP = 603; // 车间：MesMdWorkshopDO
    public static final int MD_USER = 604; // 人员：系统用户
    public static final int MD_CLIENT = 605; // 客户：MesMdClientDO

    // ========== 特殊类型（非业务实体） [900, 1000) ==========

    public static final int QUALITY_STATUS = 900; // 质量状态：非实体，用于盘点等场景的状态筛选

}
