package cn.iocoder.yudao.module.ai;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * AI 错误码枚举类
 *
 * ai 系统，使用 1-040-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== API 密钥 1-040-000-000 ==========
    ErrorCode API_KEY_NOT_EXISTS = new ErrorCode(1_040_000_000, "API 密钥不存在");
    ErrorCode API_KEY_DISABLE = new ErrorCode(1_040_000_001, "API 密钥已禁用！");

    // ========== API 聊天模型 1-040-001-000 ==========
    ErrorCode CHAT_MODEL_NOT_EXISTS = new ErrorCode(1_040_001_000, "模型不存在!");
    ErrorCode CHAT_MODEL_DISABLE = new ErrorCode(1_040_001_001, "模型({})已禁用!");
    ErrorCode CHAT_MODEL_DEFAULT_NOT_EXISTS = new ErrorCode(1_040_001_002, "操作失败，找不到默认聊天模型");

    // ========== API 聊天模型 1-040-002-000 ==========
    ErrorCode CHAT_ROLE_NOT_EXISTS = new ErrorCode(1_040_002_000, "聊天角色不存在");
    ErrorCode CHAT_ROLE_DISABLE = new ErrorCode(1_040_001_001, "聊天角色({})已禁用!");
    ErrorCode CHAT_ROLE_DEFAULT_NOT_EXISTS = new ErrorCode(1_040_001_002, "操作失败，找不到默认聊天角色");

    // ========== API 聊天会话 1-040-003-000 ==========

    ErrorCode CHAT_CONVERSATION_NOT_EXISTS = new ErrorCode(1_040_003_000, "对话不存在!");
    ErrorCode CHAT_CONVERSATION_MODEL_ERROR = new ErrorCode(1_040_003_001, "操作失败，该聊天模型的配置不完整");
    ErrorCode CHAT_CONVERSATION_UPDATE_MAX_TOKENS_ERROR = new ErrorCode(1_040_003_002, "更新对话失败，最大 Token 超过上限");
    ErrorCode CHAT_CONVERSATION_UPDATE_MAX_CONTEXTS_ERROR = new ErrorCode(1_040_003_002, "更新对话失败，最大 Context 超过上限");

    // ========== API 聊天消息 1-040-004-000 ==========
    ErrorCode AI_CHAT_MESSAGE_NOT_EXIST = new ErrorCode(1_040_004_000, "消息不存在!");
    ErrorCode AI_CHAT_STREAM_ERROR = new ErrorCode(1_040_004_001, "Stream 对话异常!");

    // midjourney

    ErrorCode AI_MIDJOURNEY_IMAGINE_FAIL = new ErrorCode(1_022_000_040, "midjourney imagine 操作失败!");
    ErrorCode AI_MIDJOURNEY_OPERATION_NOT_EXISTS = new ErrorCode(1_022_000_040, "midjourney 操作不存在!");
    ErrorCode AI_MIDJOURNEY_MESSAGE_ID_INCORRECT = new ErrorCode(1_022_000_040, "midjourney message id 不正确!");

    // ========== API 绘画 1-040-005-000 ==========

    ErrorCode AI_IMAGE_NOT_CREATE_USER = new ErrorCode(1_022_005_000, "不是创建用户，不能删除 image!");
}
