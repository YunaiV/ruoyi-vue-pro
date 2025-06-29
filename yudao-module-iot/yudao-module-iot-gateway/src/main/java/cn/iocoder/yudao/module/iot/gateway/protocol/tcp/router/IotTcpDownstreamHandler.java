package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IoT 网关 TCP 下行消息处理器
 * <p>
 * 从消息总线接收到下行消息，然后发布到 TCP 连接，从而被设备所接收
 *
 * @author 芋道源码
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotTcpDownstreamHandler {

    private final IotTcpConnectionManager connectionManager;

    private final IotDeviceMessageService messageService;

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     */
    public void handle(IotDeviceMessage message) {
        // 1. 获取设备对应的连接
        NetSocket socket = connectionManager.getConnection(String.valueOf(message.getDeviceId()));
        if (socket == null) {
            log.error("[handle][设备({})的连接不存在]", message.getDeviceId());
            return;
        }

        // 2. 编码消息
        byte[] bytes = messageService.encodeDeviceMessage(message, null, null);

        // 3. 发送消息
        socket.write(Buffer.buffer(bytes));
        // TODO @芋艿：这里的换行符，需要和设备端约定
        // TODO @haohao：tcp 要不定长？很少 \n 哈。然后有个 magic number；可以参考 dubbo rpc；
        socket.write("\n");
    }

}