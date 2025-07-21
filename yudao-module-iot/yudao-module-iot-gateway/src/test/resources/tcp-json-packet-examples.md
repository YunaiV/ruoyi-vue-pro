# TCP JSON格式协议说明

## 1. 协议概述

TCP JSON格式协议采用纯JSON格式进行数据传输，参考了EMQX和HTTP模块的数据格式设计，具有以下优势：

- **标准化**：使用标准JSON格式，易于解析和处理
- **可读性**：人类可读，便于调试和维护
- **扩展性**：可以轻松添加新字段，向后兼容
- **统一性**：与HTTP模块保持一致的数据格式

## 2. 消息格式

### 2.1 基础消息结构

```json
{
  "id": "消息唯一标识",
  "method": "消息方法",
  "deviceId": "设备ID",
  "params": {
    // 消息参数
  },
  "timestamp": 时间戳
}
```

### 2.2 字段说明

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 消息唯一标识，UUID格式 |
| method | String | 是 | 消息方法，如 thing.property.post |
| deviceId | Long | 是 | 设备ID |
| params | Object | 否 | 消息参数，具体内容根据method而定 |
| timestamp | Long | 是 | 时间戳（毫秒） |
| code | Integer | 否 | 响应码（下行消息使用） |
| message | String | 否 | 响应消息（下行消息使用） |

## 3. 消息类型

### 3.1 数据上报 (thing.property.post)

设备向服务器上报属性数据。

**示例：**
```json
{
  "id": "8ac6a1db91e64aa9996143fdbac2cbfe",
  "method": "thing.property.post",
  "deviceId": 123456,
  "params": {
    "temperature": 25.5,
    "humidity": 60.2,
    "pressure": 1013.25,
    "battery": 85
  },
  "timestamp": 1753111026437
}
```

### 3.2 心跳 (thing.state.online)

设备向服务器发送心跳保活。

**示例：**
```json
{
  "id": "7db8c4e6408b40f8b2549ddd94f6bb02",
  "method": "thing.state.online",
  "deviceId": 123456,
  "timestamp": 1753111026467
}
```

### 3.3 事件上报 (thing.event.post)

设备向服务器上报事件信息。

**示例：**
```json
{
  "id": "9e7d72731b854916b1baa5088bd6a907",
  "method": "thing.event.post",
  "deviceId": 123456,
  "params": {
    "eventType": "alarm",
    "level": "warning",
    "description": "温度过高",
    "value": 45.8
  },
  "timestamp": 1753111026468
}
```

### 3.4 属性设置 (thing.property.set)

服务器向设备下发属性设置指令。

**示例：**
```json
{
  "id": "cmd_001",
  "method": "thing.property.set",
  "deviceId": 123456,
  "params": {
    "targetTemperature": 22.0,
    "mode": "auto"
  },
  "timestamp": 1753111026469
}
```

### 3.5 服务调用 (thing.service.invoke)

服务器向设备调用服务。

**示例：**
```json
{
  "id": "service_001",
  "method": "thing.service.invoke",
  "deviceId": 123456,
  "params": {
    "service": "restart",
    "args": {
      "delay": 5
    }
  },
  "timestamp": 1753111026470
}
```

## 4. 复杂数据示例

### 4.1 多传感器综合数据

```json
{
  "id": "complex_001",
  "method": "thing.property.post",
  "deviceId": 789012,
  "params": {
    "environment": {
      "temperature": 23.8,
      "humidity": 55.0,
      "co2": 420,
      "pm25": 35
    },
    "location": {
      "latitude": 39.9042,
      "longitude": 116.4074,
      "altitude": 43.5,
      "speed": 0.0
    },
    "status": {
      "battery": 78,
      "signal": -65,
      "online": true,
      "version": "1.2.3"
    }
  },
  "timestamp": 1753111026471
}
```

## 5. 与EMQX格式的兼容性

本协议设计参考了EMQX的消息格式，具有良好的兼容性：

### 5.1 EMQX标准格式

```json
{
  "id": "msg_001",
  "method": "thing.property.post",
  "deviceId": 123456,
  "params": {
    "temperature": 25.5,
    "humidity": 60.2
  },
  "timestamp": 1642781234567
}
```

### 5.2 兼容性说明

- ✅ **字段名称**：与EMQX保持一致
- ✅ **数据类型**：完全兼容
- ✅ **消息结构**：结构相同
- ✅ **扩展字段**：支持自定义扩展

## 6. 使用示例

### 6.1 Java编码示例

```java
// 创建编解码器
IotTcpJsonDeviceMessageCodec codec = new IotTcpJsonDeviceMessageCodec();

// 创建数据上报消息
Map<String, Object> sensorData = Map.of(
    "temperature", 25.5,
    "humidity", 60.2
);
IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", sensorData);
message.setDeviceId(123456L);

// 编码为字节数组
byte[] jsonBytes = codec.encode(message);

// 解码
IotDeviceMessage decoded = codec.decode(jsonBytes);
```

### 6.2 便捷方法示例

```java
// 快速编码数据上报
byte[] dataPacket = codec.encodeDataReport(sensorData, 123456L, "product_key", "device_name");

// 快速编码心跳
byte[] heartbeatPacket = codec.encodeHeartbeat(123456L, "product_key", "device_name");

// 快速编码事件
byte[] eventPacket = codec.encodeEventReport(eventData, 123456L, "product_key", "device_name");
```

## 7. 协议优势

### 7.1 与原TCP二进制协议对比

| 特性 | 二进制协议 | JSON协议 |
|------|------------|----------|
| 可读性 | 差 | 优秀 |
| 调试难度 | 高 | 低 |
| 扩展性 | 差 | 优秀 |
| 解析复杂度 | 高 | 低 |
| 数据大小 | 小 | 稍大 |
| 标准化程度 | 低 | 高 |

### 7.2 适用场景

- ✅ **开发调试**：JSON格式便于查看和调试
- ✅ **快速集成**：标准JSON格式，集成简单
- ✅ **协议扩展**：可以轻松添加新字段
- ✅ **多语言支持**：JSON格式支持所有主流语言
- ✅ **云平台对接**：与主流IoT云平台格式兼容

## 8. 最佳实践

### 8.1 消息设计建议

1. **保持简洁**：避免过深的嵌套结构
2. **字段命名**：使用驼峰命名法，保持一致性
3. **数据类型**：使用合适的数据类型，避免字符串表示数字
4. **时间戳**：统一使用毫秒级时间戳

### 8.2 性能优化

1. **批量上报**：可以在params中包含多个数据点
2. **压缩传输**：对于大数据量可以考虑gzip压缩
3. **缓存机制**：客户端可以缓存消息，批量发送

### 8.3 错误处理

1. **格式验证**：确保JSON格式正确
2. **字段检查**：验证必填字段是否存在
3. **异常处理**：提供详细的错误信息

## 9. 迁移指南

### 9.1 从二进制协议迁移

1. **保持兼容**：可以同时支持两种协议
2. **逐步迁移**：按设备类型逐步迁移
3. **测试验证**：充分测试新协议的稳定性

### 9.2 配置变更

```java
// 在设备配置中指定编解码器类型
device.setCodecType("TCP_JSON");
```

这样就完成了TCP协议向JSON格式的升级，提供了更好的可读性、扩展性和兼容性。
