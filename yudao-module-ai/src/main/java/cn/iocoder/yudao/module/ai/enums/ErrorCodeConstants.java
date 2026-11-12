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
    ErrorCode API_CONFIG_PLACEHOLDER_NOT_RESOLVED = new ErrorCode(1_040_000_002, "AI 配置({})无法解析，请检查环境变量或配置项");

    // ========== API 模型 1-040-001-000 ==========
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(1_040_001_000, "模型不存在!");
    ErrorCode MODEL_DISABLE = new ErrorCode(1_040_001_001, "模型({})已禁用!");
    ErrorCode MODEL_DEFAULT_NOT_EXISTS = new ErrorCode(1_040_001_002, "操作失败，找不到默认模型");
    ErrorCode MODEL_USE_TYPE_ERROR = new ErrorCode(1_040_001_003, "操作失败，该模型的模型类型不正确");
    ErrorCode MODEL_REASONING_NOT_CONFIGURED = new ErrorCode(1_040_001_004, "该模型未配置思考档位，请刷新页面后重试");
    ErrorCode MODEL_REASONING_LEVEL_NOT_SUPPORTED = new ErrorCode(1_040_001_005, "该模型不支持思考档位({})，请刷新页面后重试");
    ErrorCode MODEL_REASONING_PLATFORM_NOT_SUPPORTED = new ErrorCode(1_040_001_006, "平台({})暂不支持思考配置");
    ErrorCode MODEL_REASONING_PLATFORM_MISMATCH = new ErrorCode(1_040_001_007, "模型平台({})与 API 密钥平台({})不一致，无法使用思考配置");
    ErrorCode MODEL_REASONING_CONFIG_INVALID = new ErrorCode(1_040_001_009, "思考配置不正确：{}");

    // ========== API 聊天角色 1-040-002-000 ==========
    ErrorCode CHAT_ROLE_NOT_EXISTS = new ErrorCode(1_040_002_000, "聊天角色不存在");
    ErrorCode CHAT_ROLE_DISABLE = new ErrorCode(1_040_001_001, "聊天角色({})已禁用!");

    // ========== API 聊天会话 1-040-003-000 ==========
    ErrorCode CHAT_CONVERSATION_NOT_EXISTS = new ErrorCode(1_040_003_000, "对话不存在!");
    ErrorCode CHAT_CONVERSATION_MODEL_ERROR = new ErrorCode(1_040_003_001, "操作失败，该聊天模型的配置不完整");

    // ========== API 聊天消息 1-040-004-000 ==========
    ErrorCode CHAT_MESSAGE_NOT_EXIST = new ErrorCode(1_040_004_000, "消息不存在!");
    ErrorCode CHAT_STREAM_ERROR = new ErrorCode(1_040_004_001, "对话生成异常!");
    ErrorCode CHAT_MESSAGE_IMAGE_READ_FAIL = new ErrorCode(1_040_004_002, "读取图片附件({})失败，请重新上传后重试");

    // ========== API 绘画 1-040-005-000 ==========
    ErrorCode IMAGE_NOT_EXISTS = new ErrorCode(1_040_005_000, "图片不存在!");
    ErrorCode IMAGE_MIDJOURNEY_SUBMIT_FAIL = new ErrorCode(1_040_005_001, "Midjourney 提交失败!原因：{}");
    ErrorCode IMAGE_CUSTOM_ID_NOT_EXISTS = new ErrorCode(1_040_005_002, "Midjourney 按钮 customId 不存在! {}");
    ErrorCode IMAGE_MODEL_PLATFORM_MISMATCH = new ErrorCode(1_040_005_003,
            "操作失败，绘图模型的平台（{}）与 API 密钥的平台（{}）不一致，请修改模型配置");
    ErrorCode IMAGE_MODEL_NOT_SUPPORTED = new ErrorCode(1_040_005_004, "操作失败，绘图模型({})不可用：{}");
    ErrorCode IMAGE_OPTION_INVALID = new ErrorCode(1_040_005_005, "参数({})不合法：{}");
    ErrorCode IMAGE_REFERENCE_COUNT_EXCEED = new ErrorCode(1_040_005_006, "图片数量不正确：需要 {} 张，当前 {} 张");
    ErrorCode IMAGE_PUBLIC_NOT_EXISTS = new ErrorCode(1_040_005_007, "作品不存在或已取消公开");
    ErrorCode IMAGE_PUBLIC_STATUS_FAIL = new ErrorCode(1_040_005_008, "只有已完成的作品才能公开");
    ErrorCode IMAGE_MIDJOURNEY_ACTION_NOT_SUPPORTED = new ErrorCode(1_040_005_009, "Midjourney 按钮({})暂不支持");
    ErrorCode IMAGE_MIDJOURNEY_SOURCE_INVALID = new ErrorCode(1_040_005_010, "来源作品还未完成，不能执行该操作");
    ErrorCode IMAGE_OPTIMIZE_PROMPT_FAIL = new ErrorCode(1_040_005_011, "生成专业绘画指令失败，请稍后重试");

    // ========== API 音乐 1-040-006-000 ==========
    ErrorCode MUSIC_NOT_EXISTS = new ErrorCode(1_040_006_000, "音乐不存在!");
    ErrorCode MUSIC_MODEL_TYPE_ERROR = new ErrorCode(1_040_006_001, "操作失败，所选模型不是 Suno 音乐模型");
    ErrorCode MUSIC_REF_NOT_AVAILABLE = new ErrorCode(1_040_006_002, "来源音乐不存在、未完成或不属于你，无法继续创作");
    ErrorCode MUSIC_REF_NOT_EXTEND = new ErrorCode(1_040_006_003, "只有续写作品才能合并为完整歌曲");
    ErrorCode MUSIC_EXTEND_SECONDS_EXCEED = new ErrorCode(1_040_006_004, "续写起点不能超过原曲时长");
    ErrorCode MUSIC_REF_API_KEY_UNAVAILABLE = new ErrorCode(1_040_006_005, "原曲所在渠道不可用，无法续写或合并");
    ErrorCode MUSIC_STATUS_NOT_FINISHED = new ErrorCode(1_040_006_006, "音乐还在生成中，暂不能执行该操作");
    ErrorCode MUSIC_STATUS_NOT_SUCCESS = new ErrorCode(1_040_006_007, "只有已完成的音乐才能执行该操作");
    ErrorCode MUSIC_LYRIC_GENERATE_FAIL = new ErrorCode(1_040_006_009, "歌词生成失败，请稍后重试");
    ErrorCode MUSIC_UPLOAD_FILE_INVALID = new ErrorCode(1_040_006_010, "只支持 mp3、wav 格式的音频，请重新上传");

    // ========== API 写作 1-040-007-000 ==========
    ErrorCode WRITE_NOT_EXISTS = new ErrorCode(1_040_007_000, "作文不存在!");
    ErrorCode WRITE_STREAM_ERROR = new ErrorCode(1_040_07_001, "写作生成异常!");

    // ========== API 思维导图 1-040-008-000 ==========
    ErrorCode MIND_MAP_NOT_EXISTS = new ErrorCode(1_040_008_000, "思维导图不存在!");

    // ========== API 知识库 1-040-009-000 ==========
    ErrorCode KNOWLEDGE_NOT_EXISTS = new ErrorCode(1_040_009_000, "知识库不存在!");

    ErrorCode KNOWLEDGE_DOCUMENT_NOT_EXISTS = new ErrorCode(1_040_009_101, "文档不存在!");
    ErrorCode KNOWLEDGE_DOCUMENT_FILE_EMPTY = new ErrorCode(1_040_009_102, "文档内容为空!");
    ErrorCode KNOWLEDGE_DOCUMENT_FILE_DOWNLOAD_FAIL = new ErrorCode(1_040_009_102, "文件下载失败!");
    ErrorCode KNOWLEDGE_DOCUMENT_FILE_READ_FAIL = new ErrorCode(1_040_009_102, "文档加载失败!");

    ErrorCode KNOWLEDGE_SEGMENT_NOT_EXISTS = new ErrorCode(1_040_009_202, "段落不存在!");
    ErrorCode KNOWLEDGE_SEGMENT_CONTENT_TOO_LONG = new ErrorCode(1_040_009_203, "内容 Token 数为 {}，超过最大限制 {}");

    // ========== AI 工具 1-040-010-000 ==========
    ErrorCode TOOL_NOT_EXISTS = new ErrorCode(1_040_010_000, "工具不存在");
    ErrorCode TOOL_NAME_NOT_EXISTS = new ErrorCode(1_040_010_001, "工具({})找不到 Bean");

    // ========== AI 工作流 1-040-011-000 ==========
    ErrorCode WORKFLOW_NOT_EXISTS = new ErrorCode(1_040_011_000, "工作流不存在");
    ErrorCode WORKFLOW_CODE_EXISTS = new ErrorCode(1_040_011_001, "工作流标识已存在");

    // ========== AI PPT 1-040-012-000 ==========
    ErrorCode PPT_NOT_EXISTS = new ErrorCode(1_040_012_000, "PPT 不存在");
    ErrorCode PPT_IMAGE_MODEL_NOT_SUPPORTED = new ErrorCode(1_040_012_001, "操作失败，PPT 只支持 OpenAI、硅基流动、通义的绘图模型");
    ErrorCode PPT_STATUS_IN_PROGRESS = new ErrorCode(1_040_012_006, "PPT 生成中，请稍后再试");
    ErrorCode PPT_STATUS_NOT_FAIL = new ErrorCode(1_040_012_007, "只有已失败的 PPT 才能继续生成");
    ErrorCode PPT_STATUS_NOT_SUCCESS = new ErrorCode(1_040_012_008, "只有已完成的 PPT 才能导出");
    ErrorCode PPT_SLIDE_NOT_EXISTS = new ErrorCode(1_040_012_009, "第 {} 页不存在");
    ErrorCode PPT_SLIDE_IMAGE_NOT_EXISTS = new ErrorCode(1_040_012_010, "第 {} 页还没有配图，请先继续生成");
    ErrorCode PPT_SLIDE_VERSION_NOT_EXISTS = new ErrorCode(1_040_012_011, "第 {} 页没有该版本，请刷新后重试");
    ErrorCode PPT_EXPORT_FORMAT_INVALID = new ErrorCode(1_040_012_012, "只支持导出 pptx、pdf 格式");
    ErrorCode PPT_EXPORT_FAIL = new ErrorCode(1_040_012_013, "导出失败：{}");
    ErrorCode PPT_IMAGE_MODEL_PLATFORM_MISMATCH = new ErrorCode(1_040_012_014,
            "操作失败，绘图模型的平台（{}）与 API 密钥的平台（{}）不一致，请修改模型配置");
    ErrorCode PPT_UPDATE_CONFLICT = new ErrorCode(1_040_012_015, "PPT 已被修改，请刷新后重试");

}
