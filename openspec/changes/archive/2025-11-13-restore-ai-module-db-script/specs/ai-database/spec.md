# AI 模块数据库规范

## ADDED Requirements

### Requirement: AI 工作流表定义
系统 SHALL 提供完整的 `ai_workflow` 表定义,用于存储 AI 工作流的配置信息。

#### Scenario: 创建工作流表
- **GIVEN** 数据库初始化脚本
- **WHEN** 执行建表语句
- **THEN** 成功创建 `ai_workflow` 表,包含以下字段:
  - `id` BIGINT 主键
  - `name` VARCHAR(100) 工作流名称
  - `code` VARCHAR(50) 工作流标识(唯一)
  - `graph` TEXT 工作流模型 JSON 数据
  - `remark` VARCHAR(500) 备注
  - `status` INT 状态(0-开启,1-关闭)
  - `creator` VARCHAR(64) 创建者
  - `create_time` DATETIME 创建时间
  - `updater` VARCHAR(64) 更新者
  - `update_time` DATETIME 更新时间
  - `deleted` BIT(1) 是否删除
  - `tenant_id` BIGINT 租户编号

#### Scenario: 工作流表索引
- **GIVEN** `ai_workflow` 表已创建
- **WHEN** 添加索引
- **THEN** 创建 `idx_code`(code) 唯一索引,提升按标识查询的性能
- **AND** 创建 `idx_tenant_id`(tenant_id) 索引,支持多租户查询

### Requirement: 聊天消息扩展字段
系统 SHALL 为 `ai_chat_message` 表添加扩展字段,支持推理内容、联网搜索和附件功能。

#### Scenario: 添加推理内容字段
- **GIVEN** `ai_chat_message` 表定义
- **WHEN** 添加 `reasoning_content` 字段
- **THEN** 字段类型为 TEXT
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "推理内容(用于存储模型的推理过程)"

#### Scenario: 添加联网搜索字段
- **GIVEN** `ai_chat_message` 表定义
- **WHEN** 添加 `web_search_pages` 字段
- **THEN** 字段类型为 VARCHAR(8192)
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "联网搜索的网页内容数组(JSON 格式)"
- **AND** 字段存储 WebPage 对象的 JSON 数组

#### Scenario: 添加附件URL字段
- **GIVEN** `ai_chat_message` 表定义
- **WHEN** 添加 `attachment_urls` 字段
- **THEN** 字段类型为 VARCHAR(2048)
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "附件 URL 数组(逗号分隔)"
- **AND** 支持存储多个文件 URL,以逗号分隔

### Requirement: 聊天角色知识库和工具集成
系统 SHALL 为 `ai_chat_role` 表添加字段,支持角色关联知识库、工具和 MCP 客户端。

#### Scenario: 添加知识库关联字段
- **GIVEN** `ai_chat_role` 表定义
- **WHEN** 添加 `knowledge_ids` 字段
- **THEN** 字段类型为 VARCHAR(1024)
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "引用的知识库编号列表(逗号分隔)"
- **AND** 支持存储多个知识库 ID

#### Scenario: 添加工具关联字段
- **GIVEN** `ai_chat_role` 表定义
- **WHEN** 添加 `tool_ids` 字段
- **THEN** 字段类型为 VARCHAR(1024)
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "引用的工具编号列表(逗号分隔)"
- **AND** 支持存储多个工具 ID

#### Scenario: 添加MCP客户端关联字段
- **GIVEN** `ai_chat_role` 表定义
- **WHEN** 添加 `mcp_client_names` 字段
- **THEN** 字段类型为 VARCHAR(1024)
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "引用的 MCP Client 名字列表(逗号分隔)"
- **AND** 支持存储多个 MCP 客户端名称

### Requirement: 音乐生成扩展属性
系统 SHALL 为 `ai_music` 表添加标签和时长字段,完善音乐元数据管理。

#### Scenario: 添加音乐标签字段
- **GIVEN** `ai_music` 表定义
- **WHEN** 添加 `tags` 字段
- **THEN** 字段类型为 VARCHAR(1024)
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "音乐风格标签(JSON 数组格式)"
- **AND** 存储格式为 JSON 字符串数组,如 `["流行","轻音乐"]`

#### Scenario: 添加音乐时长字段
- **GIVEN** `ai_music` 表定义
- **WHEN** 添加 `duration` 字段
- **THEN** 字段类型为 DOUBLE
- **AND** 字段允许为 NULL
- **AND** 字段注释为 "音乐时长(秒)"
- **AND** 支持小数,精确到毫秒级

## MODIFIED Requirements

### Requirement: 数据库脚本文件头注释
系统 SHALL 在数据库脚本文件头部包含清晰的元数据注释,便于版本管理和追溯。

#### Scenario: 更新脚本文件头
- **GIVEN** 数据库初始化脚本文件
- **WHEN** 修复脚本后
- **THEN** 文件头包含以下信息:
  - 脚本名称:`ruoyi-vue-pro - AI 模块数据库初始化脚本`
  - 数据库类型:`MySQL 5.7 / 8.0+`
  - 最后更新日期:`YYYY-MM-DD`
  - 版本号:如 `v2.0.0-fix-20251113`
  - 变更说明:列出主要修改内容

#### Scenario: 数据库字符集设置
- **GIVEN** 数据库初始化脚本
- **WHEN** 执行脚本
- **THEN** 设置字符集为 `utf8mb4`
- **AND** 设置排序规则为 `utf8mb4_unicode_ci`
- **AND** 确保所有表统一使用相同字符集

### Requirement: 表创建顺序优化
系统 SHALL 按照表的依赖关系组织建表语句的顺序,确保脚本可以顺利执行。

#### Scenario: 基础表优先创建
- **GIVEN** 数据库初始化脚本
- **WHEN** 按照依赖关系排序
- **THEN** 首先创建基础表(无外键依赖):
  - `ai_api_key` - API 密钥表
  - `ai_model` - 模型表
  - `ai_tool` - 工具表
  - `ai_workflow` - 工作流表
- **AND** 然后创建依赖表:
  - `ai_chat_role` - 聊天角色表(依赖 ai_model、ai_tool)
  - `ai_knowledge` - 知识库表(依赖 ai_model)
  - `ai_chat_conversation` - 对话表(依赖 ai_chat_role、ai_model)
  - `ai_knowledge_document` - 文档表(依赖 ai_knowledge)
  - `ai_chat_message` - 消息表(依赖 ai_chat_conversation、ai_chat_role、ai_model)
  - `ai_knowledge_segment` - 分段表(依赖 ai_knowledge、ai_knowledge_document)
- **AND** 最后创建独立功能表:
  - `ai_image` - 绘画表
  - `ai_music` - 音乐表
  - `ai_write` - 写作表
  - `ai_mind_map` - 思维导图表

### Requirement: 测试数据清理
系统 SHALL 清理脚本中的过时测试数据,仅保留必要的初始化数据。

#### Scenario: 移除测试数据
- **GIVEN** 数据库初始化脚本包含测试数据
- **WHEN** 清理测试数据
- **THEN** 移除所有用户生成的测试记录
- **AND** 移除个人简历等敏感测试内容
- **AND** 仅保留系统必需的初始化数据(如默认配置)

#### Scenario: 保留必要初始数据
- **GIVEN** 清理后的数据库脚本
- **WHEN** 检查初始数据
- **THEN** 可选保留平台配置示例(如 API Key 模板,不含真实密钥)
- **AND** 保留字典类型的初始值(如果需要)
- **AND** 所有保留数据必须是通用的、非敏感的

## REMOVED Requirements

无移除的需求。本次变更仅新增和修正,不删除现有表或字段,确保向后兼容。
