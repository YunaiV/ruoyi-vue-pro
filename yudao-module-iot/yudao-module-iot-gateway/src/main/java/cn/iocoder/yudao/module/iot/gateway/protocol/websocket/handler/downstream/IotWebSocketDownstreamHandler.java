package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.downstream;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 WebSocket 下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotWebSocketDownstreamHandler {

    private final IotMessageSerializer serializer;

    private final IotWebSocketConnectionManager connectionManager;

    /**
     * 处理下行消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 1. 获取连接信息
            IotWebSocketConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfoByDeviceId(
                    message.getDeviceId());
            if (connectionInfo == null) {
                log.error("[handle][连接信息不存在，设备 ID: {}]", message.getDeviceId());
                return;
            }

            // 2. 编码消息并发送到设备
            byte[] bytes = serializer.serialize(message);
            // TODO @AI：参考别的模块的做法，直接发？类似 tcp 这种；
            boolean success;
            if (serializer.getType() == IotSerializeTypeEnum.BINARY) {
                success = connectionManager.sendToDevice(message.getDeviceId(), bytes);
            } else {
                String jsonMessage = StrUtil.utf8Str(bytes);
                success = connectionManager.sendToDevice(message.getDeviceId(), jsonMessage);
            }
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
