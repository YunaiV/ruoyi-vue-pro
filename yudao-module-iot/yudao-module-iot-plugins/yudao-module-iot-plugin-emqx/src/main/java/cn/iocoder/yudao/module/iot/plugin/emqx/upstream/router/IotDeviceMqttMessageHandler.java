package cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.messages.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * IoT 设备 MQTT 消息处理器
 * <p>
 * 参考：
 * <p>
 * "<a href="https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services?spm=a2c4g.11186623.0.0.97a72915vRck44#section-g4j-5zg-12b">...</a>">
 */
@Slf4j
public class IotDeviceMqttMessageHandler {

    // 设备上报属性 标准 JSON
    // 请求Topic：/sys/${productKey}/${deviceName}/thing/event/property/post
    // 响应Topic：/sys/${productKey}/${deviceName}/thing/event/property/post_reply
    // 设备上报事件 标准 JSON
    // 请求Topic：/sys/${productKey}/${deviceName}/thing/event/${tsl.event.identifier}/post
    // 响应Topic：/sys/${productKey}/${deviceName}/thing/event/${tsl.event.identifier}/post_reply

    private static final String SYS_TOPIC_PREFIX = "/sys/";
    private static final String PROPERTY_POST_TOPIC = "/thing/event/property/post";
    private static final String EVENT_POST_TOPIC_PREFIX = "/thing/event/";
    private static final String EVENT_POST_TOPIC_SUFFIX = "/post";

    private final IotDeviceUpstreamApi deviceUpstreamApi;
    private final MqttClient mqttClient;

    public IotDeviceMqttMessageHandler(IotDeviceUpstreamApi deviceUpstreamApi, MqttClient mqttClient) {
        this.deviceUpstreamApi = deviceUpstreamApi;
        this.mqttClient = mqttClient;
    }

    public void handle(MqttPublishMessage message) {
        String topic = message.topicName();
        String payload = message.payload().toString();
        log.info("[messageHandler][接收到消息][topic: {}][payload: {}]", topic, payload);

        try {
            handleMessage(topic, payload);
        } catch (Exception e) {
            log.error("[messageHandler][处理消息失败][topic: {}][payload: {}]", topic, payload, e);
        }
    }

    private void handleMessage(String topic, String payload) {
        // 校验前缀
        if (!topic.startsWith(SYS_TOPIC_PREFIX)) {
            log.warn("[handleMessage][未知的消息类型][topic: {}]", topic);
            return;
        }

        // 处理设备属性上报消息
        if (topic.endsWith(PROPERTY_POST_TOPIC)) {
            log.info("[handleMessage][接收到设备属性上报][topic: {}]", topic);
            handlePropertyPost(topic, payload);
            return;
        }

        // 处理设备事件上报消息
        if (topic.contains(EVENT_POST_TOPIC_PREFIX) && topic.endsWith(EVENT_POST_TOPIC_SUFFIX)) {
            log.info("[handleMessage][接收到设备事件上报][topic: {}]", topic);
            handleEventPost(topic, payload);
            return;
        }

        // 未知消息类型
        log.warn("[handleMessage][未知的消息类型][topic: {}]", topic);
    }

    /**
     * 处理设备属性上报消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    private void handlePropertyPost(String topic, String payload) {
        // 解析消息内容
        JSONObject jsonObject = JSONUtil.parseObj(payload);
        String[] topicParts = topic.split("/");

        // 构建设备属性上报请求对象
        IotDevicePropertyReportReqDTO reportReqDTO = buildPropertyReportDTO(jsonObject, topicParts);

        // 调用上游 API 处理设备上报数据
        deviceUpstreamApi.reportDeviceProperty(reportReqDTO);
        log.info("[handlePropertyPost][处理设备上行消息成功][topic: {}][reportReqDTO: {}]",
                topic, JSONUtil.toJsonStr(reportReqDTO));

        // 发送响应消息
        String replyTopic = topic + "_reply";
        JSONObject response = new JSONObject()
                .set("id", jsonObject.getStr("id"))
                .set("code", 200)
                .set("data", new JSONObject())
                .set("message", "success")
                .set("method", "thing.event.property.post");

        mqttClient.publish(replyTopic,
                Buffer.buffer(response.toString()),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
        log.info("[handlePropertyPost][发送响应消息成功][topic: {}][response: {}]",
                replyTopic, response.toString());
    }

    /**
     * 处理设备事件上报消息
     *
     * @param topic   主题
     * @param payload 消息内容
     */
    private void handleEventPost(String topic, String payload) {
        // 解析消息内容
        JSONObject jsonObject = JSONUtil.parseObj(payload);
        String[] topicParts = topic.split("/");

        // 构建设备事件上报请求对象
        IotDeviceEventReportReqDTO reportReqDTO = buildEventReportDTO(jsonObject, topicParts);

        // 调用上游 API 处理设备上报数据
        deviceUpstreamApi.reportDeviceEvent(reportReqDTO);
        log.info("[handleEventPost][处理设备上行消息成功][topic: {}][reportReqDTO: {}]",
                topic, JSONUtil.toJsonStr(reportReqDTO));

        // 发送响应消息
        String replyTopic = topic + "_reply";
        String eventIdentifier = topicParts[6]; // 从 topic 中获取事件标识符
        JSONObject response = new JSONObject()
                .set("id", jsonObject.getStr("id"))
                .set("code", 200)
                .set("data", new JSONObject())
                .set("message", "success")
                .set("method", "thing.event." + eventIdentifier + ".post");

        mqttClient.publish(replyTopic,
                Buffer.buffer(response.toString()),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
        log.info("[handleEventPost][发送响应消息成功][topic: {}][response: {}]",
                replyTopic, response.toString());
    }

    /**
     * 构建设备属性上报请求对象
     *
     * @param jsonObject 消息内容
     * @param topicParts 主题部分
     * @return 设备属性上报请求对象
     */
    private IotDevicePropertyReportReqDTO buildPropertyReportDTO(JSONObject jsonObject,
                                                                 String[] topicParts) {
        return ((IotDevicePropertyReportReqDTO) new IotDevicePropertyReportReqDTO()
                .setRequestId(jsonObject.getStr("id"))
                .setProcessId(IotPluginCommonUtils.getProcessId())
                .setReportTime(LocalDateTime.now())
                .setProductKey(topicParts[2])
                .setDeviceName(topicParts[3]))
                .setProperties(jsonObject.getJSONObject("params"));
    }

    /**
     * 构建设备事件上报请求对象
     *
     * @param jsonObject 消息内容
     * @param topicParts 主题部分
     * @return 设备事件上报请求对象
     */
    private IotDeviceEventReportReqDTO buildEventReportDTO(JSONObject jsonObject, String[] topicParts) {
        return ((IotDeviceEventReportReqDTO) new IotDeviceEventReportReqDTO()
                .setRequestId(jsonObject.getStr("id"))
                .setProcessId(IotPluginCommonUtils.getProcessId())
                .setReportTime(LocalDateTime.now())
                .setProductKey(topicParts[2])
                .setDeviceName(topicParts[3]))
                .setIdentifier(topicParts[4])
                .setParams(jsonObject.getJSONObject("params"));
    }
}