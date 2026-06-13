package cn.iocoder.yudao.module.wms.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * WMS 错误码枚举类
 * <p>
 * wms 系统，使用 1-060-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== WMS 基础数据-仓库 1-060-100-000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(1_060_100_000, "仓库不存在");
    ErrorCode WAREHOUSE_NAME_DUPLICATE = new ErrorCode(1_060_100_001, "仓库名称重复");
    ErrorCode WAREHOUSE_CODE_DUPLICATE = new ErrorCode(1_060_100_002, "仓库编号重复");
    ErrorCode WAREHOUSE_HAS_ORDER = new ErrorCode(1_060_100_004, "删除失败！仓库已被{}使用！");
    ErrorCode WAREHOUSE_HAS_INVENTORY = new ErrorCode(1_060_100_005, "删除失败！仓库已存在库存余额！");

    // ========== WMS 基础数据-商品分类 1-060-102-000 ==========
    ErrorCode ITEM_CATEGORY_NOT_EXISTS = new ErrorCode(1_060_102_000, "商品分类不存在");
    ErrorCode ITEM_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_060_102_001, "商品分类名称重复");
    ErrorCode ITEM_CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1_060_102_002, "父商品分类不存在");
    ErrorCode ITEM_CATEGORY_PARENT_ERROR = new ErrorCode(1_060_102_003, "不能设置自己为父商品分类");
    ErrorCode ITEM_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_060_102_004, "不能设置自己的子商品分类为父商品分类");
    ErrorCode ITEM_CATEGORY_HAS_CHILDREN = new ErrorCode(1_060_102_005, "删除失败！请先删除该分类下的子分类！");
    ErrorCode ITEM_CATEGORY_HAS_ITEM = new ErrorCode(1_060_102_006, "删除失败！分类已被商品使用！");
    ErrorCode ITEM_CATEGORY_CODE_DUPLICATE = new ErrorCode(1_060_102_007, "商品分类编号重复");

    // ========== WMS 基础数据-商品品牌 1-060-103-000 ==========
    ErrorCode ITEM_BRAND_NOT_EXISTS = new ErrorCode(1_060_103_000, "商品品牌不存在");
    ErrorCode ITEM_BRAND_HAS_ITEM = new ErrorCode(1_060_103_001, "删除失败！品牌已被商品使用！");
    ErrorCode ITEM_BRAND_CODE_DUPLICATE = new ErrorCode(1_060_103_002, "商品品牌编号重复");
    ErrorCode ITEM_BRAND_NAME_DUPLICATE = new ErrorCode(1_060_103_003, "商品品牌名称重复");

    // ========== WMS 基础数据-商品 1-060-104-000 ==========
    ErrorCode ITEM_NOT_EXISTS = new ErrorCode(1_060_104_000, "商品不存在");
    ErrorCode ITEM_NAME_DUPLICATE = new ErrorCode(1_060_104_001, "商品名称重复");
    ErrorCode ITEM_CODE_DUPLICATE = new ErrorCode(1_060_104_007, "商品编号重复");
    ErrorCode ITEM_SKU_REQUIRED = new ErrorCode(1_060_104_002, "至少包含一个商品规格");
    ErrorCode ITEM_SKU_NAME_DUPLICATE = new ErrorCode(1_060_104_003, "商品规格名称【{}】重复");
    ErrorCode ITEM_SKU_NOT_EXISTS = new ErrorCode(1_060_104_004, "商品规格不存在");
    ErrorCode ITEM_SKU_HAS_INVENTORY = new ErrorCode(1_060_104_005, "删除失败！商品规格【{}】已被库存业务使用！");
    ErrorCode ITEM_SKU_HAS_ORDER = new ErrorCode(1_060_104_006, "删除失败！商品规格【{}】已被{}使用！");

    // ========== WMS 基础数据-往来企业 1-060-105-000 ==========
    ErrorCode MERCHANT_NOT_EXISTS = new ErrorCode(1_060_105_000, "往来企业不存在");
    ErrorCode MERCHANT_NOT_SUPPLIER = new ErrorCode(1_060_105_001, "往来企业必须是供应商或客户/供应商类型");
    ErrorCode MERCHANT_NOT_CUSTOMER = new ErrorCode(1_060_105_002, "往来企业必须是客户或客户/供应商类型");
    ErrorCode MERCHANT_HAS_ORDER = new ErrorCode(1_060_105_003, "删除失败！往来企业已被{}使用！");
    ErrorCode MERCHANT_CODE_DUPLICATE = new ErrorCode(1_060_105_004, "往来企业编号重复");
    ErrorCode MERCHANT_NAME_DUPLICATE = new ErrorCode(1_060_105_005, "往来企业名称重复");

    // ========== WMS 入库单 1-060-200-000 ==========
    ErrorCode RECEIPT_ORDER_NOT_EXISTS = new ErrorCode(1_060_200_000, "入库单不存在");
    ErrorCode RECEIPT_ORDER_NO_DUPLICATE = new ErrorCode(1_060_200_001, "入库单号重复");
    ErrorCode RECEIPT_ORDER_STATUS_NOT_PREPARE = new ErrorCode(1_060_200_002, "入库单状态不是草稿，不能操作");
    ErrorCode RECEIPT_ORDER_DETAIL_REQUIRED = new ErrorCode(1_060_200_003, "入库单至少包含一条明细");
    ErrorCode RECEIPT_ORDER_STATUS_NOT_DELETABLE = new ErrorCode(1_060_200_005, "入库单状态不是草稿或已作废，不能删除");
    ErrorCode RECEIPT_ORDER_DETAIL_NOT_EXISTS = new ErrorCode(1_060_200_007, "入库单明细不存在");

    // ========== WMS 出库单 1-060-201-000 ==========
    ErrorCode SHIPMENT_ORDER_NOT_EXISTS = new ErrorCode(1_060_201_000, "出库单不存在");
    ErrorCode SHIPMENT_ORDER_NO_DUPLICATE = new ErrorCode(1_060_201_001, "出库单号重复");
    ErrorCode SHIPMENT_ORDER_STATUS_NOT_PREPARE = new ErrorCode(1_060_201_002, "出库单状态不是草稿，不能操作");
    ErrorCode SHIPMENT_ORDER_DETAIL_REQUIRED = new ErrorCode(1_060_201_003, "出库单至少包含一条明细");
    ErrorCode SHIPMENT_ORDER_STATUS_NOT_DELETABLE = new ErrorCode(1_060_201_005, "出库单状态不是草稿或已作废，不能删除");
    ErrorCode SHIPMENT_ORDER_DETAIL_NOT_EXISTS = new ErrorCode(1_060_201_007, "出库单明细不存在");

    // ========== WMS 移库单 1-060-202-000 ==========
    ErrorCode MOVEMENT_ORDER_NOT_EXISTS = new ErrorCode(1_060_202_000, "移库单不存在");
    ErrorCode MOVEMENT_ORDER_NO_DUPLICATE = new ErrorCode(1_060_202_001, "移库单号重复");
    ErrorCode MOVEMENT_ORDER_STATUS_NOT_PREPARE = new ErrorCode(1_060_202_002, "移库单状态不是草稿，不能操作");
    ErrorCode MOVEMENT_ORDER_DETAIL_REQUIRED = new ErrorCode(1_060_202_003, "移库单至少包含一条明细");
    ErrorCode MOVEMENT_ORDER_STATUS_NOT_DELETABLE = new ErrorCode(1_060_202_005, "移库单状态不是草稿或已作废，不能删除");
    ErrorCode MOVEMENT_ORDER_DETAIL_NOT_EXISTS = new ErrorCode(1_060_202_006, "移库单明细不存在");
    ErrorCode MOVEMENT_ORDER_WAREHOUSE_SAME = new ErrorCode(1_060_202_007, "来源仓库和目标仓库不能相同");

    // ========== WMS 盘库单 1-060-203-000 ==========
    ErrorCode CHECK_ORDER_NOT_EXISTS = new ErrorCode(1_060_203_000, "盘库单不存在");
    ErrorCode CHECK_ORDER_NO_DUPLICATE = new ErrorCode(1_060_203_001, "盘库单号重复");
    ErrorCode CHECK_ORDER_STATUS_NOT_PREPARE = new ErrorCode(1_060_203_002, "盘库单状态不是草稿，不能操作");
    ErrorCode CHECK_ORDER_DETAIL_REQUIRED = new ErrorCode(1_060_203_003, "盘库单至少包含一条明细");
    ErrorCode CHECK_ORDER_STATUS_NOT_DELETABLE = new ErrorCode(1_060_203_005, "盘库单状态不是草稿或已作废，不能删除");
    ErrorCode CHECK_ORDER_DETAIL_NOT_EXISTS = new ErrorCode(1_060_203_006, "盘库单明细不存在");
    ErrorCode CHECK_ORDER_INVENTORY_CHANGED = new ErrorCode(1_060_203_007, "盘库单库存已变化，请重新加载库存后再完成");

    // ========== WMS 库存 1-060-300-000 ==========
    ErrorCode INVENTORY_QUANTITY_NOT_ENOUGH = new ErrorCode(1_060_300_000,
            "库存不足，商品：{}，商品规格：{}，仓库：{}，当前库存：{}，变更数量：{}");

}
