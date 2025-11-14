# 微信公众号模块 (yudao-module-mp)

> **模块版本**: 基于 ruoyi-vue-pro 2025.10-SNAPSHOT
> **最后更新**: 2025-11-14

---

## 模块概述

微信公众号模块提供完整的微信公众号管理功能，包括账号配置、粉丝管理、消息管理、素材管理、菜单管理和自动回复等核心功能。

### 核心功能

- **账号管理**: 支持多公众号配置管理
- **粉丝管理**: 粉丝信息同步、标签管理、分组管理
- **消息管理**: 消息收发、消息记录、事件处理
- **素材管理**: 永久素材和临时素材管理
- **菜单管理**: 自定义菜单配置、小程序链接
- **自动回复**: 关注回复、关键词回复、消息回复

---

## 数据库设计

### 表结构清单

本模块包含 **7个核心数据表**：

| 表名 | 描述 | 主要字段 |
|------|------|---------|
| `mp_account` | 公众号账号表 | appId, appSecret, token, aesKey |
| `mp_user` | 公众号粉丝表 | openid, subscribeStatus, tagIds |
| `mp_tag` | 公众号标签表 | tagId, name, count |
| `mp_menu` | 公众号菜单表 | name, type, url, replyContent |
| `mp_message` | 公众号消息表 | msgId, type, content, event |
| `mp_auto_reply` | 消息自动回复表 | type, requestKeyword, responseContent |
| `mp_material` | 公众号素材表 | mediaId, type, permanent, url |

### 数据库初始化脚本

本模块支持多种数据库：

#### MySQL
- **脚本位置**: `sql/mp-2024-05-29.sql`
- **支持版本**: MySQL 5.7 / 8.0+
- **字符集**: utf8mb4_unicode_ci

**使用方法**:
```bash
mysql -u root -p your_database < yudao-module-mp/sql/mp-2024-05-29.sql
```

#### PostgreSQL
- **脚本位置**: `sql/mp-postgresql.sql`
- **支持版本**: PostgreSQL 12+
- **编码**: UTF8

**使用方法**:
```bash
psql -U postgres -d your_database -f yudao-module-mp/sql/mp-postgresql.sql
```

**PostgreSQL 特性**:
- ✅ 使用 BIGSERIAL 实现主键自增
- ✅ 创建序列支持 @KeySequence 注解
- ✅ update_time 字段自动更新触发器
- ✅ 完整的表和字段注释

### 多租户支持

所有表都包含 `tenant_id` 字段，支持多租户隔离。

### 软删除

所有表使用 `deleted` 字段实现软删除（MySQL: bit(1), PostgreSQL: BOOLEAN）。

---

## 代码结构

```
yudao-module-mp/
├── src/main/java/cn/iocoder/yudao/module/mp/
│   ├── controller/        # 控制器层
│   │   └── admin/         # 管理后台接口
│   ├── service/           # 业务逻辑层
│   │   ├── account/       # 账号管理
│   │   ├── user/          # 粉丝管理
│   │   ├── tag/           # 标签管理
│   │   ├── menu/          # 菜单管理
│   │   ├── message/       # 消息管理
│   │   └── material/      # 素材管理
│   ├── dal/               # 数据访问层
│   │   ├── dataobject/    # 实体对象
│   │   └── mysql/         # MyBatis Mapper
│   ├── convert/           # 对象转换
│   ├── enums/             # 枚举定义
│   └── framework/         # 框架配置
└── sql/                   # 数据库脚本
    ├── mp-2024-05-29.sql      # MySQL脚本
    └── mp-postgresql.sql       # PostgreSQL脚本
```

---

## 核心实体类

### 1. MpAccountDO (公众号账号)
```java
@TableName("mp_account")
public class MpAccountDO extends TenantBaseDO {
    private Long id;
    private String name;       // 公众号名称
    private String account;    // 公众号账号
    private String appId;      // 公众号appid
    private String appSecret;  // 公众号密钥
    private String token;      // 公众号token
    private String aesKey;     // 消息加解密密钥
    // ... 其他字段
}
```

### 2. MpUserDO (公众号粉丝)
```java
@TableName(value = "mp_user", autoResultMap = true)
public class MpUserDO extends BaseDO {
    private Long id;
    private String openid;               // 用户标识
    private Integer subscribeStatus;     // 关注状态
    private LocalDateTime subscribeTime; // 关注时间
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> tagIds;           // 标签编号数组
    // ... 其他字段
}
```

**注意**: `tag_ids` 字段使用 `LongListTypeHandler`，在数据库中存储为逗号分隔的字符串。

### 3. MpMenuDO (公众号菜单)
```java
@TableName(value = "mp_menu", autoResultMap = true)
public class MpMenuDO extends BaseDO {
    private String name;            // 菜单名称
    private String menuKey;         // 菜单标识
    private Long parentId;          // 父菜单编号
    private String type;            // 按钮类型
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Article> replyArticles;  // 回复的图文消息数组
    // ... 其他字段
}
```

**注意**: `reply_articles` 字段使用 `JacksonTypeHandler`，在数据库中存储为JSON字符串。

---

## 枚举定义

### MpAutoReplyTypeEnum (回复类型)
- `SUBSCRIBE(1)`: 关注时回复
- `MESSAGE(2)`: 收到消息回复
- `KEYWORD(3)`: 关键词回复

### MpAutoReplyMatchEnum (匹配模式)
- `ALL(1)`: 完全匹配
- `LIKE(2)`: 半匹配

### MpMessageSendFromEnum (消息来源)
- `USER_TO_MP(1)`: 粉丝发送给公众号
- `MP_TO_USER(2)`: 公众号发给粉丝

---

## 技术特性

### TypeHandler 使用

本模块使用了MyBatis Plus的TypeHandler处理复杂类型：

1. **LongListTypeHandler**: 将 `List<Long>` 转换为逗号分隔字符串
   - 使用表: mp_user (tag_ids字段)
   - 数据库类型: VARCHAR(255)

2. **JacksonTypeHandler**: 将对象序列化为JSON字符串
   - 使用表: mp_menu, mp_message, mp_auto_reply (articles字段)
   - 数据库类型: VARCHAR(1024)

### 序列支持 (PostgreSQL)

所有实体类都使用 `@KeySequence` 注解：
```java
@KeySequence("mp_account_seq")
public class MpAccountDO extends TenantBaseDO {
    // ...
}
```

PostgreSQL脚本会自动创建对应的序列。

---

## 开发指南

### 添加新表

1. 在 `dal/dataobject` 中创建DO类
2. 添加 `@KeySequence` 注解（用于PostgreSQL）
3. 更新MySQL脚本和PostgreSQL脚本
4. 创建对应的Mapper接口
5. 实现Service业务逻辑

### 数据库脚本维护规范

1. **保持一致性**: MySQL和PostgreSQL脚本的表结构必须逻辑等价
2. **类型映射**: 参考设计文档中的类型映射规则
3. **注释完整**: 所有表和字段都必须有准确的注释
4. **触发器**: PostgreSQL脚本需为每个表创建update_time触发器

### 测试建议

1. 单元测试使用H2内存数据库
2. 集成测试分别验证MySQL和PostgreSQL
3. 确保TypeHandler字段的序列化和反序列化正常

---

## 依赖项

### 核心依赖
- **WxJava**: 微信开发Java SDK
- **MyBatis Plus**: ORM框架
- **Spring Boot**: 应用框架

### 外部服务
- **微信公众平台**: https://mp.weixin.qq.com/

---

## 参考资料

- [微信公众平台开发文档](https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html)
- [WxJava 文档](https://github.com/Wechat-Group/WxJava)
- [OpenSpec 提案: fix-mp-database-scripts](../openspec/changes/fix-mp-database-scripts/)

---

## 变更记录

| 日期 | 变更内容 | 作者 |
|------|---------|------|
| 2025-11-14 | 新增PostgreSQL支持，修复MySQL脚本问题 | AI Assistant |
| 2024-05-29 | 初始版本，创建MySQL脚本 | 芋道源码 |
