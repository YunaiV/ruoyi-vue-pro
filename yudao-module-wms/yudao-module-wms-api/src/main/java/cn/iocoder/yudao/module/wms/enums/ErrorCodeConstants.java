package cn.iocoder.yudao.module.wms.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * WMS 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface ErrorCodeConstants {

    // ========== WMS_EXTERNAL_STORAGE 外部存储库 2_001_000_000 ==========
    ErrorCode EXTERNAL_STORAGE_NAME_DUPLICATE = new ErrorCode(2_001_000_000, "名称已存在");
    ErrorCode EXTERNAL_STORAGE_CODE_DUPLICATE = new ErrorCode(2_001_000_001, "代码已存在");
    ErrorCode EXTERNAL_STORAGE_NOT_EXISTS = new ErrorCode(2_001_000_002, "外部存储库不存在");
    ErrorCode EXTERNAL_STORAGE_BE_REFERRED = new ErrorCode(2_001_000_003, "外部存储库被引用");

    // ========== WMS_WAREHOUSE 仓库 2_001_001_000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(2_001_001_000, "仓库不存在");
    ErrorCode WAREHOUSE_NAME_DUPLICATE = new ErrorCode(2_001_001_001, "名称已存在");
    ErrorCode WAREHOUSE_CODE_DUPLICATE = new ErrorCode(2_001_001_002, "代码已存在");
    ErrorCode WAREHOUSE_BE_REFERRED = new ErrorCode(2_001_001_003, "仓库被引用");

    // ========== WMS_WAREHOUSE_ZONE 仓库 2_001_002_000 ==========
    ErrorCode WAREHOUSE_ZONE_NOT_EXISTS = new ErrorCode(2_001_002_000, "库区不存在");
    ErrorCode WAREHOUSE_ZONE_CODE_DUPLICATE = new ErrorCode(2_001_002_001, "代码已存在");
    ErrorCode WAREHOUSE_ZONE_BE_REFERRED = new ErrorCode(2_001_002_002, "库区被引用");

    // ========== WMS_WAREHOUSE_BIN 仓库 2_001_003_000 ==========
    ErrorCode WAREHOUSE_BIN_NOT_EXISTS = new ErrorCode(2_001_003_000, "库位不存在");
    ErrorCode WAREHOUSE_BIN_CODE_DUPLICATE = new ErrorCode(2_001_003_001, "代码已存在");
    ErrorCode WAREHOUSE_BIN_BE_REFERRED = new ErrorCode(2_001_003_002, "库位被引用");

    // ========== WMS_INBOUND 入库单 2_001_004_000 ==========
    ErrorCode INBOUND_NOT_EXISTS = new ErrorCode(2_001_004_001, "入库单不存在");
    ErrorCode INBOUND_NO_DUPLICATE = new ErrorCode(2_001_004_002, "单据号已存在");
    ErrorCode INBOUND_BE_REFERRED = new ErrorCode(2_001_004_003, "入库单被引用");
    ErrorCode INBOUND_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_004_004, "入库单单据号越界");
    ErrorCode INBOUND_ITEM_PRODUCT_ID_REPEATED = new ErrorCode(2_001_004_005, "入库单标准产品ID重复");
    ErrorCode INBOUND_CAN_NOT_EDIT = new ErrorCode(2_001_004_007, "入库单不允许编辑");
    ErrorCode INBOUND_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_004_008, "入库单不符合审批条件");
    ErrorCode INBOUND_CAN_NOT_DELETE = new ErrorCode(2_001_004_009, "入库单不允许删除");
    ErrorCode INBOUND_NOT_COMPLETE = new ErrorCode(2_001_004_010, "入库单未完全入库");
    ErrorCode INBOUND_ITEM_PLAN_QTY_ERROR = new ErrorCode(2_001_004_020, "计划入库量错误");
    ErrorCode INBOUND_ITEM_ACTUAL_QTY_ERROR = new ErrorCode(2_001_004_021, "实际入库量错误");
    ErrorCode INBOUND_AUDIT_ERROR = new ErrorCode(2_001_004_022, "入库单审批状态错误");
    ErrorCode INBOUND_ABANDON_NOT_ALLOWED = new ErrorCode(2_001_004_023, "入库单不允许废弃");
    ErrorCode INBOUND_FORCE_FINISH_NOT_ALLOWED = new ErrorCode(2_001_004_024, "入库单不允许强制完成");
    ErrorCode INBOUND_AUDIT_FAIL = new ErrorCode(2_001_004_025, "审核错误，当前入库单状态为{}，在{}状态时才允许{}");
    ErrorCode INBOUND_EXISTS = new ErrorCode(2_001_004_026, "入库单已存在");

    // ========== WMS_INBOUND_ITEM 入库单详情表 2_001_005_000 ==========
    ErrorCode INBOUND_ITEM_NOT_EXISTS = new ErrorCode(2_001_005_001, "入库单详情不存在");
    ErrorCode INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_005_002, "入库单ID,标准产品ID已存在");
    ErrorCode INBOUND_ITEM_INBOUND_ID_PRODUCT_SKU_DUPLICATE = new ErrorCode(2_001_005_003, "入库单ID,标准产品SKU已存在");
    ErrorCode INBOUND_ITEM_BE_REFERRED = new ErrorCode(2_001_005_004, "入库单详情被引用");
    ErrorCode INBOUND_ITEM_CAN_NOT_EDIT = new ErrorCode(2_001_005_005, "入库单详情不允许编辑");
    ErrorCode INBOUND_ITEM_CAN_NOT_DELETE = new ErrorCode(2_001_005_006, "入库单详情不允许删除");
    ErrorCode INBOUND_ITEM_OUTBOUND_AVAILABLE_QTY_NOT_ENOUGH = new ErrorCode(2_001_005_007, "入库入库批次库存不足");
    ErrorCode INBOUND_ITEM_INBOUND_ID_DUPLICATE = new ErrorCode(2_001_005_008, "入库单ID不允许重复");
    ErrorCode INBOUND_ITEM_EXISTS = new ErrorCode(2_001_005_009, "入库单详情已存在");

    // ========== WMS_INBOUND_ITEM_FLOW 入库单库存详情扣减表 2_001_006_000 ==========
    ErrorCode INBOUND_ITEM_FLOW_NOT_EXISTS = new ErrorCode(2_001_006_001, "入库单库存详情扣减不存在");
    ErrorCode INBOUND_ITEM_FLOW_BE_REFERRED = new ErrorCode(2_001_006_002, "入库单库存详情扣减被引用");
    ErrorCode INBOUND_ITEM_FLOW_CAN_NOT_EDIT = new ErrorCode(2_001_006_003, "入库单库存详情扣减不允许编辑");
    ErrorCode INBOUND_ITEM_FLOW_CAN_NOT_DELETE = new ErrorCode(2_001_006_004, "入库单库存详情扣减不允许删除");
    ErrorCode INBOUND_ITEM_FLOW_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_006_005, "入库单库存详情扣减不符合审批条件");
    ErrorCode INBOUND_STATUS_PARSE_ERROR = new ErrorCode(2_001_006_006, "无法完成入库单状态转换");
    ErrorCode INBOUND_ITEM_FLOW_EXISTS = new ErrorCode(2_001_006_007, "入库单库存详情扣减已存在");

    // ========== WMS_APPROVAL_HISTORY 审批历史表 2_001_007_000 ==========
    ErrorCode APPROVAL_HISTORY_NOT_EXISTS = new ErrorCode(2_001_007_001, "审批历史不存在");
    ErrorCode APPROVAL_HISTORY_BE_REFERRED = new ErrorCode(2_001_007_002, "审批历史被引用");
    ErrorCode APPROVAL_HISTORY_CAN_NOT_EDIT = new ErrorCode(2_001_007_003, "审批历史不允许编辑");
    ErrorCode APPROVAL_HISTORY_CAN_NOT_DELETE = new ErrorCode(2_001_007_004, "审批历史不允许删除");
    ErrorCode APPROVAL_HISTORY_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_007_005, "审批历史不符合审批条件");

    // ========== WMS_STOCK_WAREHOUSE 仓库库存表 2_001_008_000 ==========
    ErrorCode STOCK_WAREHOUSE_NOT_EXISTS = new ErrorCode(2_001_008_001, "仓库库存不存在");
    ErrorCode STOCK_WAREHOUSE_WAREHOUSE_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_008_002, "仓库ID,产品ID已存在");
    ErrorCode STOCK_WAREHOUSE_BE_REFERRED = new ErrorCode(2_001_008_003, "仓库库存被引用");
    ErrorCode STOCK_WAREHOUSE_CAN_NOT_EDIT = new ErrorCode(2_001_008_004, "仓库库存不允许编辑");
    ErrorCode STOCK_WAREHOUSE_CAN_NOT_DELETE = new ErrorCode(2_001_008_005, "仓库库存不允许删除");
    ErrorCode STOCK_WAREHOUSE_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_008_006, "仓库库存不符合审批条件");

    // ========== WMS_STOCK_BIN 仓位库存表 2_001_009_000 ==========
    ErrorCode STOCK_BIN_NOT_EXISTS = new ErrorCode(2_001_009_001, "仓位库存不存在");
    ErrorCode STOCK_BIN_BIN_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_009_002, "库位ID,产品ID已存在");
    ErrorCode STOCK_BIN_BE_REFERRED = new ErrorCode(2_001_009_003, "仓位库存被引用");
    ErrorCode STOCK_BIN_CAN_NOT_EDIT = new ErrorCode(2_001_009_004, "仓位库存不允许编辑");
    ErrorCode STOCK_BIN_CAN_NOT_DELETE = new ErrorCode(2_001_009_005, "仓位库存不允许删除");
    ErrorCode STOCK_BIN_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_009_006, "仓位库存不符合审批条件");
    ErrorCode STOCK_BIN_NOT_ENOUGH = new ErrorCode(2_001_009_007, "仓位库存不足");

    // ========== WMS_STOCK_OWNERSHIP 所有者库存表 2_001_010_000 ==========
    ErrorCode STOCK_OWNERSHIP_NOT_EXISTS = new ErrorCode(2_001_010_001, "所有者库存不存在");
    ErrorCode STOCK_OWNERSHIP_WAREHOUSE_ID_DEPT_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_010_002, "仓库ID,库存归属部门ID,产品ID已存在");
    ErrorCode STOCK_OWNERSHIP_BE_REFERRED = new ErrorCode(2_001_010_003, "所有者库存被引用");
    ErrorCode STOCK_OWNERSHIP_CAN_NOT_EDIT = new ErrorCode(2_001_010_004, "所有者库存不允许编辑");
    ErrorCode STOCK_OWNERSHIP_CAN_NOT_DELETE = new ErrorCode(2_001_010_005, "所有者库存不允许删除");
    ErrorCode STOCK_OWNERSHIP_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_010_006, "所有者库存不符合审批条件");
    ErrorCode STOCK_OWNERSHIP_WAREHOUSE_ID_COMPANY_ID_DEPT_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_010_007, "仓库ID,库存财务主体公司ID,库存归属部门ID,产品ID已存在");

    // ========== WMS_STOCK_FLOW 库存流水表 2_001_011_000 ==========
    ErrorCode STOCK_FLOW_NOT_EXISTS = new ErrorCode(2_001_011_001, "库存流水不存在");
    ErrorCode STOCK_FLOW_BE_REFERRED = new ErrorCode(2_001_011_002, "库存流水被引用");
    ErrorCode STOCK_FLOW_CAN_NOT_EDIT = new ErrorCode(2_001_011_003, "库存流水不允许编辑");
    ErrorCode STOCK_FLOW_CAN_NOT_DELETE = new ErrorCode(2_001_011_004, "库存流水不允许删除");
    ErrorCode STOCK_FLOW_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_011_005, "库存流水不符合审批条件");

    // ========== WMS_PICKUP 拣货单 2_001_012_000 ==========
    ErrorCode PICKUP_NOT_EXISTS = new ErrorCode(2_001_012_001, "拣货单不存在");
    ErrorCode PICKUP_NO_DUPLICATE = new ErrorCode(2_001_012_002, "单据号已存在");
    ErrorCode PICKUP_BE_REFERRED = new ErrorCode(2_001_012_003, "拣货单被引用");
    ErrorCode PICKUP_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_012_004, "拣货单单据号越界");
    ErrorCode PICKUP_CAN_NOT_EDIT = new ErrorCode(2_001_012_005, "拣货单不允许编辑");
    ErrorCode PICKUP_CAN_NOT_DELETE = new ErrorCode(2_001_012_006, "拣货单不允许删除");
    ErrorCode PICKUP_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_012_007, "拣货单不符合审批条件");
    ErrorCode PICKUP_EXISTS = new ErrorCode(2_001_012_008, "拣货单已存在");

    // ========== WMS_PICKUP_ITEM 拣货单详情表 2_001_013_000 ==========
    ErrorCode PICKUP_ITEM_NOT_EXISTS = new ErrorCode(2_001_013_001, "拣货单详情不存在");
    ErrorCode PICKUP_ITEM_BE_REFERRED = new ErrorCode(2_001_013_002, "拣货单详情被引用");
    ErrorCode PICKUP_ITEM_CAN_NOT_EDIT = new ErrorCode(2_001_013_003, "拣货单详情不允许编辑");
    ErrorCode PICKUP_ITEM_CAN_NOT_DELETE = new ErrorCode(2_001_013_004, "拣货单详情不允许删除");
    ErrorCode PICKUP_ITEM_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_013_005, "拣货单详情不符合审批条件");
    ErrorCode PICKUP_ITEM_INBOUND_ITEM_ID_REPEATED = new ErrorCode(2_001_013_006, "入库单详情ID重复");
    ErrorCode PICKUP_ITEM_INBOUND_ITEM_ID_WAREHOUSE_ID_NOT_SAME = new ErrorCode(2_001_013_007, "拣货仓库错误");
    ErrorCode PICKUP_ITEM_INBOUND_ITEM_ID_NOT_SAME = new ErrorCode(2_001_013_008, "拣货明细错误");
    ErrorCode INBOUND_ITEM_PICKUP_LEFT_QUANTITY_NOT_ENOUGH = new ErrorCode(2_001_013_008, "入库单明细的待上架库存不足");
    ErrorCode PICKUP_ITEM_QTY_ERROR = new ErrorCode(2_001_013_009, "拣货数量错误");

    // ========== WMS_OUTBOUND_ITEM 出库单详情表 2_001_014_000 ==========
    ErrorCode OUTBOUND_ITEM_NOT_EXISTS = new ErrorCode(2_001_014_001, "出库单详情不存在");
    ErrorCode OUTBOUND_ITEM_OUTBOUND_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_014_002, "入库单ID,标准产品ID已存在");
    ErrorCode OUTBOUND_ITEM_BE_REFERRED = new ErrorCode(2_001_014_003, "出库单详情被引用");
    ErrorCode OUTBOUND_ITEM_CAN_NOT_EDIT = new ErrorCode(2_001_014_004, "出库单详情不允许编辑");
    ErrorCode OUTBOUND_ITEM_CAN_NOT_DELETE = new ErrorCode(2_001_014_005, "出库单详情不允许删除");
    ErrorCode OUTBOUND_ITEM_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_014_006, "出库单详情不符合审批条件");
    ErrorCode OUTBOUND_WAREHOUSE_ERROR = new ErrorCode(2_001_014_007, "出库仓库错误");
    ErrorCode OUTBOUND_AUDIT_ERROR = new ErrorCode(2_001_014_008, "出库单审批状态错误");
    ErrorCode OUTBOUND_AUDIT_FAIL = new ErrorCode(2_001_014_009, "审核错误，当前出库单状态为%s，在%s状态时才允许%s");
    ErrorCode OUTBOUND_ITEM_EXISTS = new ErrorCode(2_001_014_010, "出库单详情已存在");

    // ========== WMS_OUTBOUND 出库单 2_001_015_000 ==========
    ErrorCode OUTBOUND_NOT_EXISTS = new ErrorCode(2_001_015_001, "出库单不存在");
    ErrorCode OUTBOUND_NO_DUPLICATE = new ErrorCode(2_001_015_002, "单据号已存在");
    ErrorCode OUTBOUND_BE_REFERRED = new ErrorCode(2_001_015_003, "出库单被引用");
    ErrorCode OUTBOUND_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_015_004, "出库单单据号越界");
    ErrorCode OUTBOUND_CAN_NOT_EDIT = new ErrorCode(2_001_015_005, "出库单不允许编辑");
    ErrorCode OUTBOUND_CAN_NOT_DELETE = new ErrorCode(2_001_015_006, "出库单不允许删除");
    ErrorCode OUTBOUND_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_015_007, "出库单不符合审批条件");
    ErrorCode OUTBOUND_ITEM_PRODUCT_ID_REPEATED = new ErrorCode(2_001_015_007, "出库单产品重复");
    ErrorCode OUTBOUND_ITEM_OUTBOUND_ID_DUPLICATE = new ErrorCode(2_001_015_008, "详情不是一个出库单");
    ErrorCode OUTBOUND_ITEM_PLAN_QTY_ERROR = new ErrorCode(2_001_015_009, "计划出库量错误");
    ErrorCode OUTBOUND_ITEM_ACTUAL_QTY_ERROR = new ErrorCode(2_001_015_010, "实际出库量错误");
    ErrorCode OUTBOUND_STATUS_PARSE_ERROR = new ErrorCode(2_001_015_011, "无法完成出库单状态转换");
    ErrorCode OUTBOUND_EXISTS = new ErrorCode(2_001_015_012, "出库单已存在");

    // ========== WMS_STOCK_BIN_MOVE 库位移动表 2_001_016_000 ==========
    ErrorCode STOCK_BIN_MOVE_NOT_EXISTS = new ErrorCode(2_001_016_001, "库位移动不存在");
    ErrorCode STOCK_BIN_MOVE_NO_DUPLICATE = new ErrorCode(2_001_016_002, "单据号已存在");
    ErrorCode STOCK_BIN_MOVE_BE_REFERRED = new ErrorCode(2_001_016_003, "库位移动被引用");
    ErrorCode STOCK_BIN_MOVE_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_016_004, "库位移动单据号越界");
    ErrorCode STOCK_BIN_MOVE_CAN_NOT_EDIT = new ErrorCode(2_001_016_005, "库位移动不允许编辑");
    ErrorCode STOCK_BIN_MOVE_CAN_NOT_DELETE = new ErrorCode(2_001_016_006, "库位移动不允许删除");
    ErrorCode STOCK_BIN_MOVE_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_016_007, "库位移动不符合审批条件");
    ErrorCode STOCK_BIN_MOVE_QUANTITY_ERROR = new ErrorCode(2_001_016_008, "库位库存移动数量错误");

    // ========== WMS_STOCK_BIN_MOVE_ITEM 库位移动详情表 2_001_017_000 ==========
    ErrorCode STOCK_BIN_MOVE_ITEM_NOT_EXISTS = new ErrorCode(2_001_017_001, "库位移动详情不存在");
    ErrorCode STOCK_BIN_MOVE_ITEM_BIN_MOVE_ID_PRODUCT_ID_FROM_BIN_ID_TO_BIN_ID_DUPLICATE = new ErrorCode(2_001_017_002, "库位移动表ID,产品ID,调出库位ID,调入库位ID已存在");
    ErrorCode STOCK_BIN_MOVE_ITEM_BE_REFERRED = new ErrorCode(2_001_017_003, "库位移动详情被引用");
    ErrorCode STOCK_BIN_MOVE_ITEM_CAN_NOT_EDIT = new ErrorCode(2_001_017_004, "库位移动详情不允许编辑");
    ErrorCode STOCK_BIN_MOVE_ITEM_CAN_NOT_DELETE = new ErrorCode(2_001_017_005, "库位移动详情不允许删除");
    ErrorCode STOCK_BIN_MOVE_ITEM_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_017_006, "库位移动详情不符合审批条件");
    ErrorCode STOCK_BIN_MOVE_ITEM_EXISTS = new ErrorCode(2_001_017_006, "库位移动详情已存在");
    ErrorCode STOCK_BIN_MOVE_ITEM_REPEATED = new ErrorCode(2_001_017_007, "库位移动详情重复");

    // ========== WMS_STOCK_OWNERSHIP_MOVE 所有者库存移动表 2_001_018_000 ==========
    ErrorCode STOCK_OWNERSHIP_MOVE_NOT_EXISTS = new ErrorCode(2_001_018_001, "所有者库存移动不存在");
    ErrorCode STOCK_OWNERSHIP_MOVE_NO_DUPLICATE = new ErrorCode(2_001_018_002, "单据号已存在");
    ErrorCode STOCK_OWNERSHIP_MOVE_BE_REFERRED = new ErrorCode(2_001_018_003, "所有者库存移动被引用");
    ErrorCode STOCK_OWNERSHIP_MOVE_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_018_004, "所有者库存移动单据号越界");
    ErrorCode STOCK_OWNERSHIP_MOVE_CAN_NOT_EDIT = new ErrorCode(2_001_018_005, "所有者库存移动不允许编辑");
    ErrorCode STOCK_OWNERSHIP_MOVE_CAN_NOT_DELETE = new ErrorCode(2_001_018_006, "所有者库存移动不允许删除");
    ErrorCode STOCK_OWNERSHIP_MOVE_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_018_007, "所有者库存移动不符合审批条件");

    // ========== WMS_STOCK_OWNERSHIP_MOVE_ITEM 所有者库存移动详情表 2_001_019_000 ==========
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_NOT_EXISTS = new ErrorCode(2_001_019_001, "所有者库存移动详情不存在");
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_REPEATED = new ErrorCode(2_001_019_002, "所有者移动详情重复");
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_BE_REFERRED = new ErrorCode(2_001_019_003, "所有者库存移动详情被引用");
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_CAN_NOT_EDIT = new ErrorCode(2_001_019_004, "所有者库存移动详情不允许编辑");
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_CAN_NOT_DELETE = new ErrorCode(2_001_019_005, "所有者库存移动详情不允许删除");
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_019_006, "所有者库存移动详情不符合审批条件");
    ErrorCode STOCK_OWNERSHIP_MOVE_ITEM_EXISTS = new ErrorCode(2_001_019_007, "所有者库存移动详情已存在");
    ErrorCode STOCK_OWNERSHIP_MOVE_QUANTITY_ERROR = new ErrorCode(2_001_019_008, "所有者库存移动数量错误");

    // ========== WMS_INVENTORY 盘点表 2_001_020_000 ==========
    ErrorCode INVENTORY_NOT_EXISTS = new ErrorCode(2_001_020_001, "盘点单不存在");
    ErrorCode INVENTORY_EXISTS = new ErrorCode(2_001_020_002, "盘点单已存在");
    ErrorCode INVENTORY_NO_DUPLICATE = new ErrorCode(2_001_020_003, "单据号已存在");
    ErrorCode INVENTORY_BE_REFERRED = new ErrorCode(2_001_020_004, "盘点单被引用");
    ErrorCode INVENTORY_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_020_005, "盘点单单据号越界");
    ErrorCode INVENTORY_CAN_NOT_EDIT = new ErrorCode(2_001_020_006, "盘点单不允许编辑");
    ErrorCode INVENTORY_CAN_NOT_DELETE = new ErrorCode(2_001_020_007, "盘点单不允许删除");
    ErrorCode INVENTORY_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_020_008, "盘点单不符合审批条件");

    // ========== WMS_INVENTORY_PRODUCT 库存盘点产品表 2_001_021_000 ==========
    ErrorCode INVENTORY_PRODUCT_NOT_EXISTS = new ErrorCode(2_001_021_001, "库存盘点产品不存在");
    ErrorCode INVENTORY_PRODUCT_EXISTS = new ErrorCode(2_001_021_002, "库存盘点产品已存在");
    ErrorCode INVENTORY_PRODUCT_INVENTORY_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_021_003, "盘点结果单ID,产品ID已存在");
    ErrorCode INVENTORY_PRODUCT_BE_REFERRED = new ErrorCode(2_001_021_004, "库存盘点产品被引用");
    ErrorCode INVENTORY_PRODUCT_CAN_NOT_EDIT = new ErrorCode(2_001_021_005, "库存盘点产品不允许编辑");
    ErrorCode INVENTORY_PRODUCT_CAN_NOT_DELETE = new ErrorCode(2_001_021_006, "库存盘点产品不允许删除");
    ErrorCode INVENTORY_PRODUCT_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_021_007, "库存盘点产品不符合审批条件");
    ErrorCode INVENTORY_AUDIT_FAIL = new ErrorCode(2_001_021_008, "审核错误，当前出库单状态为%s，在%s状态时才允许%s");
    ErrorCode INVENTORY_STATUS_PARSE_ERROR = new ErrorCode(2_001_021_009, "盘点审批状态转换错误");

    // ========== WMS_INVENTORY_BIN 库位盘点表 2_001_022_000 ==========
    ErrorCode INVENTORY_BIN_NOT_EXISTS = new ErrorCode(2_001_022_001, "库位盘点不存在");
    ErrorCode INVENTORY_BIN_EXISTS = new ErrorCode(2_001_022_002, "库位盘点已存在");
    ErrorCode INVENTORY_BIN_INVENTORY_ID_BIN_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_022_003, "盘点结果单ID,仓位ID,产品ID已存在");
    ErrorCode INVENTORY_BIN_BE_REFERRED = new ErrorCode(2_001_022_004, "库位盘点被引用");
    ErrorCode INVENTORY_BIN_CAN_NOT_EDIT = new ErrorCode(2_001_022_005, "库位盘点不允许编辑");
}
