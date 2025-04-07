package com.somle.esb.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface ErrorCodeConstants {
    // ========== 同模块系统内部异常（1-020-000-000） ==========
    ErrorCode DEPT_LEVEL_ERROR = new ErrorCode(1 - 020 - 000 - 000, "经校验部门层级为0，请检查部门层级是否正确");

}
