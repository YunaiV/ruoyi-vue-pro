# Design: fix-mp-database-scripts

## Overview
本设计文档描述如何修复微信公众号模块的数据库初始化脚本，确保MySQL脚本与代码实体完全一致，并新增PostgreSQL版本以支持多数据库部署。

## Architecture Context

### Current State
```
yudao-module-mp/
├── sql/
│   └── mp-2024-05-29.sql        # 仅有MySQL脚本
├── src/main/java/.../dal/dataobject/
│   ├── account/MpAccountDO.java
│   ├── user/MpUserDO.java
│   ├── tag/MpTagDO.java
│   ├── menu/MpMenuDO.java
│   ├── message/MpMessageDO.java
│   ├── message/MpAutoReplyDO.java
│   └── material/MpMaterialDO.java
```

### Target State
```
yudao-module-mp/
├── sql/
│   ├── mp-2024-05-29.sql        # 修复后的MySQL脚本
│   └── mp-postgresql.sql        # 新增PostgreSQL脚本
├── src/main/java/.../dal/dataobject/
│   └── （代码实体保持不变）
```

## Database Schema Design

### 表清单
本模块包含7个核心数据表：

| 表名 | 实体类 | 用途 | 特殊字段 |
|------|--------|------|---------|
| mp_account | MpAccountDO | 公众号账号配置 | 继承TenantBaseDO |
| mp_user | MpUserDO | 公众号粉丝信息 | tag_ids使用LongListTypeHandler |
| mp_tag | MpTagDO | 公众号标签 | tag_id字段使用INPUT策略 |
| mp_menu | MpMenuDO | 公众号菜单配置 | reply_articles使用JacksonTypeHandler |
| mp_message | MpMessageDO | 公众号消息记录 | articles使用JacksonTypeHandler |
| mp_auto_reply | MpAutoReplyDO | 消息自动回复规则 | response_articles使用JacksonTypeHandler |
| mp_material | MpMaterialDO | 公众号素材管理 | permanent字段为Boolean类型 |

### 通用字段设计

#### 审计字段（所有表）
```sql
creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除'
```

PostgreSQL版本：
```sql
creator VARCHAR(64) DEFAULT '' ,
create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updater VARCHAR(64) DEFAULT '',
update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
deleted BOOLEAN NOT NULL DEFAULT FALSE
```

#### 多租户字段（TenantBaseDO子类）
```sql
-- MySQL
tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号'

-- PostgreSQL
tenant_id BIGINT NOT NULL DEFAULT 0
```

MpAccountDO继承TenantBaseDO，其他DO继承BaseDO但也包含tenant_id字段。

### 主键策略

#### MySQL
使用AUTO_INCREMENT：
```sql
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
PRIMARY KEY (id) USING BTREE
```

#### PostgreSQL
使用序列和BIGSERIAL：
```sql
-- 创建序列
CREATE SEQUENCE IF NOT EXISTS mp_account_seq START WITH 6;

-- 方式1: 使用BIGSERIAL（推荐）
CREATE TABLE mp_account (
  id BIGSERIAL PRIMARY KEY,
  ...
);

-- 方式2: 显式使用序列
CREATE TABLE mp_account (
  id BIGINT NOT NULL DEFAULT nextval('mp_account_seq'),
  PRIMARY KEY (id)
);
```

所有表使用@KeySequence注解，序列名格式：`{表名}_seq`

### 类型映射规则

| Java类型 | MySQL类型 | PostgreSQL类型 | 说明 |
|---------|-----------|---------------|------|
| Long | bigint | BIGINT | 主键和ID字段 |
| String | varchar(N) | VARCHAR(N) | N根据业务定义 |
| Integer (枚举) | tinyint | SMALLINT | 状态、类型字段 |
| Boolean | bit(1) | BOOLEAN | 是否字段 |
| LocalDateTime | datetime | TIMESTAMP | 时间字段 |
| Double | double | DOUBLE PRECISION | 地理坐标 |
| List<Long> | varchar(255) | VARCHAR(255) | 使用LongListTypeHandler |
| List<Article> | varchar(1024) | VARCHAR(1024) | 使用JacksonTypeHandler |

### 特殊字段处理

#### 1. mp_user.tag_ids
```java
@TableField(typeHandler = LongListTypeHandler.class)
private List<Long> tagIds;
```
存储格式：逗号分隔的Long数组，如"1,2,3"
数据库类型：VARCHAR(255)

#### 2. mp_menu.reply_articles 和 mp_message.articles
```java
@TableField(typeHandler = JacksonTypeHandler.class)
private List<MpMessageDO.Article> replyArticles;
```
存储格式：JSON数组
数据库类型：VARCHAR(1024)

#### 3. mp_material.permanent
```java
private Boolean permanent;
```
MySQL: bit(1)
PostgreSQL: BOOLEAN

## PostgreSQL特定设计

### 触发器设计
由于PostgreSQL不支持`ON UPDATE CURRENT_TIMESTAMP`，需要通过触发器实现：

```sql
-- 1. 创建触发器函数（全局，只需创建一次）
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. 为每个表创建触发器
CREATE TRIGGER update_mp_account_update_time
BEFORE UPDATE ON mp_account
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
```

### 注释设计
PostgreSQL使用`COMMENT ON`语法添加注释：

```sql
COMMENT ON TABLE mp_account IS '公众号账号表';
COMMENT ON COLUMN mp_account.id IS '编号';
COMMENT ON COLUMN mp_account.name IS '公众号名称';
-- ... 每个字段都需要单独的COMMENT语句
```

### 序列命名规范
- 格式：`{表名}_seq`
- 起始值：根据AUTO_INCREMENT值设置（如mp_account从6开始）
- 示例：
  ```sql
  CREATE SEQUENCE IF NOT EXISTS mp_account_seq START WITH 6;
  CREATE SEQUENCE IF NOT EXISTS mp_user_seq START WITH 55;
  CREATE SEQUENCE IF NOT EXISTS mp_tag_seq START WITH 14;
  CREATE SEQUENCE IF NOT EXISTS mp_menu_seq START WITH 169;
  CREATE SEQUENCE IF NOT EXISTS mp_message_seq START WITH 414;
  CREATE SEQUENCE IF NOT EXISTS mp_auto_reply_seq START WITH 55;
  CREATE SEQUENCE IF NOT EXISTS mp_material_seq START WITH 98;
  ```

## Script Structure

### MySQL脚本结构
```sql
/*
 Navicat Premium Data Transfer
 (元信息)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mp_account
-- ----------------------------
DROP TABLE IF EXISTS `mp_account`;
CREATE TABLE `mp_account` ( ... );

-- ----------------------------
-- Records of mp_account
-- ----------------------------
BEGIN;
COMMIT;

-- (重复其他表)

SET FOREIGN_KEY_CHECKS = 1;
```

### PostgreSQL脚本结构
```sql
/*
 MP模块数据库初始化脚本 (PostgreSQL)

 Source: mp-2024-05-29.sql (MySQL)
 Target: PostgreSQL 12+
 Generated: 2025-11-14

 Features:
 - 7 tables for WeChat Official Account module
 - Multi-tenant support (tenant_id)
 - Soft delete (deleted flag)
 - Audit fields (creator, create_time, updater, update_time)
 - Auto-update trigger for update_time field
*/

-- ========== 创建触发器函数（用于 update_time 自动更新）==========
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========== 创建序列 ==========
CREATE SEQUENCE IF NOT EXISTS mp_account_seq START WITH 6;
-- ... 其他序列

-- ========== 公众号账号管理 ==========
DROP TABLE IF EXISTS mp_account CASCADE;
CREATE TABLE mp_account ( ... );

COMMENT ON TABLE mp_account IS '公众号账号表';
COMMENT ON COLUMN mp_account.id IS '编号';
-- ... 其他注释

CREATE TRIGGER update_mp_account_update_time
BEFORE UPDATE ON mp_account
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- (重复其他表)
```

## Validation Strategy

### 脚本验证步骤
1. **语法验证**：在目标数据库中执行脚本，确保无语法错误
2. **结构验证**：对比生成的表结构与预期定义
3. **注释验证**：检查所有表和字段的注释是否完整
4. **触发器验证**（PostgreSQL）：测试update操作，确认update_time自动更新
5. **类型验证**：确认字段类型映射正确

### 测试环境要求
- MySQL 5.7 或 8.0+
- PostgreSQL 12+

### 验证SQL示例
```sql
-- 1. 检查表是否存在
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'your_database' AND table_name LIKE 'mp_%';

-- 2. 检查字段定义（PostgreSQL）
SELECT column_name, data_type, character_maximum_length, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'mp_account'
ORDER BY ordinal_position;

-- 3. 测试触发器（PostgreSQL）
INSERT INTO mp_account (name, account, app_id, app_secret, token)
VALUES ('test', 'test', 'test', 'test', 'test');
UPDATE mp_account SET name = 'test2' WHERE name = 'test';
SELECT name, update_time FROM mp_account WHERE name = 'test2';
-- update_time应自动更新
```

## Implementation Checklist
- [ ] 创建PostgreSQL脚本文件
- [ ] 转换所有表结构
- [ ] 添加所有序列定义
- [ ] 创建触发器函数和触发器
- [ ] 添加所有表和字段注释
- [ ] 验证MySQL脚本与代码实体一致性
- [ ] 在MySQL环境测试脚本
- [ ] 在PostgreSQL环境测试脚本
- [ ] 更新模块文档

## References
- 参考脚本：`yudao-module-erp/sql/erp-postgresql.sql`
- 参考脚本：`yudao-module-crm/sql/crm-postgresql.sql`
- MyBatis Plus文档：https://baomidou.com/
- PostgreSQL文档：https://www.postgresql.org/docs/
