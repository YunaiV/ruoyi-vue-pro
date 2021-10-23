package cn.iocoder.yudao.framework.pay.core.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 支付框架的错误码枚举
 *
 * 短信框架，使用 2-002-000-000 段
 *
 * @author 芋道源码
 */
public interface PayFrameworkErrorCodeConstants {

    ErrorCode PAY_UNKNOWN = new ErrorCode(2002000000, "未知错误，需要解析");

    // ========== 配置相关相关 2002000100 ==========
    ErrorCode PAY_CONFIG_APP_ID_ERROR = new ErrorCode(2002000100, "支付渠道 AppId 不正确");
    ErrorCode PAY_CONFIG_SIGN_ERROR = new ErrorCode(2002000100, "签名错误"); // 例如说，微信支付，配置错了 mchId 或者 mchKey


    // ========== 其它相关 2002000900 开头 ==========
    ErrorCode PAY_OPENID_ERROR = new ErrorCode(2002000900, "无效的 openid"); // 例如说，微信 openid 未授权过
    ErrorCode PAY_PARAM_MISSING = new ErrorCode(2002000901, "请求参数缺失"); // 例如说，支付少传了金额

    ErrorCode EXCEPTION = new ErrorCode(2002000999, "调用异常");

}
