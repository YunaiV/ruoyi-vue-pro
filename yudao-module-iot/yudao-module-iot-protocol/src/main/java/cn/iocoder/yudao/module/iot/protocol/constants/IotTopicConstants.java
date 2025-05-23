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

    /**
     * 方法名前缀常量
     */
    public static class MethodPrefix {
        /**
         * 物模型服务前缀
         */
        public static final String THING_SERVICE = "thing.service.";

        /**
         * 物模型事件前缀
         */
        public static final String THING_EVENT = "thing.event.";
    }

    /**
     * 完整方法名常量
     */
    public static class Method {
        /**
         * 属性设置方法
         */
        public static final String PROPERTY_SET = "thing.service.property.set";

        /**
         * 属性获取方法
         */
        public static final String PROPERTY_GET = "thing.service.property.get";

        /**
         * 属性上报方法
         */
        public static final String PROPERTY_POST = "thing.event.property.post";

        /**
         * 配置设置方法
         */
        public static final String CONFIG_SET = "thing.service.config.set";

        /**
         * OTA升级方法
         */
        public static final String OTA_UPGRADE = "thing.service.ota.upgrade";

        /**
         * 设备上线方法
         */
        public static final String DEVICE_ONLINE = "device.online";

        /**
         * 设备下线方法
         */
        public static final String DEVICE_OFFLINE = "device.offline";

        /**
         * 心跳方法
         */
        public static final String HEARTBEAT = "heartbeat";
    }

    /**
     * 主题关键字常量
     */
    public static class Keyword {
        /**
         * 事件关键字
         */
        public static final String EVENT = "event";

        /**
         * 服务关键字
         */
        public static final String SERVICE = "service";

        /**
         * 属性关键字
         */
        public static final String PROPERTY = "property";

        /**
         * 上报关键字
         */
        public static final String POST = "post";
    }

}