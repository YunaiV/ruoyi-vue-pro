package cn.iocoder.yudao.module.iot.protocol.util;

import cn.hutool.core.util.StrUtil;

/**
 * IoT HTTP 协议主题工具类
 * <p>
 * 参考阿里云IoT平台HTTPS协议标准，支持以下路径格式：
 * 1. 设备认证：/auth
 * 2. 数据上报：/topic/${actualTopic}
 * <p>
 * 其中 actualTopic 遵循MQTT主题规范，例如：
 * - /sys/{productKey}/{deviceName}/thing/service/property/set
 * - /{productKey}/{deviceName}/user/get
 *
 * @author haohao
 */
public class IotHttpTopicUtils {

    /**
     * 设备认证路径
     */
    public static final String AUTH_PATH = "/auth";

    /**
     * 数据上报路径前缀
     */
    public static final String TOPIC_PATH_PREFIX = "/topic";

    /**
     * 系统主题前缀
     */
    public static final String SYS_TOPIC_PREFIX = "/sys";

    /**
     * 构建设备认证路径
     *
     * @return 认证路径
     */
    public static String buildAuthPath() {
        return AUTH_PATH;
    }

    /**
     * 构建数据上报路径
     *
     * @param actualTopic 实际的MQTT主题
     * @return HTTP数据上报路径
     */
    public static String buildTopicPath(String actualTopic) {
        if (StrUtil.isBlank(actualTopic)) {
            return null;
        }
        return TOPIC_PATH_PREFIX + actualTopic;
    }

    /**
     * 构建系统属性设置路径
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return HTTP路径
     */
    public static String buildPropertySetPath(String productKey, String deviceName) {
        if (StrUtil.hasBlank(productKey, deviceName)) {
            return null;
        }
        String actualTopic = SYS_TOPIC_PREFIX + "/" + productKey + "/" + deviceName + "/thing/service/property/set";
        return buildTopicPath(actualTopic);
    }

    /**
     * 构建系统属性获取路径
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return HTTP路径
     */
    public static String buildPropertyGetPath(String productKey, String deviceName) {
        if (StrUtil.hasBlank(productKey, deviceName)) {
            return null;
        }
        String actualTopic = SYS_TOPIC_PREFIX + "/" + productKey + "/" + deviceName + "/thing/service/property/get";
        return buildTopicPath(actualTopic);
    }

    /**
     * 构建系统属性上报路径
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return HTTP路径
     */
    public static String buildPropertyPostPath(String productKey, String deviceName) {
        if (StrUtil.hasBlank(productKey, deviceName)) {
            return null;
        }
        String actualTopic = SYS_TOPIC_PREFIX + "/" + productKey + "/" + deviceName + "/thing/event/property/post";
        return buildTopicPath(actualTopic);
    }

    /**
     * 构建系统事件上报路径
     *
     * @param productKey      产品Key
     * @param deviceName      设备名称
     * @param eventIdentifier 事件标识符
     * @return HTTP路径
     */
    public static String buildEventPostPath(String productKey, String deviceName, String eventIdentifier) {
        if (StrUtil.hasBlank(productKey, deviceName, eventIdentifier)) {
            return null;
        }
        String actualTopic = SYS_TOPIC_PREFIX + "/" + productKey + "/" + deviceName + "/thing/event/" + eventIdentifier
                + "/post";
        return buildTopicPath(actualTopic);
    }

    /**
     * 构建系统服务调用路径
     *
     * @param productKey        产品Key
     * @param deviceName        设备名称
     * @param serviceIdentifier 服务标识符
     * @return HTTP路径
     */
    public static String buildServiceInvokePath(String productKey, String deviceName, String serviceIdentifier) {
        if (StrUtil.hasBlank(productKey, deviceName, serviceIdentifier)) {
            return null;
        }
        String actualTopic = SYS_TOPIC_PREFIX + "/" + productKey + "/" + deviceName + "/thing/service/"
                + serviceIdentifier;
        return buildTopicPath(actualTopic);
    }

    /**
     * 构建自定义主题路径
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @param customPath 自定义路径
     * @return HTTP路径
     */
    public static String buildCustomTopicPath(String productKey, String deviceName, String customPath) {
        if (StrUtil.hasBlank(productKey, deviceName, customPath)) {
            return null;
        }
        String actualTopic = "/" + productKey + "/" + deviceName + "/" + customPath;
        return buildTopicPath(actualTopic);
    }

    /**
     * 从HTTP路径中提取实际主题
     *
     * @param httpPath HTTP路径，格式：/topic/${actualTopic}
     * @return 实际主题，如果解析失败返回null
     */
    public static String extractActualTopic(String httpPath) {
        if (StrUtil.isBlank(httpPath) || !httpPath.startsWith(TOPIC_PATH_PREFIX)) {
            return null;
        }
        return httpPath.substring(TOPIC_PATH_PREFIX.length()); // 直接移除/topic前缀
    }

    /**
     * 从主题中解析产品Key
     *
     * @param topic 主题，支持系统主题和自定义主题
     * @return 产品Key，如果无法解析则返回null
     */
    public static String parseProductKeyFromTopic(String topic) {
        if (StrUtil.isBlank(topic)) {
            return null;
        }

        String[] parts = topic.split("/");

        // 系统主题格式：/sys/{productKey}/{deviceName}/...
        if (parts.length >= 4 && "sys".equals(parts[1])) {
            return parts[2];
        }

        // 自定义主题格式：/{productKey}/{deviceName}/...
        // 确保不是不完整的系统主题格式
        if (parts.length >= 3 && StrUtil.isNotBlank(parts[1]) && !"sys".equals(parts[1])) {
            return parts[1];
        }

        return null;
    }

    /**
     * 从主题中解析设备名称
     *
     * @param topic 主题，支持系统主题和自定义主题
     * @return 设备名称，如果无法解析则返回null
     */
    public static String parseDeviceNameFromTopic(String topic) {
        if (StrUtil.isBlank(topic)) {
            return null;
        }

        String[] parts = topic.split("/");

        // 系统主题格式：/sys/{productKey}/{deviceName}/...
        if (parts.length >= 4 && "sys".equals(parts[1])) {
            return parts[3];
        }

        // 自定义主题格式：/{productKey}/{deviceName}/...
        // 确保不是不完整的系统主题格式
        if (parts.length >= 3 && StrUtil.isNotBlank(parts[2]) && !"sys".equals(parts[1])) {
            return parts[2];
        }

        return null;
    }

    /**
     * 检查是否为认证路径
     *
     * @param path 路径
     * @return 如果是认证路径返回true，否则返回false
     */
    public static boolean isAuthPath(String path) {
        return AUTH_PATH.equals(path);
    }

    /**
     * 检查是否为数据上报路径
     *
     * @param path 路径
     * @return 如果是数据上报路径返回true，否则返回false
     */
    public static boolean isTopicPath(String path) {
        return path != null && path.startsWith(TOPIC_PATH_PREFIX);
    }

    /**
     * 检查是否为有效的HTTP路径
     *
     * @param path 路径
     * @return 如果是有效的HTTP路径返回true，否则返回false
     */
    public static boolean isValidHttpPath(String path) {
        return isAuthPath(path) || isTopicPath(path);
    }

    /**
     * 检查是否为系统主题
     *
     * @param topic 主题
     * @return 如果是系统主题返回true，否则返回false
     */
    public static boolean isSystemTopic(String topic) {
        return topic != null && topic.startsWith(SYS_TOPIC_PREFIX);
    }

    /**
     * 构建响应主题路径
     *
     * @param requestPath 请求路径
     * @return 响应路径，如果无法构建返回null
     */
    public static String buildReplyPath(String requestPath) {
        String actualTopic = extractActualTopic(requestPath);
        if (actualTopic == null) {
            return null;
        }

        // 为系统主题添加_reply后缀
        if (isSystemTopic(actualTopic)) {
            String replyTopic = actualTopic + "_reply";
            return buildTopicPath(replyTopic);
        }

        return null;
    }
}