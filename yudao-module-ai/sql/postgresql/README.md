# AI 模块 PostgreSQL 数据库支持

## 概述

本目录包含 AI 模块在 PostgreSQL 数据库上运行所需的额外脚本和说明。

## 文件说明

### ai-sequences.sql
PostgreSQL 序列(Sequence)定义脚本,必须在创建表之前执行。

**包含序列**:
- `ai_api_key_seq` - API 密钥表序列
- `ai_model_seq` - 模型表序列
- `ai_tool_seq` - 工具表序列
- `ai_workflow_seq` - 工作流表序列
- `ai_chat_role_seq` - 聊天角色表序列
- `ai_knowledge_seq` - 知识库表序列
- `ai_chat_conversation_seq` - 对话表序列
- `ai_knowledge_document_seq` - 知识库文档表序列
- `ai_chat_message_seq` - 聊天消息表序列
- `ai_knowledge_segment_seq` - 文档分段表序列
- `ai_image_seq` - 绘画表序列
- `ai_music_seq` - 音乐表序列
- `ai_write_seq` - 写作表序列
- `ai_mind_map_seq` - 思维导图表序列

## 代码修正

为了支持 PostgreSQL,已修正以下实体类的 `@KeySequence` 注解:

### ✅ AiApiKeyDO
```java
// 修正前
@KeySequence("ai_chat_conversation_seq") ❌

// 修正后
@KeySequence("ai_api_key_seq") ✅
```

### ✅ AiChatMessageDO
```java
// 修正前
@KeySequence("ai_chat_conversation_seq") ❌

// 修正后
@KeySequence("ai_chat_message_seq") ✅
```

## 使用步骤

### 新环境部署

```bash
# 1. 创建序列
psql -U postgres -d ruoyi-vue-pro -f yudao-module-ai/sql/postgresql/ai-sequences.sql

# 2. 创建表结构
# 注意: 需要将 MySQL 脚本转换为 PostgreSQL 语法
# 主要差异:
# - BIT(1) -> BOOLEAN
# - datetime -> TIMESTAMP
# - AUTO_INCREMENT -> 使用序列
# - COMMENT 语法不同
# - 索引语法可能有差异

# 3. 验证序列创建
psql -U postgres -d ruoyi-vue-pro -c "
SELECT sequence_name, start_value, increment_by
FROM information_schema.sequences
WHERE sequence_schema = 'public'
  AND sequence_name LIKE 'ai_%_seq'
ORDER BY sequence_name;
"
```

### 已有环境升级

```bash
# 1. 备份数据库
pg_dump -U postgres ruoyi-vue-pro > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. 执行序列创建脚本(幂等性,可重复执行)
psql -U postgres -d ruoyi-vue-pro -f yudao-module-ai/sql/postgresql/ai-sequences.sql

# 3. 执行表结构升级(需要 PostgreSQL 版本的增量脚本)
```

## MySQL 与 PostgreSQL 的主要差异

### 数据类型映射

| MySQL | PostgreSQL | 说明 |
|-------|-----------|------|
| `BIT(1)` | `BOOLEAN` | 布尔类型 |
| `datetime` | `TIMESTAMP` | 日期时间类型 |
| `BIGINT AUTO_INCREMENT` | `BIGINT DEFAULT nextval('seq_name')` | 自增主键 |
| `VARCHAR(n) COLLATE utf8mb4_unicode_ci` | `VARCHAR(n)` | 字符串类型 |
| `TEXT COLLATE utf8mb4_unicode_ci` | `TEXT` | 长文本类型 |

### 语法差异

**注释**:
```sql
-- MySQL
COMMENT '这是注释'

-- PostgreSQL
COMMENT ON COLUMN table_name.column_name IS '这是注释';
```

**默认值**:
```sql
-- MySQL
DEFAULT CURRENT_TIMESTAMP
DEFAULT b'0'

-- PostgreSQL
DEFAULT CURRENT_TIMESTAMP
DEFAULT FALSE
```

**索引**:
```sql
-- MySQL
KEY `idx_name` (`column`) USING BTREE

-- PostgreSQL
CREATE INDEX idx_name ON table_name (column);
```

## 注意事项

1. **序列必须先创建**: 在创建表之前必须先执行 `ai-sequences.sql`

2. **建表脚本转换**: 目前提供的 `ai-2025-03-15.sql` 是 MySQL 语法,需要转换为 PostgreSQL 语法

3. **字符集**: PostgreSQL 默认使用 UTF-8,无需特别指定 `utf8mb4`

4. **测试验证**: 强烈建议在测试环境充分测试后再应用到生产环境

5. **序列起始值**: 如果从 MySQL 迁移数据到 PostgreSQL,需要调整序列的起始值:
   ```sql
   -- 查询表中最大 ID
   SELECT MAX(id) FROM ai_api_key;

   -- 设置序列起始值(假设最大 ID 是 100)
   SELECT setval('ai_api_key_seq', 100);
   ```

## 下一步工作

目前已完成的工作:
- ✅ 修正实体类的 KeySequence 注解
- ✅ 创建 PostgreSQL 序列定义脚本

待完成的工作:
- [ ] 将 MySQL 建表脚本转换为 PostgreSQL 语法
- [ ] 创建 PostgreSQL 版本的增量升级脚本
- [ ] 在 PostgreSQL 环境测试验证

## 技术支持

如有问题,请参考:
- [MyBatis-Plus 多数据库支持文档](https://baomidou.com/pages/56bea0/)
- [PostgreSQL 官方文档 - 序列](https://www.postgresql.org/docs/current/sql-createsequence.html)
- 项目变更日志: `yudao-module-ai/sql/CHANGELOG.md`

---

**版本**: 2025.10-SNAPSHOT
**更新日期**: 2025-11-13
**状态**: 序列定义完成,建表脚本待转换
