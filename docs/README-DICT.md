# 字典缓存使用文档

> 后续可能使用feign或在由各个服务自己实现对应的接口，这里先实现一个简单的版本

## 1. 功能介绍

基于 Caffeine 实现的字典缓存组件，主要用于系统字典数据的缓存管理。具有以下特性：

1. 高性能：使用 Caffeine 作为本地缓存，性能优异
2. 防止缓存穿透：缓存空值，避免频繁查询数据库
3. 防止缓存雪崩：使用随机过期时间，避免同时失效
4. 支持缓存预热：可配置启动时预热的字典数据
5. 支持多种数据类型：自动进行类型转换和比较
6. 支持按场景配置：不同业务场景可使用不同的SQL

## 2. 快速开始

### 2.1 配置说明

在 `application.yml` 中添加以下配置：

```yaml
yudao:
  dict:
    cache:
      # 是否开启缓存预热
      warmup-enabled: true
      # 默认的字典查询SQL
      default-sql: "SELECT value FROM system_dict_data WHERE dict_type = ? AND status = 0"
      # 缓存预热的字典类型
      warmup-dict-types:
        # 系统功能
        system:
          - sys_user_sex
          - sys_user_status
      # 缓存预热的SQL（可选，默认使用 default-sql）
      warmup-sqls:
        system: ${yudao.dict.cache.default-sql}
```

### 2.2 基础使用

```java
// 获取字典值
List<String> values = CaffeineDictCache.getDictValues("sys_user_sex", sql);

// 清除指定缓存
CaffeineDictCache.invalidateCache("sys_user_sex", sql);

// 清除所有缓存
CaffeineDictCache.invalidateAll();

// 获取缓存统计信息
String stats = CaffeineDictCache.getCacheStats();
```

### 2.3 在验证注解中使用

```java
// 单个值验证
@InEnum(dictType = "sys_user_sex")
private Integer sex;

// 集合验证
@InEnum(dictType = "sys_user_status")
private List<String> statusList;
```

## 3. 高级特性

### 3.1 缓存预热

系统启动时会自动预热配置的字典数据，可通过以下配置控制：

```yaml
yudao:
  dict:
    cache:
      warmup-enabled: true  # 是否开启预热
      warmup-dict-types:    # 预热的字典类型
        scene1:             # 场景标识
          - dict_type1      # 字典类型列表
          - dict_type2
```

### 3.2 自定义SQL

可以为不同的业务场景配置不同的SQL：

```yaml
yudao:
  dict:
    cache:
      warmup-sqls:
        scene1: "SELECT value FROM custom_dict WHERE type = ? AND status = 1"
        scene2: "SELECT dict_value FROM other_dict WHERE dict_type = ?"
```

## 4. 缓存配置说明

### 4.1 核心参数

- 初始容量：128
- 最大容量：1024
- 过期时间：120-180分钟（随机）
- 空值缓存：启用

### 4.2 缓存键生成规则

缓存键格式：`sql + "#" + dictType`

### 4.3 统计指标

通过 `getCacheStats()` 可获取以下统计信息：
- 缓存命中率
- 加载成功率
- 总请求数
- 加载异常数

## 5. 最佳实践

### 5.1 性能优化建议

1. 合理配置缓存容量
2. 按业务场景分组配置
3. 避免过多的字典类型预热
4. 定期监控缓存统计信息

### 5.2 配置建议

1. 将常用的字典类型配置为预热
2. 为不同业务场景使用不同的SQL
3. 设置合适的过期时间
4. 开启空值缓存避免穿透

### 5.3 异常处理

1. SQL配置错误：预热时会记录错误日志
2. 缓存未命中：返回空列表
3. 数据库异常：记录错误日志并返回空列表

## 6. 注意事项

1. 预热配置的SQL必须符合接口规范（参数为字典类型）
2. 缓存键包含SQL，注意SQL长度
3. 预热的字典类型不宜过多，避免影响启动时间
4. 建议在开发环境开启预热，生产环境按需配置

## 7. 常见问题

### 7.1 缓存未生效

- 检查 SQL 配置是否正确
- 确认字典类型是否存在
- 验证数据库连接是否正常

### 7.2 性能问题

- 检查缓存统计信息
- 优化缓存容量配置
- 减少不必要的预热配置

### 7.3 内存问题

- 合理设置最大容量
- 适当缩短过期时间
- 及时清理无用的字典类型

## 8. 版本历史

- v1.0.0: 基础版本，支持字典缓存，缓存预热功能 by Ken
