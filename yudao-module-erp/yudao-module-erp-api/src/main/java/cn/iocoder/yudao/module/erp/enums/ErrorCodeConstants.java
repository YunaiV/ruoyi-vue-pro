package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 销售订单（1-030-000-000） ==========
    ErrorCode SALE_ORDER_NOT_EXISTS = new ErrorCode(1_030_000_000, "销售订单不存在");

}
