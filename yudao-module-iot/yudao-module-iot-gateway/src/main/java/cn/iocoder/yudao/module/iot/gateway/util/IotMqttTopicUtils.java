package cn.iocoder.yudao.module.iot.gateway.util;

/**
 * IoT 网关 MQTT 主题工具类
 * <p>
 * 用于统一管理 MQTT 协议中的主题常量，基于 Alink 协议规范
 *
 * @author 芋道源码
 */
public final class IotMqttTopicUtils {

    // ========== 静态常量 ==========

    /**
     * 系统主题前缀
     */
    private static final String SYS_TOPIC_PREFIX = "/sys/";

    /**
     * 服务调用主题前缀
     */
    private static final String SERVICE_TOPIC_PREFIX = "/thing/";

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
     * 构建设备属性设置主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertySetTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + "/thing/property/set";
    }

    /**
     * 构建设备属性上报回复主题
     * <p>
     * 当设备上报属性时，会收到该主题的回复
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyPostReplyTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + "/thing/property/post_reply";
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

}