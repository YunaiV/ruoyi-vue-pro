package cn.iocoder.yudao.module.iot.protocol.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.protocol.constants.IotTopicConstants;
import cn.iocoder.yudao.module.iot.protocol.enums.IotMessageDirectionEnum;
import cn.iocoder.yudao.module.iot.protocol.enums.IotMessageTypeEnum;
import lombok.Data;

/**
 * IoT 主题解析器
 * <p>
 * 用于解析各种格式的 IoT 主题，提取其中的关键信息
 *
 * @author haohao
 */
public class IotTopicParser {

    /**
     * 主题解析结果
     */
    @Data
    public static class TopicInfo {
        /**
         * 产品Key
         */
        private String productKey;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 消息类型
         */
        private IotMessageTypeEnum messageType;

        /**
         * 消息方向
         */
        private IotMessageDirectionEnum direction;

        /**
         * 服务标识符（仅服务调用时有效）
         */
        private String serviceIdentifier;

        /**
         * 事件标识符（仅事件上报时有效）
         */
        private String eventIdentifier;

        /**
         * 是否为响应主题
         */
        private boolean isReply;

        /**
         * 原始主题
         */
        private String originalTopic;
    }

    /**
     * 解析主题
     *
     * @param topic 主题字符串
     * @return 解析结果，如果解析失败返回 null
     */
    public static TopicInfo parse(String topic) {
        if (StrUtil.isBlank(topic)) {
            return null;
        }

        TopicInfo info = new TopicInfo();
        info.setOriginalTopic(topic);

        // 检查是否为响应主题
        boolean isReply = topic.endsWith(IotTopicConstants.REPLY_SUFFIX);
        info.setReply(isReply);

        // 移除响应后缀，便于后续解析
        String normalizedTopic = isReply ? topic.substring(0, topic.length() - IotTopicConstants.REPLY_SUFFIX.length())
                : topic;

        // 解析系统主题
        if (normalizedTopic.startsWith(IotTopicConstants.SYS_TOPIC_PREFIX)) {
            return parseSystemTopic(info, normalizedTopic);
        }

        // 解析自定义主题
        return parseCustomTopic(info, normalizedTopic);
    }

    /**
     * 解析系统主题
     * 格式：/sys/{productKey}/{deviceName}/thing/service/{identifier}
     * 或：/sys/{productKey}/{deviceName}/thing/event/{identifier}/post
     */
    private static TopicInfo parseSystemTopic(TopicInfo info, String topic) {
        String[] parts = topic.split("/");
        if (parts.length < 6) {
            return null;
        }

        // 解析产品Key和设备名称
        info.setProductKey(parts[2]);
        info.setDeviceName(parts[3]);

        // 判断消息方向：包含 /post 通常是上行，其他是下行
        info.setDirection(topic.contains("/post") || topic.contains("/reply") ? IotMessageDirectionEnum.UPSTREAM
                : IotMessageDirectionEnum.DOWNSTREAM);

        // 解析具体的消息类型
        if (topic.contains("/thing/service/")) {
            return parseServiceTopic(info, topic, parts);
        } else if (topic.contains("/thing/event/")) {
            return parseEventTopic(info, topic, parts);
        }

        return null;
    }

    /**
     * 解析服务相关主题
     */
    private static TopicInfo parseServiceTopic(TopicInfo info, String topic, String[] parts) {
        // 查找 service 关键字的位置
        int serviceIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("service".equals(parts[i])) {
                serviceIndex = i;
                break;
            }
        }

        if (serviceIndex == -1 || serviceIndex + 1 >= parts.length) {
            return null;
        }

        String serviceType = parts[serviceIndex + 1];

        // 根据服务类型确定消息类型
        switch (serviceType) {
            case "property":
                if (serviceIndex + 2 < parts.length) {
                    String operation = parts[serviceIndex + 2];
                    if ("set".equals(operation)) {
                        info.setMessageType(IotMessageTypeEnum.PROPERTY_SET);
                    } else if ("get".equals(operation)) {
                        info.setMessageType(IotMessageTypeEnum.PROPERTY_GET);
                    }
                }
                break;
            case "config":
                if (serviceIndex + 2 < parts.length && "set".equals(parts[serviceIndex + 2])) {
                    info.setMessageType(IotMessageTypeEnum.CONFIG_SET);
                }
                break;
            case "ota":
                if (serviceIndex + 2 < parts.length && "upgrade".equals(parts[serviceIndex + 2])) {
                    info.setMessageType(IotMessageTypeEnum.OTA_UPGRADE);
                }
                break;
            default:
                // 自定义服务
                info.setMessageType(IotMessageTypeEnum.SERVICE_INVOKE);
                info.setServiceIdentifier(serviceType);
                break;
        }

        return info;
    }

    /**
     * 解析事件相关主题
     */
    private static TopicInfo parseEventTopic(TopicInfo info, String topic, String[] parts) {
        // 查找 event 关键字的位置
        int eventIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("event".equals(parts[i])) {
                eventIndex = i;
                break;
            }
        }

        if (eventIndex == -1 || eventIndex + 1 >= parts.length) {
            return null;
        }

        String eventType = parts[eventIndex + 1];

        if ("property".equals(eventType) && eventIndex + 2 < parts.length && "post".equals(parts[eventIndex + 2])) {
            info.setMessageType(IotMessageTypeEnum.PROPERTY_POST);
        } else {
            // 自定义事件
            info.setMessageType(IotMessageTypeEnum.EVENT_POST);
            info.setEventIdentifier(eventType);
        }

        return info;
    }

    /**
     * 解析自定义主题
     * 这里可以根据实际需求扩展自定义主题的解析逻辑
     */
    private static TopicInfo parseCustomTopic(TopicInfo info, String topic) {
        // TODO: 根据业务需要实现自定义主题解析逻辑
        return info;
    }

    /**
     * 检查主题是否为有效的系统主题
     *
     * @param topic 主题
     * @return 如果是有效的系统主题返回 true，否则返回 false
     */
    public static boolean isValidSystemTopic(String topic) {
        TopicInfo info = parse(topic);
        return info != null &&
                StrUtil.isNotBlank(info.getProductKey()) &&
                StrUtil.isNotBlank(info.getDeviceName()) &&
                info.getMessageType() != null;
    }

    /**
     * 检查主题是否为响应主题
     *
     * @param topic 主题
     * @return 如果是响应主题返回 true，否则返回 false
     */
    public static boolean isReplyTopic(String topic) {
        return topic != null && topic.endsWith(IotTopicConstants.REPLY_SUFFIX);
    }
}