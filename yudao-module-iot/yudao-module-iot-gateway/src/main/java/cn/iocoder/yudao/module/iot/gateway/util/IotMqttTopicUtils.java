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

    // ========== MQTT HTTP 接口路径常量 ==========

    /**
     * MQTT 认证接口路径
     * 对应 EMQX HTTP 认证插件的认证请求接口
     */
    public static final String MQTT_AUTH_PATH = "/mqtt/auth";

    /**
     * MQTT 统一事件处理接口路径
     * 对应 EMQX Webhook 的统一事件处理接口，支持所有客户端事件
     * 包括：client.connected、client.disconnected、message.publish 等
     */
    public static final String MQTT_EVENT_PATH = "/mqtt/event";

    /**
     * MQTT 授权接口路径（预留）
     * 对应 EMQX HTTP 授权插件的授权检查接口
     */
    public static final String MQTT_AUTHZ_PATH = "/mqtt/authz";

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
     * 构建设备配置推送主题
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildConfigPushTopic(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + "/thing/config/push";
    }

    /**
     * 构建设备事件上报通用回复主题
     * <p>
     * 不包含具体的事件标识符，事件标识符通过消息 data 中的 identifier 字段传递
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildEventPostReplyTopicGeneric(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + "/thing/event/post_reply";
    }

    /**
     * 构建设备服务调用通用主题
     * <p>
     * 不包含具体的服务标识符，服务标识符通过消息 data 中的 identifier 字段传递
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildServiceTopicGeneric(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + "/thing/service/invoke";
    }

    /**
     * 构建设备服务调用通用回复主题
     * <p>
     * 不包含具体的服务标识符，服务标识符通过消息 data 中的 identifier 字段传递
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildServiceReplyTopicGeneric(String productKey, String deviceName) {
        return buildDeviceTopicPrefix(productKey, deviceName) + "/thing/service/invoke_reply";
    }

}