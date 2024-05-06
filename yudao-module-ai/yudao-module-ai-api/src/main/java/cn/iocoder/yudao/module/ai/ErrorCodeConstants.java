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
    ErrorCode AI_CHAT_ROLE_NOT_EXISTENT = new ErrorCode(1_022_000_001, "AI Role 不存在!");;
    ErrorCode AI_CHAT_CONTINUE_CONVERSATION_ID_NOT_NULL = new ErrorCode(1_022_000_002, "chat 继续对话，对话 id 不能为空!");;
    ErrorCode AI_CHAT_CONTINUE_NOT_EXIST = new ErrorCode(1_022_000_020, "chat 对话不存在!");
    ErrorCode AI_CHAT_CONVERSATION_NOT_YOURS = new ErrorCode(1_022_000_021, "这条 chat 对话不是你的!");

    // midjourney

    ErrorCode AI_MIDJOURNEY_IMAGINE_FAIL = new ErrorCode(1_022_000_040, "midjourney imagine 操作失败!");

    // role

    ErrorCode AI_CHAT_ROLE_NOT_EXIST = new ErrorCode(1_022_000_060, "chatRole 不存在!");

    // modal

    ErrorCode AI_MODAL_NOT_EXIST = new ErrorCode(1_022_000_080, "AI 模型不存在!");
    ErrorCode AI_MODAL_CONFIG_PARAMS_INCORRECT = new ErrorCode(1_022_000_081, "AI 模型 config 参数不正确! {} ");
    ErrorCode AI_MODAL_NOT_SUPPORTED_MODAL = new ErrorCode(1_022_000_082, "AI 模型不支持的 modal! {} ");
    ErrorCode AI_MODAL_PLATFORM_PARAMS_INCORRECT = new ErrorCode(1_022_000_083, "AI 平台参数不正确! {} ");

}
