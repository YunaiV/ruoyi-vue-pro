package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT 协议：下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotMqttDownstreamHandler {

    private final IotDeviceMessageService deviceMessageService;

    private final IotMqttConnectionManager connectionManager;

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     * @return 是否处理成功
     */
    public boolean handleDownstreamMessage(IotDeviceMessage message) {
        try {
            // TODO @AI：参考 IotTcpDownstreamHandler 逻辑；
            // 1. 基础校验
            if (message == null || message.getDeviceId() == null) {
                log.warn("[handleDownstreamMessage][消息或设备 ID 为空，忽略处理]");
                return false;
            }

            // 2. 检查设备是否在线
            // TODO @AI：这块逻辑，是不是冗余？直接使用 3. 获取连接信息判断不就行了？
            if (connectionManager.isDeviceOffline(message.getDeviceId())) {
                log.warn("[handleDownstreamMessage][设备离线，无法发送消息，设备 ID：{}]", message.getDeviceId());
                return false;
            }

            // 3. 获取连接信息
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfoByDeviceId(message.getDeviceId());
            if (connectionInfo == null) {
                log.warn("[handleDownstreamMessage][连接信息不存在，设备 ID：{}]", message.getDeviceId());
                return false;
            }

            // 4. 序列化
            byte[] payload = deviceMessageService.encodeDeviceMessage(message, connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName());
            if (payload == null || payload.length == 0) {
                log.warn("[handleDownstreamMessage][消息编码失败，设备 ID：{}]", message.getDeviceId());
                return false;
            }

            // 5. 发送消息到设备
            // TODO @AI：参考 IotTcpDownstreamHandler 的逻辑；
            return sendMessageToDevice(message, connectionInfo, payload);
        } catch (Exception e) {
            if (message != null) {
                log.error("[handleDownstreamMessage][处理下行消息异常，设备 ID：{}，错误：{}]",
                        message.getDeviceId(), e.getMessage(), e);
            }
            return false;
        }
    }

    // TODO @AI 是不是合并到 handleDownstreamMessage 里；
    /**
     * 发送消息到设备
     *
     * @param message        设备消息
     * @param connectionInfo 连接信息
     * @param payload        消息负载
     * @return 是否发送成功
     */
    private boolean sendMessageToDevice(IotDeviceMessage message,
                                        IotMqttConnectionManager.ConnectionInfo connectionInfo,
                                        byte[] payload) {
        // 1. 构建主题
        String topic = buildDownstreamTopic(message, connectionInfo);
        // TODO @AI：直接断言，非空！
        if (StrUtil.isBlank(topic)) {
            log.warn("[sendMessageToDevice][主题构建失败，设备 ID：{}，方法：{}]",
                    message.getDeviceId(), message.getMethod());
            return false;
        }

        // 2. 发送消息
        boolean success = connectionManager.sendToDevice(message.getDeviceId(), topic, payload, MqttQoS.AT_LEAST_ONCE.value(), false);
        if (success) {
            log.debug("[sendMessageToDevice][消息发送成功，设备 ID：{}，主题：{}，方法：{}]",
                    message.getDeviceId(), topic, message.getMethod());
        } else {
            log.warn("[sendMessageToDevice][消息发送失败，设备 ID：{}，主题：{}，方法：{}]",
                    message.getDeviceId(), topic, message.getMethod());
        }
        return success;
    }

    /**
     * 构建下行消息主题
     *
     * @param message        设备消息
     * @param connectionInfo 连接信息
     * @return 主题
     */
    private String buildDownstreamTopic(IotDeviceMessage message,
                                        IotMqttConnectionManager.ConnectionInfo connectionInfo) {
        // TODO @AI：直接断言，非空！
        String method = message.getMethod();
        if (StrUtil.isBlank(method)) {
            return null;
        }

        // 使用工具类构建主题，支持回复消息处理
        boolean isReply = IotDeviceMessageUtils.isReplyMessage(message);
        return IotMqttTopicUtils.buildTopicByMethod(method, connectionInfo.getProductKey(),
                connectionInfo.getDeviceName(), isReply);
    }

}
