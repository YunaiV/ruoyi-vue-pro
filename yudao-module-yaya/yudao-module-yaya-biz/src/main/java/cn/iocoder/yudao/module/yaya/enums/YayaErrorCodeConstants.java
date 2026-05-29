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

    ErrorCode YAYA_AI_UNAUTHORIZED = new ErrorCode(1_050_003_001, "Yaya AI 服务鉴权失败");
    ErrorCode YAYA_AI_REQUEST_FAILED = new ErrorCode(1_050_003_002, "Yaya AI 服务请求失败");
    ErrorCode YAYA_AI_TASK_NOT_EXISTS = new ErrorCode(1_050_003_003, "Yaya AI 任务不存在");

    ErrorCode YAYA_RECORDING_FILE_REQUIRED = new ErrorCode(1_050_004_001, "Yaya 录音文件不能为空");
    ErrorCode YAYA_RECORDING_NOT_EXISTS = new ErrorCode(1_050_004_002, "Yaya 录音不存在");

    ErrorCode YAYA_EVALUATION_NOT_EXISTS = new ErrorCode(1_050_005_001, "Yaya 测评不存在");

    ErrorCode YAYA_MEMBER_PLAN_NOT_EXISTS = new ErrorCode(1_050_006_001, "Yaya 会员套餐不存在");
    ErrorCode YAYA_MEMBER_PLAN_KEY_DUPLICATE = new ErrorCode(1_050_006_002, "Yaya 会员套餐 key 已存在");
    ErrorCode YAYA_ENTITLEMENT_REQUIRED = new ErrorCode(1_050_006_003, "Yaya 会员权益不足");

    ErrorCode YAYA_MEMBER_ORDER_NOT_EXISTS = new ErrorCode(1_050_007_001, "Yaya 会员订单不存在");
    ErrorCode YAYA_MEMBER_ORDER_PAY_MISMATCH = new ErrorCode(1_050_007_002, "Yaya 会员订单支付信息不匹配");

}
