package cn.iocoder.yudao.framework.common.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface ErrorCodeConstants {

    ErrorCode AOP_ENHANCED_EXCEPTION = new ErrorCode(100001, "AOP增强异常");

    // ========== 模板（1-030-100-000） ==========
    ErrorCode GENERATE_CONTRACT_FAIL = new ErrorCode(500001, "获取模板({})失败,({})");
    ErrorCode GENERATE_CONTRACT_FAIL_PARSE = new ErrorCode(500002, "解析模板文件({})失败,({})");
}
