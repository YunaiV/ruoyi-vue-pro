package cn.iocoder.yudao.module.product.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Product 错误码枚举类
 *
 * product 系统，使用 1-008-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 商品分类相关 1008001000 ============
    ErrorCode CATEGORY_NOT_EXISTS = new ErrorCode(1008001000, "商品分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1008001001, "父分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_FIRST_LEVEL = new ErrorCode(1008001002, "父分类不能是二级分类");
    ErrorCode CATEGORY_EXISTS_CHILDREN = new ErrorCode(1008001003, "存在子分类，无法删除");
    ErrorCode CATEGORY_DISABLED = new ErrorCode(1008001004, "商品分类({})已禁用，无法使用");
    ErrorCode CATEGORY_LEVEL_ERROR = new ErrorCode(1008001005, "商品分类不正确，原因：必须使用第三级的商品分类下");

    // ========== 商品品牌相关编号 1008002000 ==========
    ErrorCode BRAND_NOT_EXISTS = new ErrorCode(1008002000, "品牌不存在");
    ErrorCode BRAND_DISABLED = new ErrorCode(1008002001, "品牌不存在");
    ErrorCode BRAND_NAME_EXISTS = new ErrorCode(1008002002, "品牌名称已存在");

    // ========== 商品规格名称 1008003000 ==========
    ErrorCode PROPERTY_NOT_EXISTS = new ErrorCode(1008003000, "规格名称不存在");

    // ========== 规格值 1008004000 ==========
    ErrorCode PROPERTY_VALUE_NOT_EXISTS = new ErrorCode(1008004000, "规格值不存在");

    // ========== 商品 SPU 1008005000 ==========
    ErrorCode SPU_NOT_EXISTS = new ErrorCode(1008005000, "商品 SPU 不存在");

    // ========== 商品 SKU 1008006000 ==========
    ErrorCode SKU_NOT_EXISTS = new ErrorCode(1008006000, "商品 SKU 不存在");
    ErrorCode SKU_PROPERTIES_DUPLICATED = new ErrorCode(1008006001, "商品 SKU 的属性组合存在重复");
    ErrorCode SPU_ATTR_NUMBERS_MUST_BE_EQUALS = new ErrorCode(1008006002, "一个 SPU 下的每个 SKU，其规格数必须一致");
    ErrorCode SPU_SKU_NOT_DUPLICATE = new ErrorCode(1008006003, "一个 SPU 下的每个 SKU，必须不重复");

}
