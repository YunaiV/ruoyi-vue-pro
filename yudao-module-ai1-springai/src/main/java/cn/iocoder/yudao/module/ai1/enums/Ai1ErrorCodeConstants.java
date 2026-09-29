package cn.iocoder.yudao.module.ai1.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * AI1 错误码枚举类
 * <p>
 * ai1 系统，使用 1-041-000-000 段
 */
public interface Ai1ErrorCodeConstants {

    // ========== 供应商 1-041-000-000 ==========
    ErrorCode PROVIDER_NOT_EXISTS = new ErrorCode(1_041_000_000, "供应商不存在");
    ErrorCode PROVIDER_DISABLE = new ErrorCode(1_041_000_001, "供应商({}) 已禁用");
    ErrorCode PROVIDER_HAS_MODEL = new ErrorCode(1_041_000_003, "供应商下存在模型，禁止删除");
    ErrorCode PROVIDER_REMOTE_MODEL_LOAD_FAIL = new ErrorCode(1_041_000_004, "模型拉取失败：请检查供应商接口地址与 API 密钥");
    ErrorCode PROVIDER_REMOTE_MODEL_EMPTY = new ErrorCode(1_041_000_005, "远程未返回可用模型");
    ErrorCode CONFIG_PLACEHOLDER_NOT_RESOLVED = new ErrorCode(1_041_000_006, "配置({})无法解析，请检查环境变量或配置项");

    // ========== 模型 1-041-001-000 ==========
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(1_041_001_000, "模型不存在");
    ErrorCode MODEL_DISABLE = new ErrorCode(1_041_001_001, "模型({}) 已禁用");
    ErrorCode MODEL_DUPLICATE = new ErrorCode(1_041_001_002, "模型标识({}) 已存在");
    ErrorCode MODEL_USED_BY_AGENT = new ErrorCode(1_041_001_003, "模型已被 Agent 使用，禁止删除");
    ErrorCode MODEL_NOT_BELONG_PROVIDER = new ErrorCode(1_041_001_004, "模型不属于该供应商");
    ErrorCode MODEL_TYPE_NOT_CHAT = new ErrorCode(1_041_001_005, "所选模型不是对话模型");
    ErrorCode MODEL_TYPE_NOT_EMBEDDING = new ErrorCode(1_041_001_006, "所选模型不是嵌入模型");
    ErrorCode MODEL_IMPORT_ALL_EXISTS = new ErrorCode(1_041_001_007, "所选模型均已导入，无需重复导入");

    // ========== 知识库 1-041-002-000 ==========
    ErrorCode KNOWLEDGE_BASE_NOT_EXISTS = new ErrorCode(1_041_002_000, "知识库不存在");
    ErrorCode KNOWLEDGE_BASE_DOCUMENT_EMPTY = new ErrorCode(1_041_002_001, "知识库下暂无文档");
    ErrorCode KNOWLEDGE_BASE_SEARCH_FAIL = new ErrorCode(1_041_002_002, "向量检索失败：{}");

    // ========== 知识文档 1-041-003-000 ==========
    ErrorCode KNOWLEDGE_DOCUMENT_NOT_EXISTS = new ErrorCode(1_041_003_000, "文档不存在");
    ErrorCode KNOWLEDGE_DOCUMENT_CONTENT_EMPTY = new ErrorCode(1_041_003_001, "文档内容为空，无法向量化");
    ErrorCode KNOWLEDGE_DOCUMENT_FILE_TYPE_INVALID = new ErrorCode(1_041_003_002, "仅支持 .txt / .md 文本文件");
    ErrorCode KNOWLEDGE_DOCUMENT_FILE_EMPTY = new ErrorCode(1_041_003_003, "文件内容为空");
    ErrorCode KNOWLEDGE_DOCUMENT_FILE_READ_FAIL = new ErrorCode(1_041_003_004, "文件读取失败：{}");
    ErrorCode KNOWLEDGE_DOCUMENT_VECTORIZE_FAIL = new ErrorCode(1_041_003_005, "向量化失败：{}");

    // ========== MCP 1-041-004-000 ==========
    ErrorCode MCP_NOT_EXISTS = new ErrorCode(1_041_004_000, "MCP 不存在");
    ErrorCode MCP_CONFIG_INVALID = new ErrorCode(1_041_004_001, "config 配置需为合法的 JSON 对象");
    ErrorCode MCP_COMMAND_REQUIRED = new ErrorCode(1_041_004_002, "本地（stdio）类型必须在 config 中配置 command 命令");
    ErrorCode MCP_URL_REQUIRED = new ErrorCode(1_041_004_003, "远程（http）类型必须配置服务地址");

    // ========== SKILL 1-041-005-000 ==========
    ErrorCode SKILL_NOT_EXISTS = new ErrorCode(1_041_005_000, "SKILL 不存在");
    ErrorCode SKILL_NAME_DUPLICATE = new ErrorCode(1_041_005_002, "SKILL 名称({}) 已存在");

    // ========== SKILL 内容文件 1-041-006-000 ==========
    ErrorCode SKILL_FILE_NOT_EXISTS = new ErrorCode(1_041_006_000, "文件或目录不存在");
    ErrorCode SKILL_FILE_NAME_INVALID = new ErrorCode(1_041_006_001, "名称格式不合法（不能包含路径分隔符、控制字符，且不能为 . 或 ..）");
    ErrorCode SKILL_FILE_NAME_DUPLICATE = new ErrorCode(1_041_006_002, "同级下已存在同名节点({})");
    ErrorCode SKILL_FILE_PARENT_INVALID = new ErrorCode(1_041_006_003, "父节点必须是同一 SKILL 下的目录");
    ErrorCode SKILL_FILE_LOCKED = new ErrorCode(1_041_006_004, "固定文件或目录禁止{}");
    ErrorCode SKILL_FILE_MOVE_TO_SELF = new ErrorCode(1_041_006_005, "不能移入自身或其子目录");
    ErrorCode SKILL_FILE_NOT_FILE = new ErrorCode(1_041_006_006, "目录不支持保存内容");

    // ========== Agent 1-041-007-000 ==========
    ErrorCode AGENT_NOT_EXISTS = new ErrorCode(1_041_007_000, "Agent 不存在");
    ErrorCode AGENT_DISABLE = new ErrorCode(1_041_007_001, "Agent({}) 已关闭");

    // ========== 对话 1-041-008-000 ==========
    ErrorCode CHAT_CONVERSATION_NOT_EXISTS = new ErrorCode(1_041_008_000, "对话不存在");

    // ========== 消息 1-041-009-000 ==========
    ErrorCode CHAT_MESSAGE_NOT_EXISTS = new ErrorCode(1_041_009_000, "消息不存在");

}
