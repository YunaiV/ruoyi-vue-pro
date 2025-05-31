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

    // TODO @haohao：SYS_TOPIC_PREFIX、SERVICE_TOPIC_PREFIX、REPLY_SUFFIX 类似这种，要不搞成这个里面的静态变量？不是枚举值
    /**
     * 系统主题前缀
     */
    SYS_TOPIC_PREFIX("/sys/", "系统主题前缀"),

    /**
     * 服务调用主题前缀
     */
    SERVICE_TOPIC_PREFIX("/thing/service/", "服务调用主题前缀"),

    // TODO @haohao：注释时，中英文之间，有个空格；
    /**
     * 设备属性设置主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/service/property/set
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/service/property/set_reply
     */
    PROPERTY_SET_TOPIC("/thing/service/property/set", "设备属性设置主题"),

    /**
     * 设备属性获取主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/service/property/get
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/service/property/get_reply
     */
    PROPERTY_GET_TOPIC("/thing/service/property/get", "设备属性获取主题"),

    /**
     * 设备配置设置主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/service/config/set
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/service/config/set_reply
     */
    CONFIG_SET_TOPIC("/thing/service/config/set", "设备配置设置主题"),

    /**
     * 设备OTA升级主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/service/ota/upgrade
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/service/ota/upgrade_reply
     */
    OTA_UPGRADE_TOPIC("/thing/service/ota/upgrade", "设备OTA升级主题"),

    /**
     * 设备属性上报主题
     * 请求 Topic：/sys/${productKey}/${deviceName}/thing/event/property/post
     * 响应 Topic：/sys/${productKey}/${deviceName}/thing/event/property/post_reply
     */
    PROPERTY_POST_TOPIC("/thing/event/property/post", "设备属性上报主题"),

    /**
     * 设备事件上报主题前缀
     */
    EVENT_POST_TOPIC_PREFIX("/thing/event/", "设备事件上报主题前缀"),

    /**
     * 设备事件上报主题后缀
     */
    EVENT_POST_TOPIC_SUFFIX("/post", "设备事件上报主题后缀"),

    /**
     * 响应主题后缀
     */
    REPLY_SUFFIX("_reply", "响应主题后缀");

    private final String topic;
    private final String description;

    /**
     * 构建设备服务调用主题
     *
     * @param productKey        产品Key
     * @param deviceName        设备名称
     * @param serviceIdentifier 服务标识符
     * @return 完整的主题路径
     */
    public static String buildServiceTopic(String productKey, String deviceName, String serviceIdentifier) {
        // TODO @haohao：貌似 SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName 是统一的；
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName +
                SERVICE_TOPIC_PREFIX.getTopic() + serviceIdentifier;
    }

    /**
     * 构建设备属性设置主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertySetTopic(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName + PROPERTY_SET_TOPIC.getTopic();
    }

    /**
     * 构建设备属性获取主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyGetTopic(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName + PROPERTY_GET_TOPIC.getTopic();
    }

    /**
     * 构建设备配置设置主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildConfigSetTopic(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName + CONFIG_SET_TOPIC.getTopic();
    }

    /**
     * 构建设备 OTA 升级主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildOtaUpgradeTopic(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName + OTA_UPGRADE_TOPIC.getTopic();
    }

    /**
     * 构建设备属性上报主题
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 完整的主题路径
     */
    public static String buildPropertyPostTopic(String productKey, String deviceName) {
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName + PROPERTY_POST_TOPIC.getTopic();
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
        return SYS_TOPIC_PREFIX.getTopic() + productKey + "/" + deviceName +
                EVENT_POST_TOPIC_PREFIX.getTopic() + eventIdentifier + EVENT_POST_TOPIC_SUFFIX.getTopic();
    }

    /**
     * 获取响应主题
     *
     * @param requestTopic 请求主题
     * @return 响应主题
     */
    public static String getReplyTopic(String requestTopic) {
        return requestTopic + REPLY_SUFFIX.getTopic();
    }

}