package cn.iocoder.yudao.module.pay.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Pay 错误码 Core 枚举类
 *
 * pay 系统，使用 1-007-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== APP 模块 1-007-000-000 ==========
    ErrorCode APP_NOT_FOUND = new ErrorCode(1_007_000_000, "App 不存在");
    ErrorCode APP_IS_DISABLE = new ErrorCode(1_007_000_002, "App 已经被禁用");
    ErrorCode APP_EXIST_ORDER_CANT_DELETE =  new ErrorCode(1_007_000_003, "支付应用存在支付订单，无法删除");
    ErrorCode APP_EXIST_REFUND_CANT_DELETE =  new ErrorCode(1_007_000_004, "支付应用存在退款订单，无法删除");

    // ========== CHANNEL 模块 1-007-001-000 ==========
    ErrorCode CHANNEL_NOT_FOUND = new ErrorCode(1_007_001_000, "支付渠道的配置不存在");
    ErrorCode CHANNEL_IS_DISABLE = new ErrorCode(1_007_001_001, "支付渠道已经禁用");
    ErrorCode CHANNEL_EXIST_SAME_CHANNEL_ERROR = new ErrorCode(1_007_001_004, "已存在相同的渠道");

    // ========== ORDER 模块 1-007-002-000 ==========
    ErrorCode PAY_ORDER_NOT_FOUND = new ErrorCode(1_007_002_000, "支付订单不存在");
    ErrorCode PAY_ORDER_STATUS_IS_NOT_WAITING = new ErrorCode(1_007_002_001, "支付订单不处于待支付");
    ErrorCode PAY_ORDER_STATUS_IS_SUCCESS = new ErrorCode(1_007_002_002, "订单已支付，请刷新页面");
    ErrorCode PAY_ORDER_IS_EXPIRED = new ErrorCode(1_007_002_003, "支付订单已经过期");
    ErrorCode PAY_ORDER_SUBMIT_CHANNEL_ERROR = new ErrorCode(1_007_002_004, "发起支付报错，错误码：{}，错误提示：{}");
    ErrorCode PAY_ORDER_REFUND_FAIL_STATUS_ERROR = new ErrorCode(1_007_002_005, "支付订单退款失败，原因：状态不是已支付或已退款");

    // ========== ORDER 模块(拓展单) 1-007-003-000 ==========
    ErrorCode PAY_ORDER_EXTENSION_NOT_FOUND = new ErrorCode(1_007_003_000, "支付交易拓展单不存在");
    ErrorCode PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING = new ErrorCode(1_007_003_001, "支付交易拓展单不处于待支付");
    ErrorCode PAY_ORDER_EXTENSION_IS_PAID = new ErrorCode(1_007_003_002, "订单已支付，请等待支付结果");

    // ========== 支付模块(退款) 1-007-006-000 ==========
    ErrorCode REFUND_PRICE_EXCEED = new ErrorCode(1_007_006_000, "退款金额超过订单可退款金额");
    ErrorCode REFUND_HAS_REFUNDING = new ErrorCode(1_007_006_002, "已经有退款在处理中");
    ErrorCode REFUND_EXISTS = new ErrorCode(1_007_006_003, "已经存在退款单");
    ErrorCode REFUND_NOT_FOUND = new ErrorCode(1_007_006_004, "支付退款单不存在");
    ErrorCode REFUND_STATUS_IS_NOT_WAITING = new ErrorCode(1_007_006_005, "支付退款单不处于待退款");

    // ========== 钱包模块 1-007-007-000 ==========
    ErrorCode WALLET_NOT_FOUND = new ErrorCode(1_007_007_000, "用户钱包不存在");
    ErrorCode WALLET_BALANCE_NOT_ENOUGH = new ErrorCode(1_007_007_001, "钱包余额不足");
    ErrorCode WALLET_TRANSACTION_NOT_FOUND = new ErrorCode(1_007_007_002, "未找到对应的钱包交易");
    ErrorCode WALLET_REFUND_EXIST = new ErrorCode(1_007_007_003, "已经存在钱包退款");
    ErrorCode WALLET_FREEZE_PRICE_NOT_ENOUGH = new ErrorCode(1_007_007_004, "钱包冻结余额不足");

    // ========== 钱包充值模块 1-007-008-000 ==========
    ErrorCode WALLET_RECHARGE_NOT_FOUND = new ErrorCode(1_007_008_000, "钱包充值记录不存在");
    ErrorCode WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(1_007_008_001, "钱包充值更新支付状态失败，钱包充值记录不是【未支付】状态");
    ErrorCode WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR = new ErrorCode(1_007_008_002, "钱包充值更新支付状态失败，支付单编号不匹配");
    ErrorCode WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(1_007_008_003, "钱包充值更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH = new ErrorCode(1_007_008_004, "钱包充值更新支付状态失败，支付单金额不匹配");
    ErrorCode WALLET_RECHARGE_REFUND_FAIL_NOT_PAID = new ErrorCode(1_007_008_005, "钱包发起退款失败，钱包充值订单未支付");
    ErrorCode WALLET_RECHARGE_REFUND_FAIL_REFUNDED = new ErrorCode(1_007_008_006, "钱包发起退款失败，钱包充值订单已退款");
    ErrorCode WALLET_RECHARGE_REFUND_BALANCE_NOT_ENOUGH = new ErrorCode(1_007_008_007, "钱包发起退款失败，钱包余额不足");
    ErrorCode WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR = new ErrorCode(1_007_008_008, "钱包退款更新失败，钱包退款单编号不匹配");
    ErrorCode WALLET_RECHARGE_REFUND_FAIL_REFUND_NOT_FOUND = new ErrorCode(1_007_008_009, "钱包退款更新失败，退款订单不存在");
    ErrorCode WALLET_RECHARGE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH = new ErrorCode(1_007_008_010, "钱包退款更新失败，退款单金额不匹配");
    ErrorCode WALLET_RECHARGE_PACKAGE_NOT_FOUND = new ErrorCode(1_007_008_011, "钱包充值套餐不存在");
    ErrorCode WALLET_RECHARGE_PACKAGE_IS_DISABLE = new ErrorCode(1_007_008_012, "钱包充值套餐已禁用");
    ErrorCode WALLET_RECHARGE_PACKAGE_NAME_EXISTS = new ErrorCode(1_007_008_013, "钱包充值套餐名称已存在");

    // ========== 转账模块 1-007-009-000 ==========
    ErrorCode PAY_TRANSFER_SUBMIT_CHANNEL_ERROR = new ErrorCode(1_007_009_000, "发起转账报错，错误码：{}，错误提示：{}");
    ErrorCode PAY_TRANSFER_NOT_FOUND = new ErrorCode(1_007_009_001, "转账单不存在");
    ErrorCode PAY_SAME_MERCHANT_TRANSFER_TYPE_NOT_MATCH = new ErrorCode(1_007_009_002, "两次相同转账请求的类型不匹配");
    ErrorCode PAY_SAME_MERCHANT_TRANSFER_PRICE_NOT_MATCH = new ErrorCode(1_007_009_003, "两次相同转账请求的金额不匹配");
    ErrorCode PAY_MERCHANT_TRANSFER_EXISTS = new ErrorCode(1_007_009_004, "该笔业务的转账已经发起,请查询转账订单相关状态");
    ErrorCode PAY_TRANSFER_STATUS_IS_NOT_WAITING = new ErrorCode(1_007_009_005, "转账单不处于待转账");
    ErrorCode PAY_TRANSFER_STATUS_IS_NOT_PENDING = new ErrorCode(1_007_009_006, "转账单不处于待转账或转账中");

    // ========== 示例订单 1-007-900-000 ==========
    ErrorCode DEMO_ORDER_NOT_FOUND = new ErrorCode(1_007_900_000, "示例订单不存在");
    ErrorCode DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(1_007_900_001, "示例订单更新支付状态失败，订单不是【未支付】状态");
    ErrorCode DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(1_007_900_002, "示例订单更新支付状态失败，支付单编号不匹配");
    ErrorCode DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(1_007_900_003, "示例订单更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode DEMO_ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(1_007_900_004, "示例订单更新支付状态失败，支付单金额不匹配");
    ErrorCode DEMO_ORDER_REFUND_FAIL_NOT_PAID = new ErrorCode(1_007_900_005, "发起退款失败，示例订单未支付");
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUNDED = new ErrorCode(1_007_900_006, "发起退款失败，示例订单已退款");
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_NOT_FOUND = new ErrorCode(1_007_900_007, "发起退款失败，退款订单不存在");
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_NOT_SUCCESS = new ErrorCode(1_007_900_008, "发起退款失败，退款订单未退款成功");
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_ORDER_ID_ERROR = new ErrorCode(1_007_900_009, "发起退款失败，退款单编号不匹配");
    ErrorCode DEMO_ORDER_REFUND_FAIL_REFUND_PRICE_NOT_MATCH = new ErrorCode(1_007_900_010, "发起退款失败，退款单金额不匹配");

    // ========== 示例转账订单 1-007-901-001 ==========
    ErrorCode DEMO_TRANSFER_NOT_FOUND = new ErrorCode(1_007_901_001, "示例转账单不存在");
    ErrorCode DEMO_TRANSFER_FAIL_TRANSFER_ID_ERROR = new ErrorCode(1_007_901_002, "转账失败，转账单编号不匹配");
    ErrorCode DEMO_TRANSFER_FAIL_PRICE_NOT_MATCH = new ErrorCode(1_007_901_003, "转账失败，转账单金额不匹配");
}
