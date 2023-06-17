package cn.iocoder.yudao.module.point.enums;


import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Pay 错误码 Core 枚举类
 *
 * pay 系统，使用 1-007-000-000 段
 */
public interface ErrorCodeConstants {

    ErrorCode CONFIG_NOT_EXISTS = new ErrorCode(499, "积分设置不存在");

    ErrorCode CONFIG_EXISTS = new ErrorCode(499, "积分设置已存在，只允配置一条记录");


    ErrorCode SIGN_IN_CONFIG_NOT_EXISTS = new ErrorCode(499, "签到天数规则不存在");
    ErrorCode SIGN_IN_CONFIG_EXISTS = new ErrorCode(499, "签到天数规则已存在");

    ErrorCode RECORD_NOT_EXISTS = new ErrorCode( 499, "用户积分记录不存在");

    ErrorCode SIGN_IN_RECORD_NOT_EXISTS = new ErrorCode(499, "用户签到积分不存在");



}
