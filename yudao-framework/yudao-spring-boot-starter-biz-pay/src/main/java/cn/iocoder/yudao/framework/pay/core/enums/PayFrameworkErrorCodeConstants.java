package cn.iocoder.yudao.framework.pay.core.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 支付框架的错误码枚举
 *
 * 支付框架，使用 2-002-000-000 段
 *
 * @author 芋道源码
 */
public interface PayFrameworkErrorCodeConstants {

    ErrorCode ORDER_UNIFIED_ERROR = new ErrorCode(2002000000, "发起支付失败，原因：{}");

}
