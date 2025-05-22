package cn.iocoder.yudao.module.iot.protocol.constants;

/**
 * IoT 设备主题常量类
 * <p>
 * 用于统一管理 MQTT 协议中的主题常量，基于 Alink 协议规范
 *
 * @author haohao
 */
public class IotTopicConstants {

    /**
     * 系统主题前缀
     */
    public static final String SYS_TOPIC_PREFIX = "/sys/";

    /**
     * 服务调用主题前缀
     */
    public static final String SERVICE_TOPIC_PREFIX = "/thing/service/";

    /**
     * 设备属性设置主题
     * 请求Topic：/sys/${productKey}/${deviceName}/thing/service/property/set
     * 响应Topic：/sys/${productKey}/${deviceName}/thing/service/property/set_reply
     */
    public static final String PROPERTY_SET_TOPIC = "/thing/service/property/set";

    /**
     * 设备属性获取主题
     * 请求Topic：/sys/${productKey}/${deviceName}/thing/service/property/get
     * 响应Topic：/sys/${productKey}/${deviceName}/thing/service/property/get_reply
     */
    public static final String PROPERTY_GET_TOPIC = "/thing/service/property/get";

    /**
     * 设备配置设置主题
     * 请求Topic：/sys/${productKey}/${deviceName}/thing/service/config/set
     * 响应Topic：/sys/${productKey}/${deviceName}/thing/service/config/set_reply
     */
    public static final String CONFIG_SET_TOPIC = "/thing/service/config/set";

    /**
     * 设备OTA升级主题
     * 请求Topic：/sys/${productKey}/${deviceName}/thing/service/ota/upgrade
     * 响应Topic：/sys/${productKey}/${deviceName}/thing/service/ota/upgrade_reply
     */
    public static final String OTA_UPGRADE_TOPIC = "/thing/service/ota/upgrade";

    /**
     * 设备属性上报主题
     * 请求Topic：/sys/${productKey}/${deviceName}/thing/event/property/post
     * 响应Topic：/sys/${productKey}/${deviceName}/thing/event/property/post_reply
     */
    public static final String PROPERTY_POST_TOPIC = "/thing/event/property/post";

    /**
     * 设备事件上报主题前缀
     */
    public static final String EVENT_POST_TOPIC_PREFIX = "/thing/event/";

    /**
     * 设备事件上报主题后缀
     */
    public static final String EVENT_POST_TOPIC_SUFFIX = "/post";

    /**
     * 响应主题后缀
     */
    public static final String REPLY_SUFFIX = "_reply";

} 