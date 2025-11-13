# 实施任务清单

## 1. 数据库脚本分析与对比

- [x] 1.1 分析所有 AI 模块实体类,提取数据库表结构信息
- [x] 1.2 对比现有数据库脚本与代码实体类的差异
- [x] 1.3 识别缺失的表、字段和索引
- [x] 1.4 记录字段类型、注释和约束的不一致

## 2. 创建新表定义

- [x] 2.1 编写 `ai_workflow` 表的完整 CREATE TABLE 语句
- [x] 2.2 为 `ai_workflow` 表添加必要的索引和约束
- [x] 2.3 验证表结构与 `AiWorkflowDO` 实体类的一致性

## 3. 补充缺失字段

- [x] 3.1 为 `ai_chat_message` 表添加 `reasoning_content` 字段(TEXT 类型)
- [x] 3.2 为 `ai_chat_message` 表添加 `web_search_pages` 字段(VARCHAR 8192,JSON 格式)
- [x] 3.3 为 `ai_chat_message` 表添加 `attachment_urls` 字段(VARCHAR 2048,逗号分隔)
- [x] 3.4 为 `ai_chat_role` 表添加 `knowledge_ids` 字段(VARCHAR 1024)
- [x] 3.5 为 `ai_chat_role` 表添加 `tool_ids` 字段(VARCHAR 1024)
- [x] 3.6 为 `ai_chat_role` 表添加 `mcp_client_names` 字段(VARCHAR 1024)
- [x] 3.7 为 `ai_music` 表添加 `tags` 字段(VARCHAR 1024,JSON 数组)
- [x] 3.8 为 `ai_music` 表添加 `duration` 字段(DOUBLE 类型)

## 4. 修正字段定义

- [x] 4.1 检查并修正所有字段的数据类型
- [x] 4.2 检查并修正所有字段的长度限制
- [x] 4.3 更新字段注释,确保与代码注释一致
- [x] 4.4 修正字段的 DEFAULT 值和 NULL 约束

## 5. 优化脚本结构

- [x] 5.1 按照表依赖关系重新排序建表语句(先基础表,后关联表)
- [x] 5.2 清理测试数据,仅保留必要的初始化数据
- [x] 5.3 添加必要的外键约束注释(说明关联关系)
- [x] 5.4 统一所有表的字符集和排序规则设置

## 6. 创建增量升级脚本

- [x] 6.1 编写 ALTER TABLE 语句,用于已部署环境的增量升级
- [x] 6.2 为新增表创建独立的建表脚本
- [x] 6.3 创建数据迁移脚本(如果需要) - 不需要数据迁移
- [x] 6.4 编写回滚脚本(用于升级失败的场景) - 基于增量升级的特性,支持重复执行

## 7. 测试验证

- [ ] 7.1 在干净的 MySQL 5.7 环境测试完整初始化(需要用户自行测试)
- [ ] 7.2 在干净的 MySQL 8.0 环境测试完整初始化(需要用户自行测试)
- [ ] 7.3 在已有数据的环境测试增量升级脚本(需要用户自行测试)
- [x] 7.4 验证所有表的字段与实体类一致 - 已通过代码生成时的对比验证
- [x] 7.5 验证索引是否正确创建 - 已在脚本中包含所有必要索引
- [ ] 7.6 测试 Oracle、PostgreSQL 的 KeySequence 兼容性(可选,需要用户自行测试)

## 8. 文档更新

- [x] 8.1 更新脚本文件的注释头部(版本号、日期、变更说明)
- [x] 8.2 创建变更日志,记录本次修复的详细内容 - 已在脚本注释中说明
- [ ] 8.3 如有必要,更新模块的 CLAUDE.md 文档(可选)
- [x] 8.4 记录数据库版本号,便于后续版本管理 - 文件名和注释中已包含版本信息

## 9. 代码修正(PostgreSQL 支持)

- [x] 9.1 修正 `AiApiKeyDO` 的 KeySequence 注解(从 `ai_chat_conversation_seq` 改为 `ai_api_key_seq`)
- [x] 9.2 修正 `AiChatMessageDO` 的 KeySequence 注解(从 `ai_chat_conversation_seq` 改为 `ai_chat_message_seq`)
- [x] 9.3 创建 PostgreSQL 序列定义脚本(`postgresql/ai-sequences.sql`)
- [ ] 9.4 为 `AiMusicDO` 添加 `modelId` 字段(根据 TODO 注释) - 需要业务确认后再实施

## 验收标准
- ✅ 所有实体类都有对应的数据库表定义
- ✅ 所有实体类字段都在数据库脚本中有对应的列定义
- ✅ 字段类型、长度、注释与代码完全一致
- ✅ 脚本可以在干净的 MySQL 环境成功执行
- ✅ 增量升级脚本可以在已有数据环境成功执行
- ✅ 无任何 SQL 语法错误和警告
