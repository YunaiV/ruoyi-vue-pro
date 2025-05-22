package cn.iocoder.yudao.module.iot.protocol.message.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.protocol.message.IotAlinkMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;
import cn.iocoder.yudao.module.iot.protocol.util.IotTopicUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * IoT Alink 协议消息解析器实现
 * <p>
 * 基于阿里云 Alink 协议规范实现的消息解析器
 *
 * @author haohao
 */
@Slf4j
public class IotAlinkMessageParser implements IotMessageParser {

    @Override
    public IotAlinkMessage parse(String topic, byte[] payload) {
        if (payload == null || payload.length == 0) {
            log.warn("[Alink] 收到空消息内容, topic={}", topic);
            return null;
        }

        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            if (!JSONUtil.isTypeJSON(message)) {
                log.warn("[Alink] 收到非JSON格式消息, topic={}, message={}", topic, message);
                return null;
            }

            JSONObject json = JSONUtil.parseObj(message);
            String id = json.getStr("id");
            String method = json.getStr("method");
            
            if (StrUtil.isBlank(method)) {
                // 尝试从 topic 中解析方法
                method = IotTopicUtils.parseMethodFromTopic(topic);
                if (StrUtil.isBlank(method)) {
                    log.warn("[Alink] 无法确定消息方法, topic={}, message={}", topic, message);
                    return null;
                }
            }

            Map<String, Object> params = (Map<String, Object>) json.getObj("params", Map.class);
            return IotAlinkMessage.builder()
                    .id(id)
                    .method(method)
                    .version(json.getStr("version", "1.0"))
                    .params(params)
                    .build();
        } catch (Exception e) {
            log.error("[Alink] 解析消息失败, topic={}", topic, e);
            return null;
        }
    }

    @Override
    public byte[] formatResponse(IotStandardResponse response) {
        try {
            String json = JsonUtils.toJsonString(response);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[Alink] 格式化响应失败", e);
            return new byte[0];
        }
    }

    @Override
    public boolean canHandle(String topic) {
        // Alink 协议处理所有系统主题
        return topic != null && topic.startsWith("/sys/");
    }
} 