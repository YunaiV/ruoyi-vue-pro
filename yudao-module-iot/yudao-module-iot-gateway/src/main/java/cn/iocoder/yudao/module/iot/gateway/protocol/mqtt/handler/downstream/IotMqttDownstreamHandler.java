package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.handler.downstream;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;

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
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 1. 检查设备连接
            IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfoByDeviceId(
                    message.getDeviceId());
            if (connectionInfo == null) {
                log.warn("[handle][连接信息不存在，设备 ID: {}，方法: {}，消息 ID: {}]",
                        message.getDeviceId(), message.getMethod(), message.getId());
                return;
            }

            // 2.1 序列化消息
            byte[] payload = deviceMessageService.serializeDeviceMessage(message, connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName());
            Assert.isTrue(payload != null && payload.length > 0, "消息编码结果不能为空");
            // 2.2 构建主题
            Assert.notBlank(message.getMethod(), "消息方法不能为空");
            boolean isReply = IotDeviceMessageUtils.isReplyMessage(message);
            String topic = IotMqttTopicUtils.buildTopicByMethod(message.getMethod(), connectionInfo.getProductKey(),
                    connectionInfo.getDeviceName(), isReply);
            Assert.notBlank(topic, "主题不能为空");

            // 3. 发送到设备
            boolean success = connectionManager.sendToDevice(message.getDeviceId(), topic, payload,
                    MqttQoS.AT_LEAST_ONCE.value(), false);
            if (!success) {
                throw new RuntimeException("下行消息发送失败");
            }
            log.info("[handle][下行消息发送成功，设备 ID: {}，方法: {}，消息 ID: {}，主题: {}，数据长度: {} 字节]",
                    message.getDeviceId(), message.getMethod(), message.getId(), topic, payload.length);
        } catch (Exception e) {
            log.error("[handle][处理下行消息失败，设备 ID: {}，方法: {}，消息内容: {}]",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

    /**
     * 处理透传下行（raw.push 或编码脚本的输出）：原始字节发布到 raw/push 主题
     *
     * @param message 下行消息
     * @param bytes   要写出的原始字节
     * @return 提交结果：设备离线或发送失败时失败；成功只表示 publish 已提交，晚到的写出失败由连接管理器记日志
     */
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes) {
        // 1. 检查设备连接
        IotMqttConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfoByDeviceId(
                message.getDeviceId());
        if (connectionInfo == null) {
            log.warn("[handleRawMessage][连接信息不存在，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());
            return Future.failedFuture(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
        }

        // 2. 发布到 raw/push 主题
        String topic = IotMqttTopicUtils.buildRawPushTopic(connectionInfo.getProductKey(), connectionInfo.getDeviceName());
        boolean success = connectionManager.sendToDevice(message.getDeviceId(), topic, bytes,
                MqttQoS.AT_LEAST_ONCE.value(), false);
        return success ? Future.succeededFuture() : Future.failedFuture(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
    }

}
