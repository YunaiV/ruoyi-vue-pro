package cn.iocoder.yudao.module.ai;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * AI 错误码枚举类
 *
 * ai 系统，使用 1-040-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== API 密钥 1-040-000-000 ==========
    ErrorCode API_KEY_NOT_EXISTS = new ErrorCode(1_040_000_000, "AI API 密钥不存在");
    ErrorCode API_KEY_DISABLE = new ErrorCode(1_040_000_001, "AI API 密钥已禁用！");

    // ========== API 聊天模型 1-040-001-000 ==========

    ErrorCode CHAT_MODAL_NOT_EXIST = new ErrorCode(1_040_001_000, "AI 模型不存在!");
    ErrorCode CHAT_MODAL_DISABLE = new ErrorCode(1_040_001_001, "AI 模型({})已禁用!");

    // ========== API 聊天模型 1-040-002-000 ==========
    ErrorCode CHAT_ROLE_NOT_EXISTS = new ErrorCode(1_040_002_000, "AI 聊天角色不存在");
    ErrorCode CHAT_ROLE_DISABLE = new ErrorCode(1_040_001_001, "AI 聊天角色({})已禁用!");

    // conversation

    ErrorCode AI_CONVERSATION_NOT_EXISTS = new ErrorCode(1_022_000_002, "AI 对话不存在!");;

    // midjourney

    ErrorCode AI_MIDJOURNEY_IMAGINE_FAIL = new ErrorCode(1_022_000_040, "midjourney imagine 操作失败!");
    ErrorCode AI_MIDJOURNEY_OPERATION_NOT_EXISTS = new ErrorCode(1_022_000_040, "midjourney 操作不存在!");
    ErrorCode AI_MIDJOURNEY_MESSAGE_ID_INCORRECT = new ErrorCode(1_022_000_040, "midjourney message id 不正确!");

    // chat
    ErrorCode AI_CHAT_MESSAGE_NOT_EXIST = new ErrorCode(1_022_000_100, "AI 提问的 MessageId 不存在!");

}
