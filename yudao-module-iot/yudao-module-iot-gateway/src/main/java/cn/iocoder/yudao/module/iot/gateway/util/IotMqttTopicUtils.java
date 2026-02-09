package cn.iocoder.yudao.module.iot.gateway.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

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
     * 回复主题后缀
     */
    private static final String REPLY_TOPIC_SUFFIX = "_reply";

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
     * MQTT ACL 接口路径
     * 对应 EMQX HTTP ACL 插件的 ACL 请求接口
     */
    public static final String MQTT_ACL_PATH = "/mqtt/acl";

    // ========== 消息方法标准化 ==========

    /**
     * 标准化设备回复消息的 method
     * <p>
     * MQTT 协议中，设备回复下行指令时，topic 和 method 会携带 _reply 后缀
     * （如 thing.service.invoke_reply）。平台内部统一使用基础 method（如 thing.service.invoke），
     * 通过 {@link IotDeviceMessage#getCode()} 非空来识别回复消息。
     * <p>
     * 此方法剥离 _reply 后缀，并确保 code 字段被设置。
     *
     * @param message 设备消息
     */
    public static void normalizeReplyMethod(IotDeviceMessage message) {
        String method = message.getMethod();
        if (!StrUtil.endWith(method, REPLY_TOPIC_SUFFIX)) {
            return;
        }
        // 1. 剥离 _reply 后缀
        message.setMethod(method.substring(0, method.length() - REPLY_TOPIC_SUFFIX.length()));
        // 2. 确保 code 被设置，使 isReplyMessage() 能正确识别
        if (message.getCode() == null) {
            message.setCode(GlobalErrorCodeConstants.SUCCESS.getCode());
        }
    }

    // ========== 工具方法 ==========

    /**
     * 根据消息方法构建对应的主题
     *
     * @param method 消息方法，例如 thing.property.post
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param isReply 是否为回复消息
     * @return 完整的主题路径
     */
    public static String buildTopicByMethod(String method, String productKey, String deviceName, boolean isReply) {
        if (StrUtil.isBlank(method)) {
            return null;
        }
        // 1. 将点分隔符转换为斜杠
        String topicSuffix = method.replace('.', '/');
        // 2. 对于回复消息，添加 _reply 后缀
        if (isReply) {
            topicSuffix += REPLY_TOPIC_SUFFIX;
        }
        // 3. 构建完整主题
        return SYS_TOPIC_PREFIX + productKey + "/" + deviceName + "/" + topicSuffix;
    }

    /**
     * 校验主题是否允许订阅
     * <p>
     * 规则：主题必须以 /sys/{productKey}/{deviceName}/ 开头，
     * 或者是通配符形式 /sys/{productKey}/{deviceName}/#
     *
     * @param topic      订阅的主题
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 是否允许订阅
     */
    public static boolean isTopicSubscribeAllowed(String topic, String productKey, String deviceName) {
        if (!StrUtil.isAllNotBlank(topic, productKey, deviceName)) {
            return false;
        }
        // 构建设备主题前缀
        String deviceTopicPrefix = SYS_TOPIC_PREFIX + productKey + "/" + deviceName + "/";
        // 主题必须以设备前缀开头，或者是设备前缀的通配符形式
        return topic.startsWith(deviceTopicPrefix)
                || topic.equals(SYS_TOPIC_PREFIX + productKey + "/" + deviceName + "/#");
    }

    /**
     * 校验主题是否允许发布
     * <p>
     * 规则：主题必须以 /sys/{productKey}/{deviceName}/ 开头，且不允许包含通配符（+/#）。
     *
     * @param topic      发布的主题
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 是否允许发布
     */
    public static boolean isTopicPublishAllowed(String topic, String productKey, String deviceName) {
        if (!StrUtil.isAllNotBlank(topic, productKey, deviceName)) {
            return false;
        }
        // MQTT publish topic 不允许包含通配符，但这里做一次兜底校验
        if (topic.contains("#") || topic.contains("+")) {
            return false;
        }
        String deviceTopicPrefix = SYS_TOPIC_PREFIX + productKey + "/" + deviceName + "/";
        return topic.startsWith(deviceTopicPrefix);
    }

}
