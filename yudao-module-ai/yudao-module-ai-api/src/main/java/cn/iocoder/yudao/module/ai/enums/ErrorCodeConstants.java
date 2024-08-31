package cn.iocoder.yudao.module.ai.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * AI 错误码枚举类
 * <p>
 * ai 系统，使用 1-040-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== API 密钥 1-040-000-000 ==========
    ErrorCode API_KEY_NOT_EXISTS = new ErrorCode(1_040_000_000, "API 密钥不存在");
    ErrorCode API_KEY_DISABLE = new ErrorCode(1_040_000_001, "API 密钥已禁用！");
    ErrorCode API_KEY_MIDJOURNEY_NOT_FOUND = new ErrorCode(1_040_000_900, "Midjourney 模型不存在");
    ErrorCode API_KEY_SUNO_NOT_FOUND = new ErrorCode(1_040_000_901, "Suno 模型不存在");
    ErrorCode API_KEY_IMAGE_NODE_FOUND = new ErrorCode(1_040_000_902, "平台({}) 图片模型未配置");

    // ========== API 聊天模型 1-040-001-000 ==========
    ErrorCode CHAT_MODEL_NOT_EXISTS = new ErrorCode(1_040_001_000, "模型不存在!");
    ErrorCode CHAT_MODEL_DISABLE = new ErrorCode(1_040_001_001, "模型({})已禁用!");
    ErrorCode CHAT_MODEL_DEFAULT_NOT_EXISTS = new ErrorCode(1_040_001_002, "操作失败，找不到默认聊天模型");

    // ========== API 聊天模型 1-040-002-000 ==========
    ErrorCode CHAT_ROLE_NOT_EXISTS = new ErrorCode(1_040_002_000, "聊天角色不存在");
    ErrorCode CHAT_ROLE_DISABLE = new ErrorCode(1_040_001_001, "聊天角色({})已禁用!");

    // ========== API 聊天会话 1-040-003-000 ==========

    ErrorCode CHAT_CONVERSATION_NOT_EXISTS = new ErrorCode(1_040_003_000, "对话不存在!");
    ErrorCode CHAT_CONVERSATION_MODEL_ERROR = new ErrorCode(1_040_003_001, "操作失败，该聊天模型的配置不完整");

    // ========== API 聊天消息 1-040-004-000 ==========

    ErrorCode CHAT_MESSAGE_NOT_EXIST = new ErrorCode(1_040_004_000, "消息不存在!");
    ErrorCode CHAT_STREAM_ERROR = new ErrorCode(1_040_004_001, "对话生成异常!");

    // ========== API 绘画 1-040-005-000 ==========

    ErrorCode IMAGE_NOT_EXISTS = new ErrorCode(1_022_005_000, "图片不存在!");
    ErrorCode IMAGE_MIDJOURNEY_SUBMIT_FAIL = new ErrorCode(1_022_005_001, "Midjourney 提交失败!原因：{}");
    ErrorCode IMAGE_CUSTOM_ID_NOT_EXISTS = new ErrorCode(1_022_005_002, "Midjourney 按钮 customId 不存在! {}");
    ErrorCode IMAGE_FAIL = new ErrorCode(1_022_005_002, "图片绘画失败! {}");

    // ========== API 音乐 1-040-006-000 ==========
    ErrorCode MUSIC_NOT_EXISTS = new ErrorCode(1_022_006_000, "音乐不存在!");

    // ========== API 写作 1-022-007-000 ==========
    ErrorCode WRITE_NOT_EXISTS = new ErrorCode(1_022_007_000, "作文不存在!");
    ErrorCode WRITE_STREAM_ERROR = new ErrorCode(1_022_07_001, "写作生成异常!");

    // ========== API 思维导图 1-040-008-000 ==========
    ErrorCode MIND_MAP_NOT_EXISTS = new ErrorCode(1_040_008_000, "思维导图不存在!");


    // ========== API 知识库 1-022-008-000 ==========
    ErrorCode KNOWLEDGE_NOT_EXISTS = new ErrorCode(1_022_008_000, "知识库不存在!");
    ErrorCode KNOWLEDGE_DOCUMENT_NOT_EXISTS = new ErrorCode(1_022_008_001, "文档不存在!");
    ErrorCode KNOWLEDGE_SEGMENT_NOT_EXISTS = new ErrorCode(1_022_008_002, "段落不存在!");

}
