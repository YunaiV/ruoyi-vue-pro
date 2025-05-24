package cn.iocoder.yudao.module.iot.protocol.message.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.IotMqttMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;
import cn.iocoder.yudao.module.iot.protocol.util.IotTopicUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * IoT MQTT 协议消息解析器实现
 * <p>
 * 基于 MQTT 协议规范实现的消息解析器，支持设备属性、事件、服务调用等标准功能
 *
 * @author haohao
 */
@Slf4j
public class IotMqttMessageParser implements IotMessageParser {

    @Override
    public IotMqttMessage parse(String topic, byte[] payload) {
        if (payload == null || payload.length == 0) {
            log.warn("[MQTT] 收到空消息内容, topic={}", topic);
            return null;
        }

        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            if (!JSONUtil.isTypeJSON(message)) {
                log.warn("[MQTT] 收到非JSON格式消息, topic={}, message={}", topic, message);
                return null;
            }

            JSONObject json = JSONUtil.parseObj(message);
            String id = json.getStr("id");
            String method = json.getStr("method");

            if (StrUtil.isBlank(method)) {
                // 尝试从 topic 中解析方法
                method = IotTopicUtils.parseMethodFromTopic(topic);
                if (StrUtil.isBlank(method)) {
                    log.warn("[MQTT] 无法确定消息方法, topic={}, message={}", topic, message);
                    return null;
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) json.getObj("params", Map.class);
            return IotMqttMessage.builder()
                    .id(id)
                    .method(method)
                    .version(json.getStr("version", "1.0"))
                    .params(params)
                    .build();
        } catch (Exception e) {
            log.error("[MQTT] 解析消息失败, topic={}", topic, e);
            return null;
        }
    }

    @Override
    public byte[] formatResponse(IotStandardResponse response) {
        try {
            String json = JsonUtils.toJsonString(response);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[MQTT] 格式化响应失败", e);
            return new byte[0];
        }
    }

    @Override
    public boolean canHandle(String topic) {
        // MQTT 协议支持更多主题格式
        return topic != null && (
                topic.startsWith("/sys/") ||      // 兼容现有系统主题
                        topic.startsWith("/mqtt/") ||     // 新的通用 MQTT 主题
                        topic.startsWith("/device/")      // 设备主题
        );
    }
} 