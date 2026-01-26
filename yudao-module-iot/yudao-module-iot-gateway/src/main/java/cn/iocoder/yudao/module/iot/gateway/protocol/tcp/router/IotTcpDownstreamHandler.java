package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotTcpDownstreamHandler {

    private final IotDeviceMessageService deviceMessageService;

    private final IotTcpConnectionManager connectionManager;

    /**
     * 处理下行消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 1. 获取连接信息（包含 codecType）
            IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfoByDeviceId(
                    message.getDeviceId());
            if (connectionInfo == null) {
                log.error("[handle][连接信息不存在，设备 ID: {}]", message.getDeviceId());
                return;
            }

            // 2. 使用连接时的 codecType 编码消息，并发送到设备
            byte[] bytes = deviceMessageService.encodeDeviceMessage(message, connectionInfo.getCodecType());
            boolean success = connectionManager.sendToDevice(message.getDeviceId(), bytes);
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