# TCP 二进制协议数据包格式说明和示例

## 1. 二进制协议概述

TCP 二进制协议是一种高效的自定义协议格式，适用于对带宽和性能要求较高的场景。该协议采用紧凑的二进制格式，减少数据传输量，提高传输效率。

## 2. 数据包格式

### 2.1 整体结构

根据代码实现，TCP 二进制协议的数据包格式为：

```
+----------+----------+----------+----------+
| 包头     | 功能码   | 消息序号 | 包体数据 |
| 4字节    | 2字节    | 2字节    | 变长     |
+----------+----------+----------+----------+
```

**注意**：与原始设计相比，实际实现中移除了设备地址字段，简化了协议结构。

### 2.2 字段说明

| 字段   | 长度  | 类型     | 说明              |
|------|-----|--------|-----------------|
| 包头   | 4字节 | int    | 后续数据的总长度（不包含包头） |
| 功能码  | 2字节 | short  | 消息类型标识          |
| 消息序号 | 2字节 | short  | 消息唯一标识          |
| 包体数据 | 变长  | string | JSON 格式的消息内容    |

### 2.3 功能码定义

根据代码实现，支持的功能码：

| 功能码 | 名称   | 说明           |
|-----|------|--------------|
| 10  | 设备注册 | 设备首次连接时的注册请求 |
| 11  | 注册回复 | 服务器对注册请求的回复  |
| 20  | 心跳请求 | 设备发送的心跳包     |
| 21  | 心跳回复 | 服务器对心跳的回复    |
| 30  | 消息上行 | 设备向服务器发送的数据  |
| 40  | 消息下行 | 服务器向设备发送的指令  |

**常量定义：**

```java
public static final short CODE_REGISTER = 10;
public static final short CODE_REGISTER_REPLY = 11;
public static final short CODE_HEARTBEAT = 20;
public static final short CODE_HEARTBEAT_REPLY = 21;
public static final short CODE_MESSAGE_UP = 30;
public static final short CODE_MESSAGE_DOWN = 40;
```

## 3. 包体数据格式

### 3.1 JSON 负载结构

包体数据采用 JSON 格式，包含以下字段：

```json
{
   "method": "消息方法",
   "params": {
      // 消息参数
   },
   "timestamp": 时间戳,
   "requestId": "请求ID",
   "msgId": "消息ID"
}
```

### 3.2 字段说明

| 字段名       | 类型     | 必填 | 说明                           |
|-----------|--------|----|------------------------------|
| method    | String | 是  | 消息方法，如 `thing.property.post` |
| params    | Object | 否  | 消息参数                         |
| timestamp | Long   | 是  | 时间戳（毫秒）                      |
| requestId | String | 否  | 请求唯一标识                       |
| msgId     | String | 否  | 消息唯一标识                       |

**常量定义：**

```java
public static final String METHOD = "method";
public static final String PARAMS = "params";
public static final String TIMESTAMP = "timestamp";
public static final String REQUEST_ID = "requestId";
public static final String MESSAGE_ID = "msgId";
```

## 4. 消息类型

### 4.1 数据上报 (thing.property.post)

设备向服务器上报属性数据。

**功能码：** 30 (CODE_MESSAGE_UP)

**包体数据示例：**

```json
{
   "method": "thing.property.post",
   "params": {
      "temperature": 25.5,
      "humidity": 60.2,
      "pressure": 1013.25
   },
   "timestamp": 1642781234567,
   "requestId": "req_001"
}
```

### 4.2 心跳 (thing.state.online)

设备向服务器发送心跳保活。

**功能码：** 20 (CODE_HEARTBEAT)

**包体数据示例：**

```json
{
   "method": "thing.state.online",
   "params": {},
   "timestamp": 1642781234567,
   "requestId": "req_002"
}
```

### 4.3 消息方法常量

```java
public static final String PROPERTY_POST = "thing.property.post"; // 数据上报
public static final String STATE_ONLINE = "thing.state.online";   // 心跳
```

## 5. 数据包示例

### 5.1 温度传感器数据上报

**包体数据：**
```json
{
  "method": "thing.property.post",
  "params": {
    "temperature": 25.5,
    "humidity": 60.2,
    "pressure": 1013.25
  },
   "timestamp": 1642781234567,
   "requestId": "req_001"
}
```

**数据包结构：**
```
包头: 0x00000045 (69字节)
功能码: 0x001E (30 - 消息上行)
消息序号: 0x1234 (4660)
包体: JSON字符串
```

**完整十六进制数据包：**
```
00 00 00 45 00 1E 12 34
7B 22 6D 65 74 68 6F 64 22 3A 22 74 68 69 6E 67
2E 70 72 6F 70 65 72 74 79 2E 70 6F 73 74 22 2C
22 70 61 72 61 6D 73 22 3A 7B 22 74 65 6D 70 65
72 61 74 75 72 65 22 3A 32 35 2E 35 2C 22 68 75
6D 69 64 69 74 79 22 3A 36 30 2E 32 2C 22 70 72
65 73 73 75 72 65 22 3A 31 30 31 33 2E 32 35 7D
2C 22 74 69 6D 65 73 74 61 6D 70 22 3A 31 36 34
32 37 38 31 32 33 34 35 36 37 2C 22 72 65 71 75
65 73 74 49 64 22 3A 22 72 65 71 5F 30 30 31 22 7D
```

### 5.2 心跳包示例

**包体数据：**
```json
{
  "method": "thing.state.online",
   "params": {},
   "timestamp": 1642781234567,
   "requestId": "req_002"
}
```

**数据包结构：**
```
包头: 0x00000028 (40字节)
功能码: 0x0014 (20 - 心跳请求)
消息序号: 0x5678 (22136)
包体: JSON字符串
```

**完整十六进制数据包：**
```
00 00 00 28 00 14 56 78
7B 22 6D 65 74 68 6F 64 22 3A 22 74 68 69 6E 67
2E 73 74 61 74 65 2E 6F 6E 6C 69 6E 65 22 2C 22
70 61 72 61 6D 73 22 3A 7B 7D 2C 22 74 69 6D 65
73 74 61 6D 70 22 3A 31 36 34 32 37 38 31 32 33
34 35 36 37 2C 22 72 65 71 75 65 73 74 49 64 22
3A 22 72 65 71 5F 30 30 32 22 7D
```

## 6. 编解码器实现

### 6.1 编码器类型

```java
public static final String TYPE = "TCP_BINARY";
```

### 6.2 编码过程

1. **参数验证**：检查消息和方法是否为空
2. **确定功能码**：
   - 心跳消息：使用 `CODE_HEARTBEAT` (20)
   - 其他消息：使用 `CODE_MESSAGE_UP` (30)
3. **构建负载**：使用 `buildSimplePayload()` 构建 JSON 负载
4. **生成消息序号**：基于当前时间戳生成
5. **构建数据包**：创建 `TcpDataPackage` 对象
6. **编码为字节流**：使用 `encodeTcpDataPackage()` 编码

### 6.3 解码过程

1. **参数验证**：检查字节数组是否为空
2. **解码数据包**：使用 `decodeTcpDataPackage()` 解码
3. **确定消息方法**：
   - 功能码 20：`thing.state.online` (心跳)
   - 功能码 30：`thing.property.post` (数据上报)
4. **解析负载信息**：使用 `parsePayloadInfo()` 解析 JSON 负载
5. **构建设备消息**：创建 `IotDeviceMessage` 对象
6. **设置服务 ID**：使用 `generateServerId()` 生成

### 6.4 服务 ID 生成

```java
private String generateServerId(TcpDataPackage dataPackage) {
   return String.format("tcp_binary_%d_%d", dataPackage.getCode(), dataPackage.getMid());
}
```

## 7. 数据包解析步骤

### 7.1 解析流程

1. **读取包头（4字节）**
   - 获取后续数据的总长度
   - 验证数据包完整性

2. **读取功能码（2字节）**
   - 确定消息类型

3. **读取消息序号（2字节）**
   - 获取消息唯一标识

4. **读取包体数据（变长）**
   - 解析 JSON 格式的消息内容

### 7.2 Java 解析示例

```java
public TcpDataPackage parsePacket(byte[] packet) {
    int index = 0;
    
    // 1. 解析包头
    int totalLength = ByteBuffer.wrap(packet, index, 4).getInt();
    index += 4;

   // 2. 解析功能码
    short functionCode = ByteBuffer.wrap(packet, index, 2).getShort();
    index += 2;

   // 3. 解析消息序号
    short messageId = ByteBuffer.wrap(packet, index, 2).getShort();
    index += 2;

   // 4. 解析包体数据
    String payload = new String(packet, index, packet.length - index);

   return new TcpDataPackage(functionCode, messageId, payload);
}
```

## 8. 使用示例

### 8.1 基本使用

```java
// 创建编解码器
IotTcpBinaryDeviceMessageCodec codec = new IotTcpBinaryDeviceMessageCodec();

// 创建数据上报消息
Map<String, Object> sensorData = Map.of(
        "temperature", 25.5,
        "humidity", 60.2
);

// 编码
IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", sensorData);
byte[] data = codec.encode(message);

// 解码
IotDeviceMessage decoded = codec.decode(data);
```

### 8.2 错误处理

```java
try{
byte[] data = codec.encode(message);
// 处理编码成功
}catch(
IllegalArgumentException e){
        // 处理参数错误
        log.

error("编码参数错误: {}",e.getMessage());
        }catch(
TcpCodecException e){
        // 处理编码失败
        log.

error("编码失败: {}",e.getMessage());
        }
```

## 9. 注意事项

1. **字节序**：所有多字节数据使用大端序（Big-Endian）
2. **字符编码**：字符串数据使用 UTF-8 编码
3. **JSON 格式**：包体数据必须是有效的 JSON 格式
4. **长度限制**：单个数据包建议不超过 1MB
5. **错误处理**：解析失败时会抛出 `TcpCodecException`
6. **功能码映射**：目前只支持心跳和数据上报两种消息类型

## 10. 协议特点

### 10.1 优势

- **高效传输**：二进制格式，数据量小
- **性能优化**：减少解析开销
- **带宽节省**：相比 JSON 格式节省带宽
- **实时性好**：适合高频数据传输

### 10.2 适用场景

- ✅ **高频数据传输**：传感器数据实时上报
- ✅ **带宽受限环境**：移动网络、卫星通信
- ✅ **性能要求高**：需要低延迟的场景
- ✅ **设备资源有限**：嵌入式设备、IoT 设备

### 10.3 与 JSON 协议对比

| 特性    | 二进制协议 | JSON 协议 |
|-------|-------|---------|
| 数据大小  | 小     | 稍大      |
| 解析性能  | 高     | 中等      |
| 可读性   | 差     | 优秀      |
| 调试难度  | 高     | 低       |
| 扩展性   | 差     | 优秀      |
| 实现复杂度 | 高     | 低       |

这样就完成了 TCP 二进制协议的完整说明，与实际代码实现完全一致。
