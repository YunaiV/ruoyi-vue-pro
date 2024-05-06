package cn.iocoder.yudao.module.weapp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode APPS_LIST_NOT_EXISTS = new ErrorCode(1_001_005_001, "小程序清单不存在");
    ErrorCode APPS_CLASS_NOT_EXISTS = new ErrorCode(1_001_005_002, "小程序分类不存在");

}
