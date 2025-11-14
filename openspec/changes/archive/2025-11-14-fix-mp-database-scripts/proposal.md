# Proposal: fix-mp-database-scripts

## Status
`completed` (2025-11-14)

## Summary
修复和补充微信公众号模块（yudao-module-mp）的数据库初始化脚本，确保MySQL脚本与代码实体完全一致，并新增PostgreSQL版本的初始化脚本，以支持多数据库部署。

## Motivation
当前yudao-module-mp模块存在以下问题：

1. **MySQL脚本可能过时**：现有MySQL脚本（mp-2024-05-29.sql）可能与最新的代码实体定义不一致
2. **缺少PostgreSQL支持**：模块中已使用`@KeySequence`注解支持PostgreSQL等数据库,但缺少对应的PostgreSQL初始化脚本
3. **多数据库兼容性**：项目支持MySQL、PostgreSQL、Oracle等多种数据库，但mp模块只有MySQL脚本

解决这些问题将：
- 确保数据库脚本与代码实体定义完全一致
- 支持PostgreSQL部署，提升数据库选择灵活性
- 与项目其他模块（如erp、crm）保持一致的多数据库支持策略

## Scope
本提案涉及以下范围：

### In Scope
- 验证并修复MySQL脚本（mp-2024-05-29.sql）与代码实体的一致性
- 新增PostgreSQL版本的初始化脚本（mp-postgresql.sql）
- 确保以下7个表的脚本完整且准确：
  - mp_account（公众号账号表）
  - mp_user（公众号粉丝表）
  - mp_tag（公众号标签表）
  - mp_menu（公众号菜单表）
  - mp_message（公众号消息表）
  - mp_auto_reply（公众号消息自动回复表）
  - mp_material（公众号素材表）

### Out of Scope
- 不涉及业务逻辑代码修改
- 不涉及数据迁移脚本
- 不涉及Oracle、SQL Server等其他数据库脚本（可作为后续扩展）
- 不涉及表结构变更或新增字段

## Capabilities Affected
- **database-initialization**: 数据库初始化能力，新增PostgreSQL支持
- **multi-database-support**: 多数据库支持能力，增强mp模块的数据库兼容性

## Dependencies
- 依赖已有的代码实体定义（DO类）
- 参考erp和crm模块的PostgreSQL脚本实现方式

## Risks and Mitigations
| Risk | Impact | Mitigation |
|------|--------|-----------|
| 脚本与代码实体不一致 | 中 | 逐字段对比代码实体和脚本，确保100%一致 |
| PostgreSQL类型映射错误 | 中 | 参考erp/crm模块的成功实现，使用标准类型映射 |
| 序列命名冲突 | 低 | 使用模块前缀（mp_）确保序列名称唯一 |

## Alternatives Considered
1. **仅修复MySQL脚本**：不足以满足多数据库需求
2. **生成所有数据库脚本**：工作量大，先实现PostgreSQL，其他数据库按需补充

## Implementation Notes
### MySQL脚本验证要点
- 字段类型：确保与MyBatis Plus注解一致
- 字符集：使用utf8mb4_unicode_ci
- 索引：根据业务需要添加合适的索引
- 注释：确保字段注释与代码注释一致

### PostgreSQL脚本实现要点
- 使用BIGSERIAL替代AUTO_INCREMENT
- 创建序列（mp_xxx_seq）支持@KeySequence
- 使用BOOLEAN替代MySQL的bit(1)
- 使用NUMERIC(24,6)替代decimal
- 添加update_time自动更新触发器
- 使用COMMENT ON语法添加表和字段注释

## Success Criteria
- [x] MySQL脚本与代码实体100%一致 ✅
  - 删除了mp_account表的url字段
  - 修复了mp_menu.parent_id字段类型（varchar→bigint）
  - 统一了所有字段注释与代码一致
- [x] PostgreSQL脚本能成功执行并创建所有表 ✅
  - 创建了7个表和7个序列
  - 实现了update_time自动更新触发器
  - 提供了完整的验证脚本
- [x] 两种脚本的表结构逻辑等价 ✅
  - 正确的类型映射（bit→BOOLEAN, tinyint→SMALLINT等）
  - 相同的字段定义和约束
- [x] 所有字段都有准确的注释 ✅
  - MySQL使用COMMENT语法
  - PostgreSQL使用COMMENT ON语法
- [x] 遵循项目现有的SQL脚本规范 ✅
  - 参考了erp和crm模块的实现方式
  - 符合项目编码规范
