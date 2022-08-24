package cn.iocoder.yudao.module.product.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Product 错误码枚举类
 *
 * product 系统，使用 1-008-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 商品分类相关 1008001000 ============
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1008001000, "商品分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1008001001, "父分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_FIRST_LEVEL = new ErrorCode(1008001002, "父分类不能是二级分类");
    ErrorCode PRODUCT_CATEGORY_EXISTS_CHILDREN = new ErrorCode(1008001003, "存在子分类，无法删除");
    ErrorCode PRODUCT_CATEGORY_DISABLED = new ErrorCode(1008001004, "商品分类({})已禁用，无法使用");
    ErrorCode PRODUCT_CATEGORY_LEVEL = new ErrorCode(1008001005, "商品需挂在三级分类下");

    // ========== 品牌相关编号 1008002000 ==========
    ErrorCode PRODUCT_BRAND_NOT_EXISTS = new ErrorCode(1008002000, "品牌不存在");

    // ========== 规格名称 1008003000 ==========
    ErrorCode PROPERTY_NOT_EXISTS = new ErrorCode(1008003000, "规格名称不存在");

    // ========== 规格值 1008004000 ==========
    ErrorCode PROPERTY_VALUE_NOT_EXISTS = new ErrorCode(1008004000, "规格值不存在");

    // ========== 商品spu 1008005000 ==========
    ErrorCode SPU_NOT_EXISTS = new ErrorCode(1008005000, "商品spu不存在");

    // ========== 商品sku 1008006000 ==========
    ErrorCode SKU_NOT_EXISTS = new ErrorCode(1008006000, "商品sku不存在");
    ErrorCode SKU_PROPERTIES_DUPLICATED = new ErrorCode(1008006001, "商品sku的属性组合存在重复");

    ErrorCode PRODUCT_SPU_ATTR_NUMBERS_MUST_BE_EQUALS = new ErrorCode(1008006002, "一个 Spu 下的每个 SKU ，其规格数必须一致");

    ErrorCode PRODUCT_SPU_SKU_NOT_DUPLICATE = new ErrorCode(1008006003, "一个 SPU 下的每个 SKU ，必须不重复");
}
