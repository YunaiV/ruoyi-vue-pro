package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.upstream;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议的处理器抽象基类
 * <p>
 * 提供通用的连接校验、响应发送等功能
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public abstract class IotMqttAbstractHandler {

    // TODO @AI：不使用 codec，使用 serializer 来进行编解码；
    /**
     * 默认编解码类型（MQTT 使用 Alink 协议）
     */
    protected static final String DEFAULT_CODEC_TYPE = "Alink";

    protected final IotMqttConnectionManager connectionManager;
    protected final IotDeviceMessageService deviceMessageService;

    // TODO @AI：这个是否需要？？？！！！
    /**
     * 是否需要连接已认证（默认 true）
     * <p>
     * 仅 IotMqttConnectionHandler 覆盖为 false
     *
     * @return 是否需要连接已认证
     */
    protected boolean requiresAuthenticated() {
        return true;
    }

    // TODO @AI：不确定，是不是基于 method 就可以算出来 reply topic ？？？！！！
    // TODO @AI：需要传递 seriabler 序列对象，不是通过 deviceMessageService.encodeDeviceMessage 获取到合适的；
    /**
     * 发送成功响应到设备
     *
     * @param endpoint    MQTT 连接端点
     * @param productKey  产品 Key
     * @param deviceName  设备名称
     * @param requestId   请求 ID
     * @param method      方法名
     * @param data        响应数据
     */
    protected void sendSuccessResponse(MqttEndpoint endpoint, String productKey, String deviceName,
                                        String requestId, String method, Object data) {
        try {
            // 1. 构建响应消息
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, data, 0, null);

            // 2. 编码消息（使用默认编解码器）
            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, DEFAULT_CODEC_TYPE);

            // 3. 构建响应主题并发送
            String replyTopic = IotMqttTopicUtils.buildTopicByMethod(method, productKey, deviceName, true);
            endpoint.publish(replyTopic, Buffer.buffer(encodedData), MqttQoS.AT_LEAST_ONCE, false, false);
            log.debug("[sendSuccessResponse][发送成功响应，主题: {}]", replyTopic);
        } catch (Exception e) {
            log.error("[sendSuccessResponse][发送成功响应异常，客户端 ID: {}]", endpoint.clientIdentifier(), e);
        }
    }

    /**
     * 发送错误响应到设备
     *
     * @param endpoint     MQTT 连接端点
     * @param productKey   产品 Key
     * @param deviceName   设备名称
     * @param requestId    请求 ID
     * @param method       方法名
     * @param errorMessage 错误消息
     */
    protected void sendErrorResponse(MqttEndpoint endpoint, String productKey, String deviceName,
                                      String requestId, String method, String errorMessage) {
        try {
            // 1. 构建响应消息
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, null, 400, errorMessage);

            // 2. 编码消息（使用默认编解码器）
            byte[] encodedData = deviceMessageService.encodeDeviceMessage(responseMessage, DEFAULT_CODEC_TYPE);

            // 3. 构建响应主题并发送
            String replyTopic = IotMqttTopicUtils.buildTopicByMethod(method, productKey, deviceName, true);
            endpoint.publish(replyTopic, Buffer.buffer(encodedData), MqttQoS.AT_LEAST_ONCE, false, false);
            log.debug("[sendErrorResponse][发送错误响应，主题: {}]", replyTopic);
        } catch (Exception e) {
            log.error("[sendErrorResponse][发送错误响应异常，客户端 ID: {}]", endpoint.clientIdentifier(), e);
        }
    }

    // TODO @AI：搞个基础的 writeResponse 会不会更好？参考 IotTcpUpstreamHandler 里；

}
