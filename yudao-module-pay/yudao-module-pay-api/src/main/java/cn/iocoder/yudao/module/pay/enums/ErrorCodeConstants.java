package cn.iocoder.yudao.module.pay.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Pay 错误码 Core 枚举类
 *
 * pay 系统，使用 1-007-000-000 段
 */
public interface ErrorCodeConstants {

    /**
     * ========== APP 模块 1-007-000-000 ==========
     */
    ErrorCode PAY_APP_NOT_FOUND = new ErrorCode(1007000000, "App 不存在");
    ErrorCode PAY_APP_IS_DISABLE = new ErrorCode(1007000002, "App 已经被禁用");
    ErrorCode PAY_APP_EXIST_TRANSACTION_ORDER_CANT_DELETE =  new ErrorCode(1007000003, "支付应用存在交易中的订单,无法删除");

    /**
     * ========== CHANNEL 模块 1-007-001-000 ==========
     */
    ErrorCode PAY_CHANNEL_NOT_FOUND = new ErrorCode(1007001000, "支付渠道的配置不存在");
    ErrorCode PAY_CHANNEL_IS_DISABLE = new ErrorCode(1007001001, "支付渠道已经禁用");
    ErrorCode PAY_CHANNEL_CLIENT_NOT_FOUND = new ErrorCode(1007001002, "支付渠道的客户端不存在");
    ErrorCode CHANNEL_NOT_EXISTS = new ErrorCode(1007001003, "支付渠道不存在");
    ErrorCode CHANNEL_EXIST_SAME_CHANNEL_ERROR = new ErrorCode(1007001005, "已存在相同的渠道");
    ErrorCode CHANNEL_WECHAT_VERSION_2_MCH_KEY_IS_NULL = new ErrorCode(1007001006,"微信渠道v2版本中商户密钥不可为空");
    ErrorCode CHANNEL_WECHAT_VERSION_3_PRIVATE_KEY_IS_NULL = new ErrorCode(1007001007,"微信渠道v3版本apiclient_key.pem不可为空");
    ErrorCode CHANNEL_WECHAT_VERSION_3_CERT_KEY_IS_NULL = new ErrorCode(1007001008,"微信渠道v3版本中apiclient_cert.pem不可为空");
    ErrorCode PAY_CHANNEL_NOTIFY_VERIFY_FAILED = new ErrorCode(1007001009, "渠道通知校验失败");

    // ========== ORDER 模块 1-007-002-000 ==========

    ErrorCode PAY_ORDER_NOT_FOUND = new ErrorCode(1007002000, "支付订单不存在");
    ErrorCode PAY_ORDER_STATUS_IS_NOT_WAITING = new ErrorCode(1007002001, "支付订单不处于待支付");
    ErrorCode PAY_ORDER_STATUS_IS_NOT_SUCCESS = new ErrorCode(1007002002, "支付订单不处于已支付");
    ErrorCode PAY_ORDER_ERROR_USER = new ErrorCode(1007002003, "支付订单用户不正确");

    /**
     * ========== ORDER 模块(拓展单) 1-007-003-000 ==========
     */
    ErrorCode PAY_ORDER_EXTENSION_NOT_FOUND = new ErrorCode(1007003000, "支付交易拓展单不存在");
    ErrorCode PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING = new ErrorCode(1007003001, "支付交易拓展单不处于待支付");
    ErrorCode PAY_ORDER_EXTENSION_STATUS_IS_NOT_SUCCESS = new ErrorCode(1007003002, "支付订单不处于已支付");

    // ========== 支付模块(退款) 1-007-006-000 ==========
    ErrorCode PAY_REFUND_AMOUNT_EXCEED = new ErrorCode(1007006000, "退款金额超过订单可退款金额");
    ErrorCode PAY_REFUND_ALL_REFUNDED = new ErrorCode(1007006001, "订单已经全额退款");
    ErrorCode PAY_REFUND_CHN_ORDER_NO_IS_NULL = new ErrorCode(1007006002, "该订单的渠道订单为空");
    ErrorCode PAY_REFUND_SUCCEED = new ErrorCode(1007006003, "已经退款成功");
    ErrorCode PAY_REFUND_NOT_FOUND = new ErrorCode(1007006004, "支付退款单不存在");


    /**
     * ========== 支付商户信息 1-007-004-000 ==========
     */
    ErrorCode PAY_MERCHANT_NOT_EXISTS = new ErrorCode(1007004000, "支付商户信息不存在");
    ErrorCode PAY_MERCHANT_EXIST_APP_CANT_DELETE = new ErrorCode(1007004001, "支付商户存在支付应用,无法删除");

}
