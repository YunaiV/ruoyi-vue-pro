package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.handler.downstream;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.RAW_NOT_SUPPORTED;

/**
 * IoT 网关 TCP 下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotTcpDownstreamHandler {

    private final IotTcpConnectionManager connectionManager;

    /**
     * TCP 帧编解码器（处理粘包/拆包）
     */
    private final IotTcpFrameCodec codec;
    /**
     * 消息序列化器（处理业务消息序列化/反序列化）
     */
    private final IotMessageSerializer serializer;

    /**
     * 处理下行消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            // 1.1 检查是否是属性设置消息
            if (ObjUtil.equals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), message.getMethod())) {
                return;
            }
            if (ObjUtil.notEqual(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), message.getMethod())) {
                log.warn("[handle][忽略非属性设置消息: {}]", message.getMethod());
                return;
            }
            log.info("[handle][处理下行消息，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());
            // 1.2 检查设备连接
            IotTcpConnectionManager.ConnectionInfo connectionInfo = connectionManager.getConnectionInfoByDeviceId(
                    message.getDeviceId());
            if (connectionInfo == null) {
                log.warn("[handle][连接信息不存在，设备 ID: {}，方法: {}，消息 ID: {}]",
                        message.getDeviceId(), message.getMethod(), message.getId());
                return;
            }

            // 2. 序列化 + 帧编码
            byte[] payload = serializer.serialize(message);
            Buffer frameData = codec.encode(payload);

            // 3. 发送到设备
            boolean success = connectionManager.sendToDevice(message.getDeviceId(), frameData.getBytes());
            if (!success) {
                throw new RuntimeException("下行消息发送失败");
            }
            log.info("[handle][下行消息发送成功，设备 ID: {}，方法: {}，消息 ID: {}，数据长度: {} 字节]",
                    message.getDeviceId(), message.getMethod(), message.getId(), frameData.length());
        } catch (Exception e) {
            log.error("[handle][处理下行消息失败，设备 ID: {}，方法: {}，消息内容: {}]",
                    message.getDeviceId(), message.getMethod(), message, e);
        }
    }

    /**
     * 处理透传下行（raw.push 或编码脚本的输出）：原始字节按 codec 加帧后写出
     * <p>
     * 只有认证时声明了 mode=raw 的透传连接才支持；普通 JSON / 二进制连接返回 RAW_NOT_SUPPORTED
     *
     * @param message 下行消息
     * @param bytes   要写出的原始字节
     * @return 真实的写出结果
     */
    public Future<Void> handleRawMessage(IotDeviceMessage message, byte[] bytes) {
        // 1. 检查设备连接：之后只写这个检查过的连接（期间设备换成新连接时写出失败，不写到未校验的新连接上）
        NetSocket socket = connectionManager.getSocketByDeviceId(message.getDeviceId());
        IotTcpConnectionManager.ConnectionInfo connectionInfo = socket != null ? connectionManager.getConnectionInfo(socket) : null;
        if (connectionInfo == null) {
            log.warn("[handleRawMessage][连接信息不存在，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());
            return Future.failedFuture(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
        }
        if (!Boolean.TRUE.equals(connectionInfo.getRawMode())) {
            log.warn("[handleRawMessage][非透传连接，不支持透传下发，设备 ID: {}，方法: {}，消息 ID: {}]",
                    message.getDeviceId(), message.getMethod(), message.getId());
            return Future.failedFuture(exception(RAW_NOT_SUPPORTED));
        }

        // 2. 帧编码 + 发送到设备
        Buffer frameData;
        try {
            frameData = codec.encode(bytes);
        } catch (Exception e) {
            log.warn("[handleRawMessage][帧编码失败，设备 ID: {}，数据长度: {} 字节，错误: {}]",
                    message.getDeviceId(), bytes.length, e.getMessage());
            return Future.failedFuture(exception(DEVICE_OFFLINE_OR_WRITE_FAILED));
        }
        return connectionManager.sendToConnectionAsync(message.getDeviceId(), socket, frameData.getBytes());
    }

}