package cn.iocoder.yudao.module.oms.api.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface OmsErrorCodeConstants {
    // ========== OMS 店铺 1-040-500-000 ==========
    ErrorCode SHOP_NOT_EXISTS = new ErrorCode(1_040_500_000, "店铺不存在");
}
