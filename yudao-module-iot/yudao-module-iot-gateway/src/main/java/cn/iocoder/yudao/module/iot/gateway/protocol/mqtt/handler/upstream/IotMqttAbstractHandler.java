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

    protected final IotMqttConnectionManager connectionManager;
    protected final IotDeviceMessageService deviceMessageService;

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
    @SuppressWarnings("SameParameterValue")
    protected void sendSuccessResponse(MqttEndpoint endpoint, String productKey, String deviceName,
                                       String requestId, String method, Object data) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, data, 0, null);
        writeResponse(endpoint, productKey, deviceName, method, responseMessage);
    }

    /**
     * 发送错误响应到设备
     *
     * @param endpoint     MQTT 连接端点
     * @param productKey   产品 Key
     * @param deviceName   设备名称
     * @param requestId    请求 ID
     * @param method       方法名
     * @param errorCode    错误码
     * @param errorMessage 错误消息
     */
    protected void sendErrorResponse(MqttEndpoint endpoint, String productKey, String deviceName,
                                      String requestId, String method, Integer errorCode, String errorMessage) {
        IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(requestId, method, null, errorCode, errorMessage);
        writeResponse(endpoint, productKey, deviceName, method, responseMessage);
    }

    /**
     * 写入响应消息到设备
     *
     * @param endpoint        MQTT 连接端点
     * @param productKey      产品 Key
     * @param deviceName      设备名称
     * @param method          方法名
     * @param responseMessage 响应消息
     */
    private void writeResponse(MqttEndpoint endpoint, String productKey, String deviceName,
                               String method, IotDeviceMessage responseMessage) {
        try {
            // 1.1 序列化消息（根据设备配置的序列化类型）
            byte[] encodedData = deviceMessageService.serializeDeviceMessage(responseMessage, productKey, deviceName);
            // 1.2 构建响应主题
            String replyTopic = IotMqttTopicUtils.buildTopicByMethod(method, productKey, deviceName, true);

            // 2. 发送响应消息
            endpoint.publish(replyTopic, Buffer.buffer(encodedData), MqttQoS.AT_LEAST_ONCE, false, false);
            log.debug("[writeResponse][发送响应，主题: {}，code: {}]", replyTopic, responseMessage.getCode());
        } catch (Exception e) {
            log.error("[writeResponse][发送响应异常，客户端 ID: {}]", endpoint.clientIdentifier(), e);
        }
    }

}
