package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * IoT 设备【消息】的工具类
 *
 * @author 芋道源码
 */
public class IotDeviceMessageUtils {

    // ========== Message 相关 ==========

    public static String generateMessageId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 是否是上行消息：由设备发送
     *
     * @param message 消息
     * @return 是否
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    public static boolean isUpstreamMessage(IotDeviceMessage message) {
        IotDeviceMessageMethodEnum methodEnum = IotDeviceMessageMethodEnum.of(message.getMethod());
        Assert.notNull(methodEnum, "无法识别的消息方法：" + message.getMethod());
        // 注意：回复消息时，需要取反
        return !isReplyMessage(message) ? methodEnum.getUpstream() : !methodEnum.getUpstream();
    }

    /**
     * 是否是回复消息，通过 {@link IotDeviceMessage#getCode()} 非空进行识别
     *
     * @param message 消息
     * @return 是否
     */
    public static boolean isReplyMessage(IotDeviceMessage message) {
        return message.getCode() != null;
    }

    /**
     * 提取消息中的标识符
     *
     * @param message 消息
     * @return 标识符
     */
    public static String getIdentifier(IotDeviceMessage message) {
        if (message == null || message.getParams() == null) {
            return null;
        }
        Object params = message.getParams();
        if (StrUtil.equalsAny(message.getMethod(), IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod())) {
            return StrUtil.toStringOrNull(readField(params, "identifier"));
        } else if (StrUtil.equalsAny(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            return StrUtil.toStringOrNull(readField(params, "state"));
        }
        return null;
    }

    /**
     * 从 params 中读取字段值，兼容 Map 和 POJO（DTO）两种形态
     *
     * Why：MQ 消息经 JSON 反序列化后 params 是 Map，但本地总线场景 producer 可能直接传 DTO 对象（如 IotDeviceEventPostReqDTO），
     * matcher 必须同时支持两种形态，避免事件触发器在同 JVM 内部消息总线下匹配失败
     */
    private static Object readField(Object params, String fieldName) {
        if (params == null) {
            return null;
        }
        if (params instanceof Map) {
            return ((Map<?, ?>) params).get(fieldName);
        }
        // 跳过 JDK 内置类型，避免反射读取到内部字段（例如 JDK8 下 String#value 会返回 char[]）
        if (ClassUtil.isJdkClass(params.getClass())) {
            return null;
        }
        try {
            return ReflectUtil.getFieldValue(params, fieldName);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取属性上报消息中包含的所有属性标识符
     *
     * 仅支持扁平结构：{ temperature: 25.5, humidity: 60 }，顶层 key 即属性标识符
     *
     * @param message 设备消息
     * @return 属性标识符集合，不为 null
     */
    public static Set<String> getPropertyIdentifiers(IotDeviceMessage message) {
        if (message == null) {
            return new LinkedHashSet<>();
        }
        Map<String, Object> params = parseParamsToMap(message.getParams());
        if (params == null) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(params.keySet());
    }

    /**
     * 判断消息中是否包含指定的标识符
     * <p>
     * 对于不同消息类型的处理：
     * - EVENT_POST/SERVICE_INVOKE：检查 params.identifier 是否匹配
     * - STATE_UPDATE：检查 params.state 是否匹配
     * - PROPERTY_POST：检查 params 中是否包含该属性 key
     *
     * @param message    消息
     * @param identifier 要检查的标识符
     * @return 是否包含
     */
    @SuppressWarnings("unchecked")
    public static boolean containsIdentifier(IotDeviceMessage message, String identifier) {
        if (message == null || message.getParams() == null || StrUtil.isBlank(identifier)) {
            return false;
        }
        // EVENT_POST / SERVICE_INVOKE / STATE_UPDATE：使用原有逻辑
        String messageIdentifier = getIdentifier(message);
        if (messageIdentifier != null) {
            return identifier.equals(messageIdentifier);
        }
        // PROPERTY_POST：检查 params 中是否包含该属性 key（支持扁平和嵌套 properties 结构）
        if (StrUtil.equals(message.getMethod(), IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())) {
            Map<String, Object> params = parseParamsToMap(message.getParams());
            if (params == null) {
                return false;
            }
            if (params.containsKey(identifier)) {
                return true;
            }
            Object properties = params.get("properties");
            return properties instanceof Map && ((Map<String, Object>) properties).containsKey(identifier);
        }
        return false;
    }

    /**
     * 判断消息中是否不包含指定的标识符
     *
     * @param message    消息
     * @param identifier 要检查的标识符
     * @return 是否不包含
     */
    public static boolean notContainsIdentifier(IotDeviceMessage message, String identifier) {
        return !containsIdentifier(message, identifier);
    }

    /**
     * 将 params 解析为 Map
     *
     * @param params 参数（可能是 Map 或 JSON 字符串）
     * @return Map，解析失败返回 null
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseParamsToMap(Object params) {
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        if (params instanceof String) {
            try {
                return JsonUtils.parseObject((String) params, Map.class);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 从设备消息中提取指定标识符的属性值
     * <p>
     * 支持的提取策略（按优先级顺序）：
     * 1. 直接值：如果 params 不是 Map，直接返回该值（适用于简单消息）
     * 2. 标识符字段：从 params[identifier] 获取
     * 3. properties 结构：从 params.properties[identifier] 获取（标准属性上报）
     * 4. data 结构：从 params.data[identifier] 获取
     * 5. value 字段：从 params.value 获取（单值消息）
     * 6. 单值 Map：如果 Map 只包含 identifier 和一个值，返回该值
     *
     * @param message    设备消息
     * @param identifier 属性标识符
     * @return 属性值，如果未找到则返回 null
     */
    @SuppressWarnings("unchecked")
    public static Object extractPropertyValue(IotDeviceMessage message, String identifier) {
        Object params = message != null ? message.getParams() : null;
        if (params == null) {
            return null;
        }

        // 策略 1：如果 params 不是 Map，直接返回该值（适用于简单的单属性消息）
        if (!(params instanceof Map)) {
            return params;
        }

        // 策略 2：直接通过标识符获取属性值
        Map<String, Object> paramsMap = (Map<String, Object>) params;
        Object directValue = paramsMap.get(identifier);
        if (directValue != null) {
            return directValue;
        }

        // 策略 3：从 properties 字段中获取（适用于标准属性上报消息）
        Object properties = paramsMap.get("properties");
        if (properties instanceof Map) {
            Map<String, Object> propertiesMap = (Map<String, Object>) properties;
            Object propertyValue = propertiesMap.get(identifier);
            if (propertyValue != null) {
                return propertyValue;
            }
        }

        // 策略 4：从 data 字段中获取（适用于某些消息格式）
        Object data = paramsMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object dataValue = dataMap.get(identifier);
            if (dataValue != null) {
                return dataValue;
            }
        }

        // 策略 5：从 value 字段中获取（适用于单值消息）
        Object value = paramsMap.get("value");
        if (value != null) {
            return value;
        }

        // 策略 6：如果 Map 只有两个字段且包含 identifier，返回另一个字段的值
        if (paramsMap.size() == 2 && paramsMap.containsKey("identifier")) {
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                if (!"identifier".equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        // 未找到对应的属性值
        return null;
    }

    /**
     * 从设备事件上报消息中提取事件值
     * <p>
     * 事件上报的 params 结构为：{"identifier": "xxx", "value": ...}，事件值即 value 字段。
     * value 可能是标量（字符串/数字/布尔），也可能是结构体（如告警事件 {level, message}）
     *
     * @param message 设备消息
     * @return 事件值，如果未找到则返回 null
     */
    public static Object extractEventValue(IotDeviceMessage message) {
        return readField(message != null ? message.getParams() : null, "value");
    }

    /**
     * 从服务调用消息中提取输入参数
     * <p>
     * 服务调用消息的 params 结构通常为：
     * {
     *     "identifier": "serviceIdentifier",
     *     "inputData": { ... } 或 "inputParams": { ... }
     * }
     *
     * @param message 设备消息
     * @return 输入参数 Map，如果未找到则返回 null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractServiceInputParams(IotDeviceMessage message) {
        if (message == null || message.getParams() == null) {
            return null;
        }
        Object params = message.getParams();
        // 兼容 Map 和 POJO（如 IotDeviceServiceInvokeReqDTO）两种 params 形态
        Object inputData = readField(params, "inputData");
        if (inputData instanceof Map) {
            return (Map<String, Object>) inputData;
        }
        Object inputParams = readField(params, "inputParams");
        if (inputParams instanceof Map) {
            return (Map<String, Object>) inputParams;
        }
        return null;
    }

    // ========== Topic 相关 ==========

    public static String buildMessageBusGatewayDeviceMessageTopic(String serverId) {
        return String.format(IotDeviceMessage.MESSAGE_BUS_GATEWAY_DEVICE_MESSAGE_TOPIC, serverId);
    }

    /**
     * 生成服务器编号
     *
     * @param serverPort 服务器端口
     * @return 服务器编号
     */
    public static String generateServerId(Integer serverPort) {
        String serverId = String.format("%s.%d", SystemUtil.getHostInfo().getAddress(), serverPort);
        // 避免一些场景无法使用 . 符号，例如说 RocketMQ Topic
        return serverId.replaceAll("\\.", "_");
    }

}