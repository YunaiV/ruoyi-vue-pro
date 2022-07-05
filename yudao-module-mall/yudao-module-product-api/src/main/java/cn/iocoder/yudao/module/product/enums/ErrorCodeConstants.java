package cn.iocoder.yudao.module.product.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * product 错误码枚举类
 * <p>
 * product 系统，使用 1-008-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 商品分类相关  1008001000============
    ErrorCode CATEGORY_NOT_EXISTS = new ErrorCode(1008001000, "商品分类不存在");
    ErrorCode CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1008001001, "父分类不存在");
    ErrorCode CATEGORY_EXISTS_CHILDREN = new ErrorCode(1008001002, "存在子分类，无法删除");

    // ========== 品牌相关编号 1008002000 ==========
    ErrorCode BRAND_NOT_EXISTS = new ErrorCode(1008002000, "品牌不存在");

    // ========== 规格名称 1008003000 ==========
    ErrorCode PROPERTY_NOT_EXISTS = new ErrorCode(1008003000, "规格名称不存在");

    // ========== 规格值 1008004000 ==========
    ErrorCode PROPERTY_VALUE_NOT_EXISTS = new ErrorCode(1008004000, "规格值不存在");

    // ========== 商品spu 1008005000 ==========
    ErrorCode SPU_NOT_EXISTS = new ErrorCode(1008005000, "商品spu不存在");

    // ========== 商品sku 1008006000 ==========
    ErrorCode SKU_NOT_EXISTS = new ErrorCode(1008006000, "商品sku不存在");
    ErrorCode SKU_PROPERTIES_DUPLICATED = new ErrorCode(1008006001, "商品sku的属性组合存在重复");
}
