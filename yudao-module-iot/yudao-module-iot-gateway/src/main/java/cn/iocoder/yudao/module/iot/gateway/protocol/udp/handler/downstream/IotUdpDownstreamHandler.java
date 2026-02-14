package cn.iocoder.yudao.module.iot.gateway.protocol.udp.handler.downstream;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import io.vertx.core.datagram.DatagramSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 UDP 下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotUdpDownstreamHandler {

    private final IotUdpProtocol protocol;

    private final IotUdpSessionManager sessionManager;

    /**
     * 消息序列化器（处理业务消息序列化/反序列化）
     */
    private final IotMessageSerializer serializer;

    /**
     * 处理下行消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());
            // 1. 检查设备会话
            IotUdpSessionManager.SessionInfo sessionInfo = sessionManager.getSession(message.getDeviceId());
            if (sessionInfo == null) {
                log.warn("[handle][会话信息不存在，设备 ID: {}，方法: {}，消息 ID: {}]",
                        message.getDeviceId(), message.getMethod(), message.getId());
                return;
            }
            DatagramSocket socket = protocol.getUdpSocket();
            if (socket == null) {
                log.error("[handle][UDP Socket 不可用，设备 ID: {}]", message.getDeviceId());
                return;
            }

            // 2. 序列化消息
            byte[] serializedData = serializer.serialize(message);

            // 3. 发送到设备
            boolean success = sessionManager.sendToDevice(message.getDeviceId(), serializedData, socket);
            if (!success) {
                throw new RuntimeException("下行消息发送失败");
            }
            log.info("[handle][下行消息发送成功，设备 ID: {}，方法: {}，消息 ID: {}，数据长度: {} 字节]",
                    message.getDeviceId(), message.getMethod(), message.getId(), serializedData.length);
        } catch (Exception e) {
            log.error("[handle][处理下行消息失败，设备 ID: {}，方法: {}，消息内容: {}]",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

}
