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
    ErrorCode WRITE_GENERATE_FAIL = new ErrorCode(1_040_007_002, "写作生成失败：{}");

    // ========== API 思维导图 1-040-008-000 ==========
    ErrorCode MIND_MAP_NOT_EXISTS = new ErrorCode(1_040_008_000, "思维导图不存在!");
    ErrorCode MIND_MAP_GENERATE_FAIL = new ErrorCode(1_040_008_001, "思维导图生成失败：{}");

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
    ErrorCode PPT_IMAGE_MODEL_NOT_SUPPORTED = new ErrorCode(1_040_012_001, "操作失败，PPT 只支持 OpenAI、硅基流动、阿里巴巴的绘图模型");
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

    // ========== AI 语音 1-040-013-000 ==========
    ErrorCode SPEECH_NOT_EXISTS = new ErrorCode(1_040_013_000, "语音不存在");
    ErrorCode SPEECH_MODEL_PLATFORM_MISMATCH = new ErrorCode(1_040_013_001,
            "操作失败，语音模型的平台（{}）与 API 密钥的平台（{}）不一致，请修改模型配置");
    ErrorCode SPEECH_MODEL_NOT_SUPPORTED = new ErrorCode(1_040_013_002, "操作失败，语音模型({})不可用：{}");
    ErrorCode SPEECH_OPTION_INVALID = new ErrorCode(1_040_013_003, "语音参数不正确：{}");
    ErrorCode SPEECH_STATUS_NOT_FINISHED = new ErrorCode(1_040_013_004, "语音还在生成中，暂不能删除");

    // ========== AI 文字识别 1-040-014-000 ==========
    ErrorCode OCR_NOT_EXISTS = new ErrorCode(1_040_014_000, "文字识别记录不存在");
    ErrorCode OCR_MODEL_PLATFORM_MISMATCH = new ErrorCode(1_040_014_001,
            "操作失败，文字识别模型的平台（{}）与 API 密钥的平台（{}）不一致，请修改模型配置");
    ErrorCode OCR_MODEL_PLATFORM_NOT_SUPPORTED = new ErrorCode(1_040_014_002, "平台({})暂不支持文字识别");
    ErrorCode OCR_CONFIG_INVALID = new ErrorCode(1_040_014_005, "文字识别配置不正确：{}");

    // ========== AI 技能 1-040-015-000 ==========
    ErrorCode SKILL_NOT_EXISTS = new ErrorCode(1_040_015_000, "技能不存在");
    ErrorCode SKILL_NAME_DUPLICATE = new ErrorCode(1_040_015_001, "技能名称({})已存在");
    ErrorCode SKILL_DISABLE = new ErrorCode(1_040_015_002, "技能({})已停用，不能绑定");
    ErrorCode SKILL_FILE_NOT_EXISTS = new ErrorCode(1_040_015_100, "技能文件不存在");
    ErrorCode SKILL_FILE_NAME_INVALID = new ErrorCode(1_040_015_101, "名称不合法：不能为空、不能为 . 或 ..，不能包含 / \\ 和控制字符，长度不超过 {} 个字符");
    ErrorCode SKILL_FILE_NAME_DUPLICATE = new ErrorCode(1_040_015_102, "同一目录下已存在名称({})");
    ErrorCode SKILL_FILE_LOCKED = new ErrorCode(1_040_015_103, "固定文件或目录不能{}");
    ErrorCode SKILL_FILE_PARENT_NOT_EXISTS = new ErrorCode(1_040_015_104, "父目录不存在");
    ErrorCode SKILL_FILE_PARENT_INVALID = new ErrorCode(1_040_015_105, "父目录必须是同一技能下的目录");
    ErrorCode SKILL_FILE_MOVE_TO_SELF = new ErrorCode(1_040_015_106, "不能移动到自身");
    ErrorCode SKILL_FILE_MOVE_TO_DESCENDANT = new ErrorCode(1_040_015_107, "不能移动到自身的子目录");
    ErrorCode SKILL_FILE_NOT_FILE = new ErrorCode(1_040_015_108, "目录不能保存内容");
    ErrorCode SKILL_FILE_TYPE_NOT_SUPPORTED = new ErrorCode(1_040_015_109, "文件({})类型不支持，只允许 {} 文本文件");
    ErrorCode SKILL_FILE_SIZE_EXCEED = new ErrorCode(1_040_015_110, "单个文件不能超过 {} KB，当前 {} KB");
    ErrorCode SKILL_FILE_TOTAL_SIZE_EXCEED = new ErrorCode(1_040_015_111, "技能文件合计不能超过 {} MB，保存后为 {} KB");
    ErrorCode SKILL_FILE_SKILL_MD_INVALID = new ErrorCode(1_040_015_112, "SKILL.md 头信息不正确：{}");

    // ========== AI 语音识别 1-040-016-000 ==========
    ErrorCode ASR_MODEL_PLATFORM_MISMATCH = new ErrorCode(1_040_016_000,
            "操作失败，语音识别模型的平台（{}）与 API 密钥的平台（{}）不一致，请修改模型配置");
    ErrorCode ASR_MODEL_NOT_SUPPORTED = new ErrorCode(1_040_016_001, "操作失败，语音识别模型({})不可用：{}");
    ErrorCode ASR_NOT_EXISTS = new ErrorCode(1_040_016_002, "语音识别记录不存在");

    // ========== AI 视频 1-040-017-000 ==========
    ErrorCode VIDEO_NOT_EXISTS = new ErrorCode(1_040_017_000, "视频不存在");
    ErrorCode VIDEO_MODEL_TYPE_ERROR = new ErrorCode(1_040_017_001, "操作失败，所选模型不是视频模型");
    ErrorCode VIDEO_MODEL_PLATFORM_MISMATCH = new ErrorCode(1_040_017_002,
            "操作失败，视频模型的平台（{}）与 API 密钥的平台（{}）不一致，请修改模型配置");
    ErrorCode VIDEO_PARAMS_INVALID = new ErrorCode(1_040_017_003, "视频参数不正确：{}");
    ErrorCode VIDEO_STATUS_NOT_FINISHED = new ErrorCode(1_040_017_004, "视频还在生成中，暂不能执行该操作");
    ErrorCode VIDEO_STATUS_NOT_SUCCESS = new ErrorCode(1_040_017_005, "只有已完成的视频才能执行该操作");

    // ========== AI 短剧 1-040-018-000 ==========
    ErrorCode DRAMA_PROJECT_NOT_EXISTS = new ErrorCode(1_040_018_000, "短剧项目不存在");
    ErrorCode DRAMA_PROJECT_PUBLIC_NOT_ALLOWED = new ErrorCode(1_040_018_001, "只有成片已完成的项目才能公开");
    ErrorCode DRAMA_SCRIPT_NOT_EXISTS = new ErrorCode(1_040_018_002, "剧本不存在，请先生成或填写剧本");
    ErrorCode DRAMA_CHAT_FAIL = new ErrorCode(1_040_018_003, "{}失败：{}");
    ErrorCode DRAMA_CHARACTER_NOT_EXISTS = new ErrorCode(1_040_018_004, "角色不存在");
    ErrorCode DRAMA_APPEARANCE_NOT_EXISTS = new ErrorCode(1_040_018_005, "角色形象不存在");
    ErrorCode DRAMA_LOCATION_NOT_EXISTS = new ErrorCode(1_040_018_006, "场景不存在");
    ErrorCode DRAMA_VISUAL_ASSET_NOT_EXISTS = new ErrorCode(1_040_018_007, "视觉资产不存在");
    ErrorCode DRAMA_AUDIO_NOT_EXISTS = new ErrorCode(1_040_018_008, "音频不存在");
    ErrorCode DRAMA_STORYBOARD_NOT_EXISTS = new ErrorCode(1_040_018_009, "分镜不存在");
    ErrorCode DRAMA_IMAGE_MODEL_ERROR = new ErrorCode(1_040_018_010, "操作失败，所选模型不是可用的绘画模型：{}");
    ErrorCode DRAMA_TASK_RUNNING = new ErrorCode(1_040_018_011, "正在生成中，请稍后再试");
    ErrorCode DRAMA_COMPOSE_STORYBOARD_NOT_SUCCESS = new ErrorCode(1_040_018_012, "镜头（{}）的视频未完成，不能合成");
    ErrorCode DRAMA_AUDIO_TYPE_ERROR = new ErrorCode(1_040_018_013, "音频类型不正确：{}");
    ErrorCode DRAMA_SPEECH_FAIL = new ErrorCode(1_040_018_014, "音频生成失败：{}");
    ErrorCode DRAMA_TASK_NOT_FAIL = new ErrorCode(1_040_018_015, "只有已失败的任务才能重新提交");
    ErrorCode DRAMA_TASK_NOT_RUNNING = new ErrorCode(1_040_018_016, "只有进行中的任务才能执行该操作");
    ErrorCode DRAMA_TASK_SYNC_NOT_SUPPORTED = new ErrorCode(1_040_018_017, "只有镜头视频支持同步状态");
    ErrorCode DRAMA_SELECTED_INDEX_INVALID = new ErrorCode(1_040_018_018, "选用的候选图不存在");
    ErrorCode DRAMA_TASK_RESUBMIT_NO_MODEL = new ErrorCode(1_040_018_019, "找不到上次使用的模型，请用户在工作台重新生成");
    ErrorCode DRAMA_TASK_SYNC_FAIL = new ErrorCode(1_040_018_020, "同步状态失败：{}");
    ErrorCode DRAMA_CHAT_RUNNING = new ErrorCode(1_040_018_021, "该项目正在{}，请等待完成后再试");
    ErrorCode DRAMA_VISUAL_ASSET_NO_PREVIOUS = new ErrorCode(1_040_018_022, "没有可撤销的上一张图片");
    ErrorCode DRAMA_PREVIOUS_STORYBOARD_NOT_EXISTS = new ErrorCode(1_040_018_023, "没有上一个镜头，或上一个镜头的视频未完成");
    ErrorCode DRAMA_FFMPEG_FAIL = new ErrorCode(1_040_018_024, "{}失败：{}");
    ErrorCode DRAMA_MATERIAL_EMPTY = new ErrorCode(1_040_018_025, "请先上传素材图片");
    ErrorCode DRAMA_FIRST_FRAME_EMPTY = new ErrorCode(1_040_018_026, "当前没有关键帧，不能修正构图");
    ErrorCode DRAMA_SCRIPT_CHANGED = new ErrorCode(1_040_018_027, "剧本已修改，请重新分析资产和生成分镜后再生成");
    ErrorCode DRAMA_CANVAS_REVISION_CONFLICT = new ErrorCode(1_040_018_028, "画布已在另一窗口更新，请重新加载后继续编辑");
    ErrorCode DRAMA_CANVAS_INVALID = new ErrorCode(1_040_018_029, "画布无效：{}");
    ErrorCode DRAMA_DIRECTOR_REVISION_CONFLICT = new ErrorCode(1_040_018_030, "导演工程已在另一窗口更新，请先导出备份，再重新加载");
    ErrorCode DRAMA_DIRECTOR_INVALID = new ErrorCode(1_040_018_031, "导演工程无效：{}");
    ErrorCode DRAMA_MOTION_REFERENCE_NOT_EXISTS = new ErrorCode(1_040_018_032, "动作参考不存在");
    ErrorCode DRAMA_MOTION_REFERENCE_INVALID = new ErrorCode(1_040_018_033, "动作参考视频不符合要求：{}");
    ErrorCode DRAMA_VIDEO_SUBMISSION_NOT_EXISTS = new ErrorCode(1_040_018_034, "视频提交记录不存在");
    ErrorCode DRAMA_VIDEO_SUBMISSION_PARAMS_CHANGED = new ErrorCode(1_040_018_035, "该请求编号已用于不同的视频参数，请重新提交");
    ErrorCode DRAMA_VIDEO_SUBMISSION_NOT_UNKNOWN = new ErrorCode(1_040_018_036, "只有提交结果未知、且仍是镜头当前提交的记录才能处理");
    ErrorCode DRAMA_VIDEO_SUBMISSION_NO_VIDEO = new ErrorCode(1_040_018_037, "该提交没有可使用的视频");
    ErrorCode DRAMA_MUSIC_NOT_READY = new ErrorCode(1_040_018_038, "AI 音乐不存在或未生成完成");
    ErrorCode DRAMA_STORYBOARD_EMPTY = new ErrorCode(1_040_018_039, "当前剧本还没有分镜，请先拆分分镜");
    ErrorCode DRAMA_CREATE_RUNNING = new ErrorCode(1_040_018_040, "初版剧本正在生成，请等待完成后再试");

}
