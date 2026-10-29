package cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.downstream;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import io.vertx.core.Future;
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

            // 2. 序列化
            byte[] bytes = serializer.serialize(message);
            String bytesContent = StrUtil.utf8Str(bytes);

            // 3. 发送到设备
            boolean success = connectionManager.sendToDevice(connectionInfo.getDeviceId(), bytesContent);
            if (!success) {
                throw new RuntimeException("下行消息发送失败");
            }
            log.info("[handle][下行消息发送成功，设备 ID: {}，方法: {}，消息 ID: {}，数据长度: {} 字节]",
                    message.getDeviceId(), message.getMethod(), message.getId(), bytes.length);
        } catch (Exception e) {
            log.error("[handle][处理下行消息失败，设备 ID: {}，方法: {}，消息内容: {}]",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

    /**
     * 处理透传下行（raw.push 或编码脚本的输出）：原始字节以 binary 帧写出
     * <p>
     * 设备未连接时，由 {@link IotWebSocketConnectionManager#sendBinaryToDeviceAsync} 返回失败
     *
     * @param message 下行消息
     * @param bytes   要写出的原始字节
     * @return 真实的写出结果
     */
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes) {
        return connectionManager.sendBinaryToDeviceAsync(message.getDeviceId(), bytes);
    }

}
