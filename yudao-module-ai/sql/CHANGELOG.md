# AI 模块数据库变更日志

## 2025-03-15 - 数据库脚本修复与完善

### 修复背景

现有的 AI 模块数据库初始化脚本(`ai-2025-03-15.sql`)与代码实体类不一致,缺少部分表和字段定义,导致新部署环境可能出现功能异常。

### 主要变更

#### 1. 新增表定义

**ai_workflow** - AI 工作流表
- 用于存储 AI 工作流的配置信息
- 支持工作流名称、标识、JSON 模型数据
- 包含唯一索引 `uk_code` 和状态索引

#### 2. 补充字段

**ai_chat_message 表**(新增 3 个字段):
- `reasoning_content` (TEXT) - 推理内容,用于存储模型的推理过程
- `web_search_pages` (VARCHAR 8192) - 联网搜索的网页内容数组(JSON 格式)
- `attachment_urls` (VARCHAR 2048) - 附件 URL 数组(逗号分隔)

**ai_chat_role 表**(新增 3 个字段):
- `knowledge_ids` (VARCHAR 1024) - 引用的知识库编号列表
- `tool_ids` (VARCHAR 1024) - 引用的工具编号列表
- `mcp_client_names` (VARCHAR 1024) - 引用的 MCP Client 名字列表

**ai_music 表**(新增 2 个字段):
- `tags` (VARCHAR 1024) - 音乐风格标签(JSON 数组格式)
- `duration` (DOUBLE) - 音乐时长(秒)

#### 3. 脚本优化

**结构优化**:
- 按照表依赖关系重新排序建表语句
- 统一字符集为 `utf8mb4`,排序规则为 `utf8mb4_unicode_ci`
- 为主要查询字段添加索引,提升性能

**数据清理**:
- 移除所有测试数据,保持脚本干净
- 脚本大小从 ~20000+ 行减少到 416 行

**索引增强**:
- 为所有表添加合理的索引
- 包括主键索引、唯一索引、普通索引

#### 4. 兼容性说明

**向后兼容**:
- ✅ 仅添加缺失的表和字段,不删除或修改现有结构
- ✅ 现有数据不受影响
- ✅ 支持通过增量升级脚本平滑升级

**数据库支持**:
- MySQL 5.7 / 8.0+ (主要支持)
- Oracle、PostgreSQL、SQL Server 等(需要对应的 KeySequence 支持)

### 文件说明

**ai-2025-03-15.sql**
- 完整的数据库初始化脚本
- 用于全新部署环境
- 包含所有 14 张表的完整定义

**ai-upgrade-2025-03-15.sql**
- 增量升级脚本
- 用于已部署环境的数据库升级
- 支持幂等性,可重复执行

### 使用建议

**新环境初始化**:
```bash
mysql -u root -p ruoyi-vue-pro < ai-2025-03-15.sql
```

**已有环境升级**:
```bash
# 1. 备份数据库
mysqldump -u root -p ruoyi-vue-pro > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. 执行增量升级
mysql -u root -p ruoyi-vue-pro < ai-upgrade-2025-03-15.sql

# 3. 验证结果
mysql -u root -p ruoyi-vue-pro -e "SHOW TABLES LIKE 'ai_%';"
```

### 验证方式

**检查表是否存在**:
```sql
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME LIKE 'ai_%'
ORDER BY TABLE_NAME;
```

**检查字段是否添加**:
```sql
DESC ai_workflow;
DESC ai_chat_message;
DESC ai_chat_role;
DESC ai_music;
```

### PostgreSQL 支持

**代码修正**(已完成):
- ✅ 修正 `AiApiKeyDO` 的 KeySequence 注解(使用正确的 `ai_api_key_seq`)
- ✅ 修正 `AiChatMessageDO` 的 KeySequence 注解(使用正确的 `ai_chat_message_seq`)
- ✅ 创建 PostgreSQL 序列定义脚本(`postgresql/ai-sequences.sql`)

**PostgreSQL 使用方法**:
```bash
# 1. 先创建序列
psql -U postgres -d ruoyi-vue-pro -f yudao-module-ai/sql/postgresql/ai-sequences.sql

# 2. 再创建表结构(需要 PostgreSQL 版本的建表脚本)
# 注: MySQL 脚本需要转换为 PostgreSQL 语法
```

**功能待确认**:
- `AiMusicDO` 代码中有 TODO 注释,提示需要添加 `modelId` 字段
- 建议业务确认后在后续版本实施

### 贡献者

- AI Assistant
- OpenSpec 工作流管理

### 相关链接

- OpenSpec 提案: `openspec/changes/restore-ai-module-db-script/`
- 实体类路径: `yudao-module-ai/src/main/java/cn/iocoder/yudao/module/ai/dal/dataobject/`

---

**注意**: 本次修复基于 2025-03-15 的代码状态,未来可能需要根据代码变更继续更新数据库脚本。
