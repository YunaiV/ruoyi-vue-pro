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
    ErrorCode INBOUND_ITEM_PRODUCT_SKU_REPEATED = new ErrorCode(2_001_004_006, "入库单标准产品SKU重复");
    ErrorCode INBOUND_CAN_NOT_EDIT = new ErrorCode(2_001_004_007, "入库单不允许编辑");
    ErrorCode INBOUND_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_004_008, "入库单不符合审批条件");
    ErrorCode INBOUND_CAN_NOT_DELETE = new ErrorCode(2_001_004_009, "入库单不允许删除");

    // ========== WMS_INBOUND_ITEM 入库单详情表 2_001_005_000 ==========
    ErrorCode INBOUND_ITEM_NOT_EXISTS = new ErrorCode(2_001_005_001, "入库单详情不存在");
    ErrorCode INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_005_002, "入库单ID,标准产品ID已存在");
    ErrorCode INBOUND_ITEM_INBOUND_ID_PRODUCT_SKU_DUPLICATE = new ErrorCode(2_001_005_003, "入库单ID,标准产品SKU已存在");
    ErrorCode INBOUND_ITEM_BE_REFERRED = new ErrorCode(2_001_005_004, "入库单详情被引用");
    ErrorCode INBOUND_ITEM_CAN_NOT_EDIT = new ErrorCode(2_001_005_005, "入库单详情不允许编辑");
    ErrorCode INBOUND_ITEM_CAN_NOT_DELETE = new ErrorCode(2_001_005_006, "入库单详情不允许删除");
    ErrorCode INBOUND_ITEM_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_005_007, "入库单详情不符合审批条件");
    ErrorCode INBOUND_ITEM_INBOUND_ID_DUPLICATE = new ErrorCode(2_001_005_008, "入库单ID不允许重复");

    // ========== WMS_INBOUND_ITEM_FLOW 入库单库存详情扣减表 2_001_006_000 ==========
    ErrorCode INBOUND_ITEM_FLOW_NOT_EXISTS = new ErrorCode(2_001_006_001, "入库单库存详情扣减不存在");
    ErrorCode INBOUND_ITEM_FLOW_BE_REFERRED = new ErrorCode(2_001_006_002, "入库单库存详情扣减被引用");
    ErrorCode INBOUND_ITEM_FLOW_CAN_NOT_EDIT = new ErrorCode(2_001_006_003, "入库单库存详情扣减不允许编辑");
    ErrorCode INBOUND_ITEM_FLOW_CAN_NOT_DELETE = new ErrorCode(2_001_006_004, "入库单库存详情扣减不允许删除");
    ErrorCode INBOUND_ITEM_FLOW_APPROVAL_CONDITION_IS_NOT_MATCH = new ErrorCode(2_001_006_005, "入库单库存详情扣减不符合审批条件");

    // ========== WMS_APPROVAL_HISTORY 审批历史表 2_001_007_000 ==========
    ErrorCode APPROVAL_HISTORY_NOT_EXISTS = new ErrorCode(2_001_007_001, "审批历史不存在");
    ErrorCode APPROVAL_HISTORY_BE_REFERRED = new ErrorCode(2_001_007_002, "审批历史被引用");
    ErrorCode APPROVAL_HISTORY_CAN_NOT_EDIT = new ErrorCode(2_001_007_003, "审批历史不允许编辑");

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
}