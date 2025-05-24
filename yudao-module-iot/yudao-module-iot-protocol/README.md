# IoT 协议模块 (yudao-module-iot-protocol)

## 概述

本模块是物联网协议处理的核心组件，提供统一的协议解析、转换和消息处理功能。作为 `yudao-module-iot-biz` 和
`yudao-module-iot-gateway-server` 等模块的共享包，实现了协议层面的抽象和统一。

## 主要功能

### 1. 协议消息模型

- **IotMqttMessage**: 基于 MQTT 协议规范的标准消息模型（默认实现）
- **IotStandardResponse**: 统一的响应格式，支持 MQTT 和 HTTP 协议

### 2. 主题管理

- **IotTopicConstants**: 主题常量定义
- **IotTopicUtils**: MQTT 主题构建和解析工具
- **IotHttpTopicUtils**: HTTP 主题构建和解析工具
- **IotTopicParser**: 高级主题解析器，支持提取设备信息、消息类型等

### 3. 协议转换

- **IotMessageParser**: 消息解析器接口
- **IotMqttMessageParser**: MQTT 协议解析器实现（默认）
- **IotHttpMessageParser**: HTTP 协议解析器实现
- **IotProtocolConverter**: 协议转换器接口
- **DefaultIotProtocolConverter**: 默认协议转换器实现

### 4. 枚举定义

- **IotProtocolTypeEnum**: 协议类型枚举
- **IotMessageTypeEnum**: 消息类型枚举
- **IotMessageDirectionEnum**: 消息方向枚举

## 使用示例

### 1. 构建主题

#### MQTT 主题

```java
// 构建设备属性设置主题
String topic = IotTopicUtils.buildPropertySetTopic("productKey", "deviceName");
// 结果: /sys/productKey/deviceName/thing/service/property/set

// 构建事件上报主题
String eventTopic = IotTopicUtils.buildEventPostTopic("productKey", "deviceName", "temperature");
// 结果: /sys/productKey/deviceName/thing/event/temperature/post

// 获取响应主题
String replyTopic = IotTopicUtils.getReplyTopic(topic);
// 结果: /sys/productKey/deviceName/thing/service/property/set_reply
```

#### HTTP 主题

```java
// 构建属性设置路径
String propSetPath = IotHttpTopicUtils.buildPropertySetPath("productKey", "deviceName");
// 结果: /topic/sys/productKey/deviceName/thing/service/property/set

// 构建属性获取路径
String propGetPath = IotHttpTopicUtils.buildPropertyGetPath("productKey", "deviceName");
// 结果: /topic/sys/productKey/deviceName/thing/service/property/get

// 构建事件上报路径
String eventPath = IotHttpTopicUtils.buildEventPostPath("productKey", "deviceName", "alarm");
// 结果: /topic/sys/productKey/deviceName/thing/event/alarm/post

// 构建自定义主题路径
String customPath = IotHttpTopicUtils.buildCustomTopicPath("productKey", "deviceName", "user/get");
// 结果: /topic/productKey/deviceName/user/get
```

### 2. 解析主题

```java
// 解析 MQTT 主题信息
IotTopicParser.TopicInfo info = IotTopicParser.parse("/sys/pk/dn/thing/service/property/set");
System.out.

println("产品Key: "+info.getProductKey()); // pk
        System.out.

println("设备名称: "+info.getDeviceName()); // dn
        System.out.

println("消息类型: "+info.getMessageType()); // PROPERTY_SET
        System.out.

println("消息方向: "+info.getDirection()); // DOWNSTREAM

// 解析 HTTP 主题信息
String httpPath = "/topic/sys/pk/dn/thing/service/property/set";
String actualTopic = IotHttpTopicUtils.extractActualTopic(httpPath);  // /sys/pk/dn/thing/service/property/set
String productKey = IotHttpTopicUtils.parseProductKeyFromTopic(actualTopic);  // pk  
String deviceName = IotHttpTopicUtils.parseDeviceNameFromTopic(actualTopic);  // dn
```

### 3. 创建 MQTT 消息

```java
// 创建属性设置消息
Map<String, Object> properties = new HashMap<>();
properties.

put("temperature",25.5);

IotMqttMessage message = IotMqttMessage.createPropertySetMessage("123456", properties);

// 转换为 JSON 字符串
String json = message.toJsonString();
```

### 4. HTTP 协议消息处理

#### HTTP 消息格式

```json
{
  "deviceKey": "productKey/deviceName",
  "messageId": "123456",
  "action": "property.set",
  "version": "1.0",
  "data": {
    "temperature": 25.5,
    "humidity": 60.0
  }
}
```

#### 使用 HTTP 协议解析器

```java
// 创建 HTTP 协议解析器
IotHttpMessageParser httpParser = new IotHttpMessageParser();

// 解析 HTTP 消息
String topic = "/topic/sys/productKey/deviceName/thing/service/property/set";
byte[] payload = httpMessage.getBytes(StandardCharsets.UTF_8);
IotMqttMessage message = httpParser.parse(topic, payload);

// 格式化 HTTP 响应
IotStandardResponse response = IotStandardResponse.success("123456", "property.set", data);
byte[] responseBytes = httpParser.formatResponse(response);
```

### 5. 使用协议转换器

```java

@Autowired
private IotProtocolConverter protocolConverter;

// 转换 MQTT 消息（推荐使用）
IotMqttMessage mqttMessage = protocolConverter.convertToStandardMessage(mqttTopic, mqttPayload, "mqtt");

// 转换 HTTP 消息
IotMqttMessage httpMessage = protocolConverter.convertToStandardMessage(httpTopic, httpPayload, "http");

// 创建响应
IotStandardResponse response = IotStandardResponse.success("123456", "property.set", data);
byte[] responseBytes = protocolConverter.convertFromStandardResponse(response, "mqtt");
```

### 6. 自定义协议解析器

```java

@Component
public class CustomMessageParser implements IotMessageParser {

    @Override
    public IotMqttMessage parse(String topic, byte[] payload) {
        // 实现自定义协议解析逻辑
        return null;
    }

    @Override
    public byte[] formatResponse(IotStandardResponse response) {
        // 实现自定义响应格式化逻辑
        return new byte[0];
    }

    @Override
    public boolean canHandle(String topic) {
        // 判断是否能处理该主题
        return topic.startsWith("/custom/");
    }
}

// 注册到协议转换器
@Autowired
private DefaultIotProtocolConverter converter;

@PostConstruct
public void init() {
    converter.registerParser("custom", new CustomMessageParser());
}
```

## 支持的协议类型

- **MQTT**: 标准 MQTT 协议，支持设备属性、事件、服务调用（默认协议）
- **HTTP**: HTTP 协议，支持设备通过 HTTP API 进行通信
- **MQTT_RAW**: MQTT 原始协议
- **TCP**: TCP 协议
- **UDP**: UDP 协议
- **CUSTOM**: 自定义协议

## 协议对比

| 协议类型     | 传输方式 | 消息格式 | 主题格式                                                                                                                       | 适用场景          |
|----------|------|------|----------------------------------------------------------------------------------------------------------------------------|---------------|
| MQTT     | MQTT | JSON | `/sys/{productKey}/{deviceName}/...`<br/>`/mqtt/{productKey}/{deviceName}/...`<br/>`/device/{productKey}/{deviceName}/...` | 实时性要求高的设备（推荐） |
| HTTP     | HTTP | JSON | `/topic/sys/{productKey}/{deviceName}/...`<br/>`/topic/{productKey}/{deviceName}/...`                                      | 间歇性通信的设备      |
| MQTT_RAW | MQTT | 原始   | 自定义格式                                                                                                                      | 特殊协议设备        |

## 模块依赖

本模块是一个基础模块，依赖项最小化：

- `yudao-common`: 基础工具类
- `hutool-all`: 工具库
- `lombok`: 简化代码
- `spring-boot-starter`: Spring Boot 基础支持

## 扩展点

### 1. 自定义消息解析器

实现 `IotMessageParser` 接口，支持新的协议格式。

### 2. 自定义协议转换器

实现 `IotProtocolConverter` 接口，提供更复杂的转换逻辑。

### 3. 自定义主题格式

扩展 `IotTopicParser` 的 `parseCustomTopic` 方法，支持自定义主题格式。

## 注意事项

1. 本模块设计为无状态的工具模块，避免引入有状态的组件
2. 所有的工具类都采用静态方法，便于直接调用
3. 异常处理采用返回 null 的方式，调用方需要做好空值检查
4. 日志级别建议设置为 INFO 或 WARN，避免过多的 DEBUG 日志
5. HTTP 协议解析器使用设备标识 `deviceKey`（格式：`productKey/deviceName`）来标识设备

## 版本更新

- v1.0.0: 基础功能实现，支持 MQTT 协议和 HTTP 协议支持
- 后续版本将支持更多协议类型和高级功能 