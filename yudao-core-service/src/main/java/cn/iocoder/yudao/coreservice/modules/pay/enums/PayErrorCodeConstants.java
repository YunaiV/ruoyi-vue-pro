package cn.iocoder.yudao.coreservice.modules.pay.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Pay 错误码枚举类
 *
 * pay 系统，使用 1-007-000-000 段
 */
public interface PayErrorCodeConstants {

    // ========== APP 模块 1-007-000-000 ==========
    ErrorCode PAY_APP_NOT_FOUND = new ErrorCode(1007000000, "App 不存在");
    ErrorCode PAY_APP_IS_DISABLE = new ErrorCode(1007000002, "App 已经被禁用");

}
