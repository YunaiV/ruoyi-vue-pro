package cn.iocoder.yudao.module.iot.protocol.message.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.protocol.constants.IotHttpConstants;
import cn.iocoder.yudao.module.iot.protocol.constants.IotLogConstants;
import cn.iocoder.yudao.module.iot.protocol.constants.IotTopicConstants;
import cn.iocoder.yudao.module.iot.protocol.message.IotMessageParser;
import cn.iocoder.yudao.module.iot.protocol.message.IotMqttMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * IoT HTTP 协议消息解析器实现
 * <p>
 * 参考阿里云IoT平台HTTPS协议标准，支持设备认证和数据上报两种消息类型：
 * <p>
 * 1. 设备认证消息格式：
 *
 * <pre>
 * POST /auth HTTP/1.1
 * Content-Type: application/json
 * {
 *   "productKey": "a1AbC***",
 *   "deviceName": "device01",
 *   "clientId": "device01_001",
 *   "timestamp": "1501668289957",
 *   "sign": "xxxxx",
 *   "signmethod": "hmacsha1",
 *   "version": "default"
 * }
 * </pre>
 * <p>
 * 2. 数据上报消息格式：
 *
 * <pre>
 * POST /topic/${topic} HTTP/1.1
 * password: ${token}
 * Content-Type: application/octet-stream
 * ${payload}
 * </pre>
 *
 * @author haohao
 */
@Slf4j
public class IotHttpMessageParser implements IotMessageParser {

    /**
     * 认证路径
     */
    public static final String AUTH_PATH = IotHttpConstants.Path.AUTH;

    /**
     * 主题路径前缀
     */
    public static final String TOPIC_PATH_PREFIX = IotHttpConstants.Path.TOPIC_PREFIX;

    @Override
    public IotMqttMessage parse(String topic, byte[] payload) {
        if (payload == null || payload.length == 0) {
            log.warn(IotLogConstants.Http.RECEIVED_EMPTY_MESSAGE, topic);
            return null;
        }

        try {
            String message = new String(payload, StandardCharsets.UTF_8);

            // 判断是认证请求还是数据上报
            if (AUTH_PATH.equals(topic)) {
                return parseAuthMessage(message);
            } else if (topic.startsWith(TOPIC_PATH_PREFIX)) {
                return parseDataMessage(topic, message);
            } else {
                log.warn(IotLogConstants.Http.UNSUPPORTED_PATH_FORMAT, topic);
                return null;
            }

        } catch (Exception e) {
            log.error(IotLogConstants.Http.PARSE_MESSAGE_FAILED, topic, e);
            return null;
        }
    }

    /**
     * 解析设备认证消息
     *
     * @param message 认证消息JSON
     * @return 标准消息格式
     */
    private IotMqttMessage parseAuthMessage(String message) {
        if (!JSONUtil.isTypeJSON(message)) {
            log.warn(IotLogConstants.Http.AUTH_MESSAGE_NOT_JSON, message);
            return null;
        }

        JSONObject json = JSONUtil.parseObj(message);

        // 验证必需字段
        String productKey = json.getStr(IotHttpConstants.AuthField.PRODUCT_KEY);
        String deviceName = json.getStr(IotHttpConstants.AuthField.DEVICE_NAME);
        String clientId = json.getStr(IotHttpConstants.AuthField.CLIENT_ID);
        String sign = json.getStr(IotHttpConstants.AuthField.SIGN);

        if (StrUtil.hasBlank(productKey, deviceName, clientId, sign)) {
            log.warn(IotLogConstants.Http.AUTH_MESSAGE_MISSING_REQUIRED_FIELDS, message);
            return null;
        }

        // 构建认证消息
        Map<String, Object> params = new HashMap<>();
        params.put(IotHttpConstants.AuthField.PRODUCT_KEY, productKey);
        params.put(IotHttpConstants.AuthField.DEVICE_NAME, deviceName);
        params.put(IotHttpConstants.AuthField.CLIENT_ID, clientId);
        params.put(IotHttpConstants.AuthField.TIMESTAMP, json.getStr(IotHttpConstants.AuthField.TIMESTAMP));
        params.put(IotHttpConstants.AuthField.SIGN, sign);
        params.put(IotHttpConstants.AuthField.SIGN_METHOD,
                json.getStr(IotHttpConstants.AuthField.SIGN_METHOD, IotHttpConstants.DefaultValue.SIGN_METHOD));

        return IotMqttMessage.builder()
                .id(generateMessageId())
                .method(IotHttpConstants.Method.DEVICE_AUTH)
                .version(json.getStr(IotHttpConstants.AuthField.VERSION, IotHttpConstants.DefaultValue.VERSION))
                .params(params)
                .build();
    }

    /**
     * 解析数据上报消息
     *
     * @param topic   主题路径，格式：/topic/${actualTopic}
     * @param message 消息内容
     * @return 标准消息格式
     */
    private IotMqttMessage parseDataMessage(String topic, String message) {
        // 提取实际的主题，去掉 /topic 前缀
        String actualTopic = topic.substring(TOPIC_PATH_PREFIX.length()); // 直接移除/topic前缀

        // 尝试解析为JSON格式
        if (JSONUtil.isTypeJSON(message)) {
            return parseJsonDataMessage(actualTopic, message);
        } else {
            // 原始数据格式
            return parseRawDataMessage(actualTopic, message);
        }
    }

    /**
     * 解析JSON格式的数据消息
     *
     * @param topic   实际主题
     * @param message JSON消息
     * @return 标准消息格式
     */
    private IotMqttMessage parseJsonDataMessage(String topic, String message) {
        JSONObject json = JSONUtil.parseObj(message);

        // 生成消息ID
        String messageId = json.getStr(IotHttpConstants.MessageField.ID);
        if (StrUtil.isBlank(messageId)) {
            messageId = generateMessageId();
        }

        // 获取方法名
        String method = json.getStr(IotHttpConstants.MessageField.METHOD);
        if (StrUtil.isBlank(method)) {
            // 根据主题推断方法名
            method = inferMethodFromTopic(topic);
        }

        // 获取参数
        Object params = json.get(IotHttpConstants.MessageField.PARAMS);
        Map<String, Object> paramsMap = new HashMap<>();
        if (params instanceof Map) {
            paramsMap.putAll((Map<String, Object>) params);
        } else if (params != null) {
            paramsMap.put(IotHttpConstants.MessageField.DATA, params);
        }

        return IotMqttMessage.builder()
                .id(messageId)
                .method(method)
                .version(json.getStr(IotHttpConstants.MessageField.VERSION,
                        IotHttpConstants.DefaultValue.MESSAGE_VERSION))
                .params(paramsMap)
                .build();
    }

    /**
     * 解析原始数据消息
     *
     * @param topic   实际主题
     * @param message 原始消息
     * @return 标准消息格式
     */
    private IotMqttMessage parseRawDataMessage(String topic, String message) {
        Map<String, Object> params = new HashMap<>();
        params.put(IotHttpConstants.MessageField.DATA, message);

        return IotMqttMessage.builder()
                .id(generateMessageId())
                .method(inferMethodFromTopic(topic))
                .version(IotHttpConstants.DefaultValue.MESSAGE_VERSION)
                .params(params)
                .build();
    }

    /**
     * 根据主题推断方法名
     *
     * @param topic 主题
     * @return 方法名
     */
    private String inferMethodFromTopic(String topic) {
        if (StrUtil.isBlank(topic)) {
            return IotHttpConstants.DefaultValue.UNKNOWN_METHOD;
        }

        // 标准系统主题解析
        if (topic.startsWith(IotTopicConstants.SYS_TOPIC_PREFIX)) {
            if (topic.contains(IotTopicConstants.PROPERTY_SET_TOPIC)) {
                return IotTopicConstants.Method.PROPERTY_SET;
            } else if (topic.contains(IotTopicConstants.PROPERTY_GET_TOPIC)) {
                return IotTopicConstants.Method.PROPERTY_GET;
            } else if (topic.contains(IotTopicConstants.PROPERTY_POST_TOPIC)) {
                return IotTopicConstants.Method.PROPERTY_POST;
            } else if (topic.contains(IotTopicConstants.EVENT_POST_TOPIC_PREFIX)
                    && topic.endsWith(IotTopicConstants.EVENT_POST_TOPIC_SUFFIX)) {
                // 自定义事件上报
                String[] parts = topic.split("/");
                // 查找event关键字的位置
                for (int i = 0; i < parts.length; i++) {
                    if (IotTopicConstants.Keyword.EVENT.equals(parts[i]) && i + 1 < parts.length) {
                        String eventId = parts[i + 1];
                        return IotTopicConstants.MethodPrefix.THING_EVENT + eventId + ".post";
                    }
                }
            } else if (topic.contains(IotTopicConstants.SERVICE_TOPIC_PREFIX)
                    && !topic.contains(IotTopicConstants.Keyword.PROPERTY)) {
                // 自定义服务调用
                String[] parts = topic.split("/");
                // 查找service关键字的位置
                for (int i = 0; i < parts.length; i++) {
                    if (IotTopicConstants.Keyword.SERVICE.equals(parts[i]) && i + 1 < parts.length) {
                        String serviceId = parts[i + 1];
                        return IotTopicConstants.MethodPrefix.THING_SERVICE + serviceId;
                    }
                }
            }
        }

        // 自定义主题
        return IotHttpConstants.Method.CUSTOM_MESSAGE;
    }

    /**
     * 生成消息ID
     *
     * @return 消息ID
     */
    private String generateMessageId() {
        return IotMqttMessage.generateRequestId();
    }

    @Override
    public byte[] formatResponse(IotStandardResponse response) {
        try {
            JSONObject httpResponse = new JSONObject();

            // 判断是否为认证响应
            if (IotHttpConstants.Method.DEVICE_AUTH.equals(response.getMethod())) {
                // 认证响应格式
                httpResponse.set(IotHttpConstants.ResponseField.CODE, response.getCode());
                httpResponse.set(IotHttpConstants.ResponseField.MESSAGE, response.getMessage());

                if (response.getCode() == 200 && response.getData() != null) {
                    JSONObject info = new JSONObject();
                    if (response.getData() instanceof Map) {
                        Map<String, Object> dataMap = (Map<String, Object>) response.getData();
                        info.putAll(dataMap);
                    } else {
                        info.set(IotHttpConstants.ResponseField.TOKEN, response.getData().toString());
                    }
                    httpResponse.set(IotHttpConstants.ResponseField.INFO, info);
                }
            } else {
                // 数据上报响应格式
                httpResponse.set(IotHttpConstants.ResponseField.CODE, response.getCode());
                httpResponse.set(IotHttpConstants.ResponseField.MESSAGE, response.getMessage());

                if (response.getCode() == 200) {
                    JSONObject info = new JSONObject();
                    info.set(IotHttpConstants.ResponseField.MESSAGE_ID, response.getId());
                    httpResponse.set(IotHttpConstants.ResponseField.INFO, info);
                }
            }

            String json = httpResponse.toString();
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(IotLogConstants.Http.FORMAT_RESPONSE_FAILED, e);
            return new byte[0];
        }
    }

    @Override
    public boolean canHandle(String topic) {
        // 支持认证路径和主题路径
        return topic != null && (AUTH_PATH.equals(topic) || topic.startsWith(TOPIC_PATH_PREFIX));
    }

    /**
     * 从设备标识中解析产品Key和设备名称
     *
     * @param deviceKey 设备标识，格式：productKey/deviceName
     * @return 包含产品Key和设备名称的数组，[0]为产品Key，[1]为设备名称
     */
    public static String[] parseDeviceKey(String deviceKey) {
        if (StrUtil.isBlank(deviceKey)) {
            return null;
        }

        String[] parts = deviceKey.split("/");
        if (parts.length != 2) {
            return null;
        }

        return new String[]{parts[0], parts[1]};
    }

    /**
     * 构建设备标识
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @return 设备标识，格式：productKey/deviceName
     */
    public static String buildDeviceKey(String productKey, String deviceName) {
        if (StrUtil.isBlank(productKey) || StrUtil.isBlank(deviceName)) {
            return null;
        }
        return productKey + "/" + deviceName;
    }
}