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

    // ========== 品牌相关编号 1008002000 ==========
    ErrorCode BRAND_NOT_EXISTS = new ErrorCode(1008002000, "品牌不存在");
}
