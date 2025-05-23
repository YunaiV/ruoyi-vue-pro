package cn.iocoder.yudao.module.iot.protocol.constants;

/**
 * IoT HTTP 协议常量类
 * <p>
 * 用于统一管理 HTTP 协议中的常量，包括路径、字段名、默认值等
 *
 * @author haohao
 */
public class IotHttpConstants {

    /**
     * 路径常量
     */
    public static class Path {
        /**
         * 认证路径
         */
        public static final String AUTH = "/auth";

        /**
         * 主题路径前缀
         */
        public static final String TOPIC_PREFIX = "/topic";
    }

    /**
     * 认证字段常量
     */
    public static class AuthField {
        /**
         * 产品Key
         */
        public static final String PRODUCT_KEY = "productKey";

        /**
         * 设备名称
         */
        public static final String DEVICE_NAME = "deviceName";

        /**
         * 客户端ID
         */
        public static final String CLIENT_ID = "clientId";

        /**
         * 时间戳
         */
        public static final String TIMESTAMP = "timestamp";

        /**
         * 签名
         */
        public static final String SIGN = "sign";

        /**
         * 签名方法
         */
        public static final String SIGN_METHOD = "signmethod";

        /**
         * 版本
         */
        public static final String VERSION = "version";
    }

    /**
     * 消息字段常量
     */
    public static class MessageField {
        /**
         * 消息ID
         */
        public static final String ID = "id";

        /**
         * 方法名
         */
        public static final String METHOD = "method";

        /**
         * 版本
         */
        public static final String VERSION = "version";

        /**
         * 参数
         */
        public static final String PARAMS = "params";

        /**
         * 数据
         */
        public static final String DATA = "data";
    }

    /**
     * 响应字段常量
     */
    public static class ResponseField {
        /**
         * 状态码
         */
        public static final String CODE = "code";

        /**
         * 消息
         */
        public static final String MESSAGE = "message";

        /**
         * 信息
         */
        public static final String INFO = "info";

        /**
         * 令牌
         */
        public static final String TOKEN = "token";

        /**
         * 消息ID
         */
        public static final String MESSAGE_ID = "messageId";
    }

    /**
     * 默认值常量
     */
    public static class DefaultValue {
        /**
         * 默认签名方法
         */
        public static final String SIGN_METHOD = "hmacmd5";

        /**
         * 默认版本
         */
        public static final String VERSION = "default";

        /**
         * 默认消息版本
         */
        public static final String MESSAGE_VERSION = "1.0";

        /**
         * 未知方法名
         */
        public static final String UNKNOWN_METHOD = "unknown";
    }

    /**
     * 方法名常量
     */
    public static class Method {
        /**
         * 设备认证
         */
        public static final String DEVICE_AUTH = "device.auth";

        /**
         * 自定义消息
         */
        public static final String CUSTOM_MESSAGE = "custom.message";
    }
}