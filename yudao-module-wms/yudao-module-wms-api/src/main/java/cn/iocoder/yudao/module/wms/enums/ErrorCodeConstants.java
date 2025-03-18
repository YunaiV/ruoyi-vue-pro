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

    // ========== WMS_WAREHOUSE 仓库 2_001_100_000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(2_001_100_000, "仓库不存在");
    ErrorCode WAREHOUSE_NAME_DUPLICATE = new ErrorCode(2_001_100_001, "名称已存在");
    ErrorCode WAREHOUSE_CODE_DUPLICATE = new ErrorCode(2_001_100_002, "代码已存在");
    ErrorCode WAREHOUSE_BE_REFERRED = new ErrorCode(2_001_100_003, "仓库被引用");

    // ========== WMS_WAREHOUSE_AREA 仓库 2_001_200_000 ==========
    ErrorCode WAREHOUSE_AREA_NOT_EXISTS = new ErrorCode(2_001_200_000, "库区不存在");
    ErrorCode WAREHOUSE_AREA_CODE_DUPLICATE = new ErrorCode(2_001_200_001, "代码已存在");
    ErrorCode WAREHOUSE_AREA_BE_REFERRED = new ErrorCode(2_001_200_002, "库区被引用");

    // ========== WMS_WAREHOUSE_LOCATION 仓库 2_001_300_000 ==========
    ErrorCode WAREHOUSE_LOCATION_NOT_EXISTS = new ErrorCode(2_001_300_000, "库位不存在");
    ErrorCode WAREHOUSE_LOCATION_CODE_DUPLICATE = new ErrorCode(2_001_300_001, "代码已存在");
    ErrorCode WAREHOUSE_LOCATION_BE_REFERRED = new ErrorCode(2_001_300_002, "库位被引用");

    // ========== WMS_INBOUND 入库单 2_001_400_000 ==========
    ErrorCode INBOUND_NOT_EXISTS = new ErrorCode(2_001_400_001, "入库单不存在");
    ErrorCode INBOUND_NO_DUPLICATE = new ErrorCode(2_001_400_002, "单据号已存在");
    ErrorCode INBOUND_BE_REFERRED = new ErrorCode(2_001_400_003, "入库单被引用");
    ErrorCode INBOUND_NO_OUT_OF_BOUNDS = new ErrorCode(2_001_400_004, "入库单单据号越界");
    ErrorCode INBOUND_ITEM_PRODUCT_ID_REPEATED = new ErrorCode(2_001_400_005, "入库单标准产品ID重复");
    ErrorCode INBOUND_ITEM_PRODUCT_SKU_REPEATED = new ErrorCode(2_001_400_006, "入库单标准产品SKU重复");

    // ========== WMS_INBOUND_ITEM 入库单详情表 2_001_500_000 ==========
    ErrorCode INBOUND_ITEM_NOT_EXISTS = new ErrorCode(2_001_500_001, "入库单详情不存在");
    ErrorCode INBOUND_ITEM_INBOUND_ID_PRODUCT_ID_DUPLICATE = new ErrorCode(2_001_500_002, "入库单ID,标准产品ID已存在");
    ErrorCode INBOUND_ITEM_INBOUND_ID_PRODUCT_SKU_DUPLICATE = new ErrorCode(2_001_500_003, "入库单ID,标准产品SKU已存在");
    ErrorCode INBOUND_ITEM_BE_REFERRED = new ErrorCode(2_001_500_004, "入库单详情被引用");

    // ========== WMS_INBOUND_ITEM_FLOW 入库单库存详情扣减表 2_001_600_000 ==========
    ErrorCode INBOUND_ITEM_FLOW_NOT_EXISTS = new ErrorCode(2_001_600_001, "入库单库存详情扣减不存在");
    ErrorCode INBOUND_ITEM_FLOW_BE_REFERRED = new ErrorCode(2_001_600_002, "入库单库存详情扣减被引用");
}