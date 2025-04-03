# IOT 组件使用说明

## 组件介绍

该模块包含多个 IoT 设备连接组件，提供不同的通信协议支持：

- `yudao-module-iot-component-core`: 核心接口和通用类
- `yudao-module-iot-component-http`: 基于 HTTP 协议的设备通信组件
- `yudao-module-iot-component-emqx`: 基于 MQTT/EMQX 的设备通信组件

## 组件架构

### 架构设计

各组件采用统一的架构设计和命名规范：

- 配置类: `IotComponentXxxAutoConfiguration` - 提供Bean定义和组件初始化逻辑
- 属性类: `IotComponentXxxProperties` - 定义组件的配置属性
- 下行接口: `*DownstreamHandler` - 处理从平台到设备的下行通信
- 上行接口: `*UpstreamServer` - 处理从设备到平台的上行通信

### Bean 命名规范

为避免 Bean 冲突，各个组件中的 Bean 已添加特定前缀：

- HTTP 组件: `httpDeviceUpstreamServer`, `httpDeviceDownstreamHandler`
- EMQX 组件: `emqxDeviceUpstreamServer`, `emqxDeviceDownstreamHandler`

### 组件启用规则

现在系统支持同时使用多个组件，但有以下规则：

1. 当`yudao.iot.component.emqx.enabled=true`时，核心模块将优先使用EMQX组件
2. 如果同时启用了多个组件，需要在业务代码中使用`@Qualifier`指定要使用的具体实现

> **重要提示：**
> 1. 组件库内部的默认配置文件**不会**被自动加载。必须将上述配置添加到主应用的配置文件中。
> 2. 所有配置项现在都已增加空值处理，配置缺失时将使用合理的默认值
> 3. `mqtt-host` 是唯一必须配置的参数，其他参数均有默认值
> 4. `mqtt-ssl` 和 `auth-port` 缺失时的默认值分别为 `false` 和 `8080`
> 5. `mqtt-topics` 缺失时将使用默认主题 `/device/#`

### 如何引用特定的 Bean

在其他组件中引用这些 Bean 时，需要使用 `@Qualifier` 注解指定 Bean 名称：

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import cn.iocoder.yudao.module.iot.component.core.downstream.IotDeviceDownstreamHandler;

@Service
public class YourServiceClass {

    // 注入 HTTP 组件的下行处理器
    @Autowired
    @Qualifier("httpDeviceDownstreamHandler")
    private IotDeviceDownstreamHandler httpDeviceDownstreamHandler;

    // 注入 EMQX 组件的下行处理器
    @Autowired
    @Qualifier("emqxDeviceDownstreamHandler")
    private IotDeviceDownstreamHandler emqxDeviceDownstreamHandler;

    // 使用示例
    public void example() {
        // 使用 HTTP 组件
        httpDeviceDownstreamHandler.invokeDeviceService(...);

        // 使用 EMQX 组件
        emqxDeviceDownstreamHandler.invokeDeviceService(...);
    }
}
```

### 组件选择指南

- **HTTP 组件**：适用于简单场景，设备通过 HTTP 接口与平台通信
- **EMQX 组件**：适用于实时性要求高的场景，基于 MQTT 协议，支持发布/订阅模式

## 常见问题

### 1. 配置未加载问题

如果遇到以下日志：

```
MQTT配置: host=null, port=null, username=null, ssl=null
[connectMqtt][MQTT Host为null，无法连接]
```

这表明配置没有被正确加载。请确保：

1. 在主应用的配置文件中（如 `application.yml` 或 `application-dev.yml`）添加了必要的 EMQX 配置
2. 配置前缀正确：`yudao.iot.component.emqx`
3. 配置了必要的 `mqtt-host` 属性

### 2. mqttSsl 空指针异常

如果遇到以下错误：

```
Cannot invoke "java.lang.Boolean.booleanValue()" because the return value of "cn.iocoder.yudao.module.iot.component.emqx.config.IotEmqxComponentProperties.getMqttSsl()" is null
```

此问题已通过代码修复，现在会自动使用默认值 `false`。同样适用于其他配置项的空值问题。

### 3. authPort 空指针异常

如果遇到以下错误：

```
Cannot invoke "java.lang.Integer.intValue()" because the return value of "cn.iocoder.yudao.module.iot.component.emqx.config.IotEmqxComponentProperties.getAuthPort()" is null
```

此问题已通过代码修复，现在会自动使用默认值 `8080`。

### 4. Bean注入问题

如果遇到以下错误：

```
Parameter 1 of method deviceDownstreamServer in IotPluginCommonAutoConfiguration required a single bean, but 2 were found
```

此问题已通过修改核心配置类来解决。现在系统会根据组件的启用状态自动选择合适的实现：

1. 优先使用EMQX组件（当`yudao.iot.component.emqx.enabled=true`时）
2. 如果EMQX未启用，则使用HTTP组件（当`yudao.iot.component.http.enabled=true`时）

如果需要同时使用两个组件，业务代码中必须使用`@Qualifier`明确指定要使用的Bean。

### 5. 使用默认配置

组件现已加入完善的默认配置和空值处理机制，使配置更加灵活。但需要注意的是，这些默认配置值必须通过在主应用配置文件中设置相应的属性才能生效。

// TODO 芋艿：后续继续完善 README.md