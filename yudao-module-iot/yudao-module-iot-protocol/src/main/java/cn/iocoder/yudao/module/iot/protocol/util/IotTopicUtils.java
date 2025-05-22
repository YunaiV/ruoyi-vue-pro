package cn.iocoder.yudao.module.iot.protocol.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.protocol.constants.IotTopicConstants;

/**
 * IoT 主题工具类
 * <p>
 * 用于构建和解析设备主题
 *
 * @author haohao
 */
public class IotTopicUtils {

    /**
     * 构建设备服务调用主题
     *
     * @param productKey        产品Key
     * @param deviceName        设备名称
     * @param serviceIdentifier 服务标识符
     * @return 完整的主题路径
     */
    public static String buildServiceTopic(String productKey, String deviceName, String serviceIdentifier) {
        return buildDeviceBaseTopic(productKey, deviceName) +
                IotTopicConstants.SERVICE_TOPIC_PREFIX + serviceIdentifier;
    }

    /**
     * 构建设备属性设置主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertySetTopic(String productKey, String deviceName) {
        return buildDeviceBaseTopic(productKey, deviceName) + IotTopicConstants.PROPERTY_SET_TOPIC;
    }

    /**
     * 构建设备属性获取主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyGetTopic(String productKey, String deviceName) {
        return buildDeviceBaseTopic(productKey, deviceName) + IotTopicConstants.PROPERTY_GET_TOPIC;
    }

    /**
     * 构建设备配置设置主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildConfigSetTopic(String productKey, String deviceName) {
        return buildDeviceBaseTopic(productKey, deviceName) + IotTopicConstants.CONFIG_SET_TOPIC;
    }

    /**
     * 构建设备 OTA 升级主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildOtaUpgradeTopic(String productKey, String deviceName) {
        return buildDeviceBaseTopic(productKey, deviceName) + IotTopicConstants.OTA_UPGRADE_TOPIC;
    }

    /**
     * 构建设备属性上报主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyPostTopic(String productKey, String deviceName) {
        return buildDeviceBaseTopic(productKey, deviceName) + IotTopicConstants.PROPERTY_POST_TOPIC;
    }

    /**
     * 构建设备事件上报主题
     *
     * @param productKey      产品Key
     * @param deviceName      设备名称
     * @param eventIdentifier 事件标识符
     * @return 完整的主题路径
     */
    public static String buildEventPostTopic(String productKey, String deviceName, String eventIdentifier) {
        return buildDeviceBaseTopic(productKey, deviceName) +
                IotTopicConstants.EVENT_POST_TOPIC_PREFIX + eventIdentifier + IotTopicConstants.EVENT_POST_TOPIC_SUFFIX;
    }

    /**
     * 获取响应主题
     *
     * @param requestTopic 请求主题
     * @return 响应主题
     */
    public static String getReplyTopic(String requestTopic) {
        return requestTopic + IotTopicConstants.REPLY_SUFFIX;
    }

    /**
     * 构建设备基础主题
     * 格式: /sys/${productKey}/${deviceName}
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 设备基础主题
     */
    public static String buildDeviceBaseTopic(String productKey, String deviceName) {
        return IotTopicConstants.SYS_TOPIC_PREFIX + productKey + "/" + deviceName;
    }

    /**
     * 从主题中解析产品Key
     * 格式: /sys/${productKey}/${deviceName}/...
     *
     * @param topic 主题
     * @return 产品Key，如果无法解析则返回null
     */
    public static String parseProductKeyFromTopic(String topic) {
        if (StrUtil.isBlank(topic) || !topic.startsWith(IotTopicConstants.SYS_TOPIC_PREFIX)) {
            return null;
        }
        
        String[] parts = topic.split("/");
        if (parts.length < 4) {
            return null;
        }
        
        return parts[2];
    }

    /**
     * 从主题中解析设备名称
     * 格式: /sys/${productKey}/${deviceName}/...
     *
     * @param topic 主题
     * @return 设备名称，如果无法解析则返回null
     */
    public static String parseDeviceNameFromTopic(String topic) {
        if (StrUtil.isBlank(topic) || !topic.startsWith(IotTopicConstants.SYS_TOPIC_PREFIX)) {
            return null;
        }
        
        String[] parts = topic.split("/");
        if (parts.length < 4) {
            return null;
        }
        
        return parts[3];
    }

    /**
     * 从主题中解析方法名
     * 例如：从 /sys/pk/dn/thing/service/property/set 解析出 property.set
     *
     * @param topic 主题
     * @return 方法名，如果无法解析则返回null
     */
    public static String parseMethodFromTopic(String topic) {
        if (StrUtil.isBlank(topic) || !topic.startsWith(IotTopicConstants.SYS_TOPIC_PREFIX)) {
            return null;
        }
        
        // 服务调用主题
        if (topic.contains("/thing/service/")) {
            String servicePart = topic.substring(topic.indexOf("/thing/service/") + "/thing/service/".length());
            return servicePart.replace("/", ".");
        }
        
        // 事件上报主题
        if (topic.contains("/thing/event/")) {
            String eventPart = topic.substring(topic.indexOf("/thing/event/") + "/thing/event/".length());
            return "event." + eventPart.replace("/", ".");
        }
        
        return null;
    }
} 