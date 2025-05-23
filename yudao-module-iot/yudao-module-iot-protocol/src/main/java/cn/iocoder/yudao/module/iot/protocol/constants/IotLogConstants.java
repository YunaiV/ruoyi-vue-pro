package cn.iocoder.yudao.module.iot.protocol.constants;

/**
 * IoT 协议日志消息常量类
 * <p>
 * 用于统一管理协议模块中的日志消息常量
 *
 * @author haohao
 */
public class IotLogConstants {

    /**
     * HTTP 协议日志消息
     */
    public static class Http {
        /**
         * 收到空消息内容
         */
        public static final String RECEIVED_EMPTY_MESSAGE = "[HTTP] 收到空消息内容, topic={}";

        /**
         * 不支持的路径格式
         */
        public static final String UNSUPPORTED_PATH_FORMAT = "[HTTP] 不支持的路径格式, topic={}";

        /**
         * 解析消息失败
         */
        public static final String PARSE_MESSAGE_FAILED = "[HTTP] 解析消息失败, topic={}";

        /**
         * 认证消息非JSON格式
         */
        public static final String AUTH_MESSAGE_NOT_JSON = "[HTTP] 认证消息非JSON格式, message={}";

        /**
         * 认证消息缺少必需字段
         */
        public static final String AUTH_MESSAGE_MISSING_REQUIRED_FIELDS = "[HTTP] 认证消息缺少必需字段, message={}";

        /**
         * 格式化响应失败
         */
        public static final String FORMAT_RESPONSE_FAILED = "[HTTP] 格式化响应失败";
    }

    /**
     * 协议转换器日志消息
     */
    public static class Converter {
        /**
         * 注册协议解析器
         */
        public static final String REGISTER_PARSER = "[协议转换器] 注册协议解析器: protocol={}, parser={}";

        /**
         * 移除协议解析器
         */
        public static final String REMOVE_PARSER = "[协议转换器] 移除协议解析器: protocol={}";

        /**
         * 不支持的协议类型
         */
        public static final String UNSUPPORTED_PROTOCOL = "[协议转换器] 不支持的协议类型: protocol={}";

        /**
         * 转换消息失败
         */
        public static final String CONVERT_MESSAGE_FAILED = "[协议转换器] 转换消息失败: protocol={}, topic={}";

        /**
         * 格式化响应失败
         */
        public static final String FORMAT_RESPONSE_FAILED = "[协议转换器] 格式化响应失败: protocol={}";

        /**
         * 自动选择协议
         */
        public static final String AUTO_SELECT_PROTOCOL = "[协议转换器] 自动选择协议: protocol={}, topic={}";

        /**
         * 协议解析失败，尝试下一个
         */
        public static final String PROTOCOL_PARSE_FAILED_TRY_NEXT = "[协议转换器] 协议解析失败，尝试下一个: protocol={}, topic={}";

        /**
         * 无法自动识别协议
         */
        public static final String CANNOT_AUTO_RECOGNIZE_PROTOCOL = "[协议转换器] 无法自动识别协议: topic={}";
    }
}