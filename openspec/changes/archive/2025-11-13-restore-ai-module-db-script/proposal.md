# 修复 AI 模块数据库初始化脚本

## Why

现有的 AI 模块数据库初始化脚本 (`yudao-module-ai/sql/ai-2025-03-15.sql`) 已经过时,缺少工作流表 (`ai_workflow`),并且存在以下问题:

1. **缺失表定义**:代码中存在 `AiWorkflowDO` 实体类,但数据库脚本中缺少对应的 `ai_workflow` 表定义
2. **字段缺失**:多个实体类新增了字段,但脚本未同步更新(如 `ai_chat_message` 表缺少 `reasoning_content`、`web_search_pages`、`attachment_urls` 等字段)
3. **KeySequence 命名错误**:代码中部分实体类的 KeySequence 注解命名不一致(如 `AiApiKeyDO` 使用了 `ai_chat_conversation_seq`)

数据库脚本与代码不一致会导致:
- 新部署的系统无法正常运行
- 数据库迁移失败
- 功能异常或报错

## What Changes

### 新增内容
- 添加 `ai_workflow` 表的完整定义及索引
- 补充 `ai_chat_message` 表的缺失字段:`reasoning_content`、`web_search_pages`、`attachment_urls`
- 补充 `ai_chat_role` 表的缺失字段:`knowledge_ids`、`tool_ids`、`mcp_client_names`
- 补充 `ai_music` 表的缺失字段:`tags`、`duration`

### 修正内容
- 修正字段类型不匹配问题
- 修正字段注释不准确问题
- 统一所有表的审计字段格式(creator、create_time、updater、update_time、deleted、tenant_id)

### 优化内容
- 清理过时的测试数据(仅保留必要的初始化数据)
- 按照表的依赖关系重新排序建表语句
- 添加必要的索引以提升查询性能

## Impact

**影响的文件**:
- `yudao-module-ai/sql/ai-2025-03-15.sql` - 主要修改文件

**影响的功能模块**:
- AI 工作流管理
- AI 聊天对话
- AI 角色管理
- AI 音乐生成
- 所有 AI 模块的数据持久化

**向后兼容性**:
- ✅ **完全兼容**:仅添加缺失的表和字段,不删除或修改现有结构
- ✅ **安全升级**:现有数据不受影响,可通过 ALTER TABLE 增量升级

**数据库支持**:
- MySQL 5.7 / 8.0+ (主要支持)
- Oracle、PostgreSQL、SQL Server 等(需要对应的 KeySequence 支持)

## 相关代码

实体类文件路径:
- `yudao-module-ai/src/main/java/cn/iocoder/yudao/module/ai/dal/dataobject/**/*.java`

主要涉及的实体类:
- `AiWorkflowDO` - 新增表定义
- `AiChatMessageDO` - 字段补充
- `AiChatRoleDO` - 字段补充
- `AiMusicDO` - 字段补充
