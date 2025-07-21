package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpCodecManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 上行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpUpstreamHandler implements Handler<NetSocket> {

    private final IotDeviceMessageService deviceMessageService;

    private final String serverId;

    private final IotTcpCodecManager codecManager;

    public IotTcpUpstreamHandler(IotTcpUpstreamProtocol protocol, IotDeviceMessageService deviceMessageService,
                                 IotTcpCodecManager codecManager) {
        this.deviceMessageService = deviceMessageService;
        this.serverId = protocol.getServerId();
        this.codecManager = codecManager;
    }

    @Override
    public void handle(NetSocket socket) {
        // 生成客户端ID用于日志标识
        String clientId = IdUtil.simpleUUID();
        log.info("[handle][收到设备连接] clientId: {}, address: {}", clientId, socket.remoteAddress());

        // 设置解析器
        RecordParser parser = RecordParser.newFixed(1024, buffer -> {
            try {
                handleDataPackage(clientId, buffer);
            } catch (Exception e) {
                log.error("[handle][处理数据包异常] clientId: {}", clientId, e);
            }
        });

        // 设置异常处理
        socket.exceptionHandler(ex -> {
            log.error("[handle][连接异常] clientId: {}, address: {}", clientId, socket.remoteAddress(), ex);
        });

        socket.closeHandler(v -> {
            log.info("[handle][连接关闭] clientId: {}, address: {}", clientId, socket.remoteAddress());
        });

        // 设置数据处理器
        socket.handler(parser);
    }

    /**
     * 处理数据包
     */
    private void handleDataPackage(String clientId, Buffer buffer) {
        try {
            // 使用编解码器管理器自动检测协议并解码消息
            IotDeviceMessage message = codecManager.decode(buffer.getBytes());
            log.info("[handleDataPackage][接收数据包] clientId: {}, 方法: {}, 设备ID: {}",
                    clientId, message.getMethod(), message.getDeviceId());

            // 处理上行消息
            handleUpstreamMessage(clientId, message);
        } catch (Exception e) {
            log.error("[handleDataPackage][处理数据包失败] clientId: {}", clientId, e);
        }
    }

    /**
     * 处理上行消息
     */
    private void handleUpstreamMessage(String clientId, IotDeviceMessage message) {
        try {
            log.info("[handleUpstreamMessage][上行消息] clientId: {}, 方法: {}, 设备ID: {}",
                    clientId, message.getMethod(), message.getDeviceId());

            // 解析设备信息（简化处理）
            String deviceId = String.valueOf(message.getDeviceId());
            String productKey = extractProductKey(deviceId);
            String deviceName = deviceId;

            // 发送消息到队列
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
        } catch (Exception e) {
            log.error("[handleUpstreamMessage][处理上行消息失败] clientId: {}", clientId, e);
        }
    }

    /**
     * 从设备ID中提取产品密钥（简化实现）
     */
    private String extractProductKey(String deviceId) {
        // 简化实现：假设设备ID格式为 "productKey_deviceName"
        if (deviceId != null && deviceId.contains("_")) {
            return deviceId.split("_")[0];
        }
        return "default_product";
    }
}