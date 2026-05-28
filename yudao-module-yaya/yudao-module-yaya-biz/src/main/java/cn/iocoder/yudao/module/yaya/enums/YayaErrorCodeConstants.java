package cn.iocoder.yudao.module.yaya.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Yaya error codes.
 */
public interface YayaErrorCodeConstants {

    ErrorCode YAYA_SEASON_NOT_EXISTS = new ErrorCode(1_050_000_001, "Yaya 内容季不存在");
    ErrorCode YAYA_SEASON_KEY_DUPLICATE = new ErrorCode(1_050_000_002, "Yaya 内容季 key 已存在");

    ErrorCode YAYA_TOPIC_NOT_EXISTS = new ErrorCode(1_050_001_001, "Yaya 话题不存在");
    ErrorCode YAYA_TOPIC_STABLE_KEY_DUPLICATE = new ErrorCode(1_050_001_002, "Yaya 话题 stable key 已存在");

    ErrorCode YAYA_MEMBER_NOT_LOGIN = new ErrorCode(1_050_002_001, "Yaya 会员未登录");

}
