package cn.iocoder.yudao.module.iot.gateway.protocol.udp.router;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotUdpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.udp.manager.IotUdpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.datagram.DatagramSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 UDP 下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpDownstreamHandler {

    private final IotDeviceMessageService deviceMessageService;

    private final IotUdpSessionManager sessionManager;

    private final IotUdpUpstreamProtocol protocol;

    public IotUdpDownstreamHandler(IotDeviceMessageService deviceMessageService,
                                   IotUdpSessionManager sessionManager,
                                   IotUdpUpstreamProtocol protocol) {
        this.deviceMessageService = deviceMessageService;
        this.sessionManager = sessionManager;
        this.protocol = protocol;
    }

    /**
     * 处理下行消息
     *
     * @param message 下行消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 1. 获取会话信息（包含 codecType）
            IotUdpSessionManager.SessionInfo sessionInfo = sessionManager.getSessionInfo(message.getDeviceId());
            if (sessionInfo == null) {
                log.warn("[handle][设备不在线，设备 ID: {}]", message.getDeviceId());
                return;
            }

            // 2. 使用会话中的 codecType 编码消息，并发送到设备
            byte[] bytes = deviceMessageService.encodeDeviceMessage(message, sessionInfo.getCodecType());
            DatagramSocket socket = protocol.getUdpSocket();
            if (socket == null) {
                log.error("[handle][UDP Socket 不可用，设备 ID: {}]", message.getDeviceId());
                return;
            }
            boolean success = sessionManager.sendToDevice(message.getDeviceId(), bytes, socket);
            if (success) {
                log.info("[handle][下行消息发送成功，设备 ID: {}，方法: {}，消息 ID: {}，数据长度: {} 字节]",
                        message.getDeviceId(), message.getMethod(), message.getId(), bytes.length);
            } else {
                log.error("[handle][下行消息发送失败，设备 ID: {}，方法: {}，消息 ID: {}]",
                        message.getDeviceId(), message.getMethod(), message.getId());
            }
        } catch (Exception e) {
            log.error("[handle][处理下行消息失败，设备 ID: {}，方法: {}，消息内容: {}]",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

}
