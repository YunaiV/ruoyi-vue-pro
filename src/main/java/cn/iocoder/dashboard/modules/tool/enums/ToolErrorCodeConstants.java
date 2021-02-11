package cn.iocoder.dashboard.modules.tool.enums;

import cn.iocoder.dashboard.common.exception.ErrorCode;

/**
 * Tool 错误码枚举类
 *
 * tool 系统，使用 1-003-000-000 段
 */
public interface ToolErrorCodeConstants {

    // ========== 字典类型（测试） 1003000000 ==========
    ErrorCode TEST_DEMO_NOT_EXISTS = new ErrorCode(1003000000, "测试示例不存在");

}
