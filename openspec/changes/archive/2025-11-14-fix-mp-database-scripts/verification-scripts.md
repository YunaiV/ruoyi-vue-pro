# 数据库脚本验证指南

## MySQL验证脚本

### 1. 创建测试数据库并执行脚本

```bash
# 创建测试数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS mp_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行脚本
mysql -u root -p mp_test < yudao-module-mp/sql/mp-2024-05-29.sql

# 验证结果
mysql -u root -p mp_test -e "SHOW TABLES;"
```

### 2. 验证表结构

```sql
-- 验证所有表都已创建
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'mp_test' AND TABLE_NAME LIKE 'mp_%'
ORDER BY TABLE_NAME;

-- 验证mp_account表结构
DESC mp_account;

-- 验证mp_menu表的parent_id字段类型（应该是bigint）
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mp_test'
  AND TABLE_NAME = 'mp_menu'
  AND COLUMN_NAME = 'parent_id';

-- 验证mp_account表没有url字段
SELECT COUNT(*) as url_field_count
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mp_test'
  AND TABLE_NAME = 'mp_account'
  AND COLUMN_NAME = 'url';
-- 应该返回 0

-- 验证所有表的字段数量
SELECT TABLE_NAME, COUNT(*) as COLUMN_COUNT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mp_test' AND TABLE_NAME LIKE 'mp_%'
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;
```

### 3. 验证字段注释

```sql
-- 验证mp_account.aes_key注释
SELECT COLUMN_NAME, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mp_test'
  AND TABLE_NAME = 'mp_account'
  AND COLUMN_NAME = 'aes_key';
-- 应该显示"消息加解密密钥"

-- 验证mp_tag.count注释
SELECT COLUMN_NAME, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'mp_test'
  AND TABLE_NAME = 'mp_tag'
  AND COLUMN_NAME = 'count';
-- 应该显示"此标签下粉丝数"
```

### 4. 测试插入和更新

```sql
-- 测试插入数据
INSERT INTO mp_account (name, account, app_id, app_secret, token)
VALUES ('测试公众号', 'test_account', 'test_app_id', 'test_secret', 'test_token');

-- 验证自增ID和默认值
SELECT id, name, creator, create_time, deleted, tenant_id
FROM mp_account
WHERE name = '测试公众号';

-- 测试update_time自动更新
UPDATE mp_account SET name = '测试公众号2' WHERE name = '测试公众号';

-- 验证update_time已更新
SELECT name, create_time, update_time
FROM mp_account
WHERE name = '测试公众号2';
-- update_time应该大于create_time

-- 清理测试数据
DELETE FROM mp_account WHERE name = '测试公众号2';
```

## PostgreSQL验证脚本

### 1. 创建测试数据库并执行脚本

```bash
# 创建测试数据库
psql -U postgres -c "CREATE DATABASE mp_test ENCODING 'UTF8';"

# 执行脚本
psql -U postgres -d mp_test -f yudao-module-mp/sql/mp-postgresql.sql

# 验证结果
psql -U postgres -d mp_test -c "\dt"
```

### 2. 验证表结构

```sql
-- 验证所有表都已创建
SELECT table_name,
       obj_description((table_schema||'.'||table_name)::regclass, 'pg_class') as table_comment
FROM information_schema.tables
WHERE table_schema = 'public' AND table_name LIKE 'mp_%'
ORDER BY table_name;

-- 验证序列创建
SELECT sequence_name, start_value, minimum_value
FROM information_schema.sequences
WHERE sequence_name LIKE 'mp_%_seq'
ORDER BY sequence_name;

-- 验证mp_menu表的parent_id字段类型（应该是bigint）
SELECT column_name, data_type,
       col_description((table_schema||'.'||table_name)::regclass::oid, ordinal_position) as column_comment
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'mp_menu'
  AND column_name = 'parent_id';

-- 验证触发器创建
SELECT trigger_name, event_manipulation, event_object_table
FROM information_schema.triggers
WHERE trigger_name LIKE 'update_mp_%'
ORDER BY event_object_table;
```

### 3. 验证字段注释

```sql
-- 验证mp_account表的所有字段注释
SELECT a.attname as column_name,
       format_type(a.atttypid, a.atttypmod) as data_type,
       col_description(a.attrelid, a.attnum) as column_comment
FROM pg_attribute a
JOIN pg_class t ON a.attrelid = t.oid
JOIN pg_namespace s ON t.relnamespace = s.oid
WHERE s.nspname = 'public'
  AND t.relname = 'mp_account'
  AND a.attnum > 0
  AND NOT a.attisdropped
ORDER BY a.attnum;

-- 验证mp_tag.count注释
SELECT col_description('mp_tag'::regclass,
       (SELECT attnum FROM pg_attribute WHERE attrelid = 'mp_tag'::regclass AND attname = 'count')
) as count_comment;
-- 应该显示"此标签下粉丝数"
```

### 4. 测试插入和更新

```sql
-- 测试插入数据
INSERT INTO mp_account (name, account, app_id, app_secret, token)
VALUES ('测试公众号', 'test_account', 'test_app_id', 'test_secret', 'test_token');

-- 验证序列自增和默认值
SELECT id, name, creator, create_time, deleted, tenant_id
FROM mp_account
WHERE name = '测试公众号';

-- 测试update_time触发器
SELECT name, create_time, update_time,
       (update_time = create_time) as times_equal
FROM mp_account
WHERE name = '测试公众号';
-- times_equal应该为true（初始插入时相等）

-- 更新数据测试触发器
UPDATE mp_account SET name = '测试公众号2' WHERE name = '测试公众号';

-- 等待1秒
SELECT pg_sleep(1);

-- 再次更新
UPDATE mp_account SET name = '测试公众号3' WHERE name = '测试公众号2';

-- 验证update_time已自动更新
SELECT name, create_time, update_time,
       (update_time > create_time) as time_updated
FROM mp_account
WHERE name = '测试公众号3';
-- time_updated应该为true

-- 清理测试数据
DELETE FROM mp_account WHERE name = '测试公众号3';
```

### 5. 验证类型映射

```sql
-- 验证Boolean类型映射
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('mp_account', 'mp_material')
  AND column_name IN ('deleted', 'permanent')
ORDER BY table_name, column_name;
-- 都应该是boolean类型

-- 验证SMALLINT类型映射
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('mp_user', 'mp_message', 'mp_auto_reply')
  AND column_name IN ('subscribe_status', 'send_from', 'type', 'request_match')
ORDER BY table_name, column_name;
-- 都应该是smallint类型

-- 验证DOUBLE PRECISION类型映射
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'mp_message'
  AND column_name IN ('location_x', 'location_y', 'scale')
ORDER BY column_name;
-- 都应该是double precision类型
```

## 预期验证结果

### MySQL

✅ **表数量**: 7个表 (mp_account, mp_auto_reply, mp_material, mp_menu, mp_message, mp_tag, mp_user)

✅ **字段数量**:
- mp_account: 15个字段
- mp_user: 23个字段
- mp_tag: 12个字段
- mp_menu: 27个字段
- mp_message: 32个字段
- mp_auto_reply: 23个字段
- mp_material: 17个字段

✅ **关键修复**:
- mp_account表没有url字段
- mp_menu.parent_id是bigint类型
- mp_account.aes_key注释为"消息加解密密钥"
- mp_tag.count注释为"此标签下粉丝数"

### PostgreSQL

✅ **表数量**: 7个表

✅ **序列数量**: 7个序列 (mp_account_seq到mp_user_seq)

✅ **触发器数量**: 7个update_time触发器

✅ **类型映射**:
- bit(1) → BOOLEAN
- tinyint → SMALLINT
- double → DOUBLE PRECISION
- datetime → TIMESTAMP

✅ **触发器功能**: update_time字段在UPDATE时自动更新为当前时间

## 清理测试环境

```bash
# MySQL
mysql -u root -p -e "DROP DATABASE IF EXISTS mp_test;"

# PostgreSQL
psql -U postgres -c "DROP DATABASE IF EXISTS mp_test;"
```
