package cn.iocoder.yudao.module.ai;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 模块 ai 错误码区间 [1-022-000-000 ~ 1-023-000-000) ==========

    // chat

    ErrorCode AI_MODULE_NOT_SUPPORTED = new ErrorCode(1_022_000_000, "AI 模型暂不支持!");

    // conversation

    ErrorCode AI_CONVERSATION_NOT_EXISTS = new ErrorCode(1_022_000_002, "AI 对话不存在!");;

    // midjourney

    ErrorCode AI_MIDJOURNEY_IMAGINE_FAIL = new ErrorCode(1_022_000_040, "midjourney imagine 操作失败!");
    ErrorCode AI_MIDJOURNEY_OPERATION_NOT_EXISTS = new ErrorCode(1_022_000_040, "midjourney 操作不存在!");
    ErrorCode AI_MIDJOURNEY_MESSAGE_ID_INCORRECT = new ErrorCode(1_022_000_040, "midjourney message id 不正确!");

    // role

    ErrorCode AI_CHAT_ROLE_NOT_EXIST = new ErrorCode(1_022_000_060, "AI 角色不存在!");
    ErrorCode AI_CHAT_ROLE_NOT_PUBLIC = new ErrorCode(1_022_000_060, "AI 角色未公开!");

    // modal

    ErrorCode AI_MODAL_NOT_EXIST = new ErrorCode(1_022_000_080, "AI 模型不存在!");
    ErrorCode AI_MODAL_CONFIG_PARAMS_INCORRECT = new ErrorCode(1_022_000_081, "AI 模型 config 参数不正确! {} ");
    ErrorCode AI_MODAL_PLATFORM_PARAMS_INCORRECT = new ErrorCode(1_022_000_083, "AI 平台参数不正确! {} ");
    ErrorCode AI_MODAL_DISABLE_NOT_USED = new ErrorCode(1_022_000_084, "AI 模型禁用不能使用!");

}
