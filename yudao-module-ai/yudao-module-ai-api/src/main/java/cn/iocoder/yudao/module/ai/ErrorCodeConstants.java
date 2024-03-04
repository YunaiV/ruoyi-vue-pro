package cn.iocoder.yudao.module.ai;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 模块 ai 错误码区间 [1-022-000-000 ~ 1-023-000-000) ==========

    ErrorCode AI_MODULE_NOT_SUPPORTED = new ErrorCode(1_022_000_000, "AI模型暂不支持!");

}
