package cn.iocoder.yudao.module.iot.gateway.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备主题枚举
 * <p>
 * 用于统一管理 MQTT 协议中的主题常量，基于 Alink 协议规范
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotDeviceTopicEnum {

    /**
     * 设备属性设置主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/property/set
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/property/set_reply
     */
    PROPERTY_SET_TOPIC("/thing/property/set", "设备属性设置主题"),

    /**
     * 设备属性获取主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/property/get
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/property/get_reply
     */
    PROPERTY_GET_TOPIC("/thing/property/get", "设备属性获取主题"),

    /**
     * 设备配置设置主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/config/set
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/config/set_reply
     */
    CONFIG_SET_TOPIC("/thing/config/set", "设备配置设置主题"),

    /**
     * 设备 OTA 升级主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/ota/upgrade
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/ota/upgrade_reply
     */
    OTA_UPGRADE_TOPIC("/thing/ota/upgrade", "设备 OTA 升级主题"),

    /**
     * 设备属性上报主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/event/property/post
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/event/property/post_reply
     */
    PROPERTY_POST_TOPIC("/thing/property/post", "设备属性上报主题"),

    /**
     * 设备事件上报主题前缀
     */
    EVENT_POST_TOPIC_PREFIX("/thing/event/", "设备事件上报主题前缀"),

    /**
     * 设备事件上报主题后缀
     */
    EVENT_POST_TOPIC_SUFFIX("/post", "设备事件上报主题后缀");

    // ========== 静态常量 ==========

    /**
     * 系统主题前缀
     */
    public static final String SYS_TOPIC_PREFIX = "/sys/";

    /**
     * 服务调用主题前缀
     */
    public static final String SERVICE_TOPIC_PREFIX = "/thing/";

    /**
     * 响应主题后缀
     */
    public static final String REPLY_SUFFIX = "_reply";

    // ========== 方法常量 ==========

    /**
     * 服务方法前缀
     */
    public static final String SERVICE_METHOD_PREFIX = "thing.";

    /**
     * 属性服务方法前缀
     */
    public static final String PROPERTY_SERVICE_METHOD_PREFIX = "thing.property.";

    /**
     * 配置服务方法前缀
     */
    public static final String CONFIG_SERVICE_METHOD_PREFIX = "thing.config.";

    /**
     * OTA 服务方法前缀
     */
    public static final String OTA_SERVICE_METHOD_PREFIX = "thing.ota.";

    /**
     * 属性设置方法
     */
    public static final String PROPERTY_SET_METHOD = "thing.property.set";

    /**
     * 属性获取方法
     */
    public static final String PROPERTY_GET_METHOD = "thing.property.get";

    // ========== 主题匹配常量 ==========

    /**
     * 事件上报主题模式
     */
    public static final String EVENT_POST_TOPIC_PATTERN = "/thing/event/";

    /**
     * 主题后缀：post
     */
    public static final String POST_SUFFIX = "/post";

    /**
     * 属性上报主题后缀
     */
    public static final String PROPERTY_POST_SUFFIX = "/thing/property/post";

    /**
     * 属性设置响应主题包含
     */
    public static final String PROPERTY_SET_TOPIC_CONTAINS = "/thing/property/set";

    /**
     * 属性获取响应主题包含
     */
    public static final String PROPERTY_GET_TOPIC_CONTAINS = "/thing/property/get";

    // ========== MQTT 认证路径常量 ==========

    /**
     * MQTT 认证路径
     */
    public static final String MQTT_AUTH_AUTHENTICATE_PATH = "/mqtt/auth/authenticate";

    /**
     * MQTT 连接事件路径
     */
    public static final String MQTT_AUTH_CONNECTED_PATH = "/mqtt/auth/connected";

    /**
     * MQTT 断开事件路径
     */
    public static final String MQTT_AUTH_DISCONNECTED_PATH = "/mqtt/auth/disconnected";

    private final String topic;
    private final String description;

    // ========== 工具方法 ==========

    /**
     * 构建设备主题前缀
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 设备主题前缀：/sys/{productKey}/{deviceName}
     */
    private static String buildDeviceTopicPrefix(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX + productKey + "/" + deviceName;
    }

    /**
     * 构建设备服务调用主题
     *
     * @param productKey        产品 Key
     * @param deviceName        设备名称
     * @param serviceIdentifier 服务标识符
     * @return 完整的主题路径
     */
    public static String buildServiceTopic(String productKey, String deviceName, String serviceIdentifier) {
        return buildDeviceTopicPrefix(productKey, deviceName) + SERVICE_TOPIC_PREFIX + serviceIdentifier;
    }

    /**
     * 构建设备属性设置主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertySetTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + PROPERTY_SET_TOPIC.getTopic();
    }

    /**
     * 构建设备属性获取主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyGetTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + PROPERTY_GET_TOPIC.getTopic();
    }

    /**
     * 构建设备配置设置主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildConfigSetTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + CONFIG_SET_TOPIC.getTopic();
    }

    /**
     * 构建设备 OTA 升级主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildOtaUpgradeTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + OTA_UPGRADE_TOPIC.getTopic();
    }

    /**
     * 构建设备属性上报主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyPostTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + PROPERTY_POST_TOPIC.getTopic();
    }

    /**
     * 构建设备事件上报主题
     *
     * @param productKey      产品 Key
     * @param deviceName      设备名称
     * @param eventIdentifier 事件标识符
     * @return 完整的主题路径
     */
    public static String buildEventPostTopic(String productKey, String deviceName, String eventIdentifier) {
        return buildDeviceTopicPrefix(productKey, deviceName) +
                EVENT_POST_TOPIC_PREFIX.getTopic() + eventIdentifier + EVENT_POST_TOPIC_SUFFIX.getTopic();
    }

    /**
     * 获取响应主题
     *
     * @param requestTopic 请求主题
     * @return 响应主题
     */
    public static String getReplyTopic(String requestTopic) {
        return requestTopic + REPLY_SUFFIX;
    }

}