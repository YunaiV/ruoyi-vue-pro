package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

import java.util.Map;

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
    @SuppressWarnings("unchecked")
    public static String getIdentifier(IotDeviceMessage message) {
        if (message.getParams() == null) {
            return null;
        }
        if (StrUtil.equalsAny(message.getMethod(), IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod())) {
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            return MapUtil.getStr(params, "identifier");
        }  else if (StrUtil.equalsAny(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            return MapUtil.getStr(params, "state");
        }
        return null;
    }

    /**
     * 从设备消息中提取指定标识符的属性值
     * - 支持多种消息格式和属性值提取策略
     * - 兼容现有的消息结构
     * - 提供统一的属性值提取接口
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
        Object params = message.getParams();
        if (params == null) {
            return null;
        }

        // 策略1：如果 params 不是 Map，直接返回该值（适用于简单的单属性消息）
        if (!(params instanceof Map)) {
            return params;
        }

        Map<String, Object> paramsMap = (Map<String, Object>) params;

        // 策略2：直接通过标识符获取属性值
        Object directValue = paramsMap.get(identifier);
        if (directValue != null) {
            return directValue;
        }

        // 策略3：从 properties 字段中获取（适用于标准属性上报消息）
        Object properties = paramsMap.get("properties");
        if (properties instanceof Map) {
            Map<String, Object> propertiesMap = (Map<String, Object>) properties;
            Object propertyValue = propertiesMap.get(identifier);
            if (propertyValue != null) {
                return propertyValue;
            }
        }

        // 策略4：从 data 字段中获取（适用于某些消息格式）
        Object data = paramsMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object dataValue = dataMap.get(identifier);
            if (dataValue != null) {
                return dataValue;
            }
        }

        // 策略5：从 value 字段中获取（适用于单值消息）
        Object value = paramsMap.get("value");
        if (value != null) {
            return value;
        }

        // 策略6：如果 Map 只有两个字段且包含 identifier，返回另一个字段的值
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