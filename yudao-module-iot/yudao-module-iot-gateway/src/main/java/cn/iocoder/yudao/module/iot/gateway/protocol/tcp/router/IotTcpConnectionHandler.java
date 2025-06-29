package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT TCP 连接处理器
 * <p>
 * 核心负责：
 * 1. 【认证】创建连接后，设备需要发送认证消息，认证通过后，才能进行后续的通信
 * 2. 【消息处理】接收设备发送的消息，解码后，发送到消息队列
 * 3. 【断开】设备断开连接后，清理资源
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotTcpConnectionHandler implements Handler<Buffer> {

    private final NetSocket socket;
    /**
     * 是否已认证
     */
    private boolean authenticated = false;
    /**
     * 设备信息
     */
    private IotDeviceRespDTO device;

    private final IotTcpConnectionManager connectionManager;

    private final IotDeviceMessageService messageService;

    private final IotDeviceService deviceService;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    public void start() {
        // 1. 设置解析器
        final RecordParser parser = RecordParser.newDelimited("\n", this);
        socket.handler(parser);

        // 2. 设置处理器
        socket.closeHandler(v -> handleConnectionClose());
        socket.exceptionHandler(this::handleException);
    }

    @Override
    public void handle(Buffer buffer) {
        log.info("[handle][接收到数据: {}]", buffer);
        try {
            // 1. 处理认证
            if (!authenticated) {
                handleAuthentication(buffer);
                return;
            }
            // 2. 处理消息
            handleMessage(buffer);
        } catch (Exception e) {
            log.error("[handle][处理异常]", e);
            socket.close();
        }
    }

    private void handleAuthentication(Buffer buffer) {
        // 1. 解析认证信息
        // TODO @芋艿：这里的认证协议，需要和设备端约定。默认为 productKey,deviceName,password
        String[] parts = buffer.toString().split(",");
        if (parts.length != 3) {
            log.error("[handleAuthentication][认证信息({})格式不正确]", buffer);
            socket.close();
            return;
        }
        String productKey = parts[0];
        String deviceName = parts[1];
        String password = parts[2];

        // 2. 执行认证
        CommonResult<Boolean> authResult = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                .setClientId(socket.remoteAddress().toString()).setUsername(productKey + "/" + deviceName)
                .setPassword(password));
        if (authResult.isError() || !BooleanUtil.isTrue(authResult.getData())) {
            log.error("[handleAuthentication][认证失败，productKey({}) deviceName({}) password({})]", productKey, deviceName,
                    password);
            socket.close();
            return;
        }

        // 3. 认证成功
        this.authenticated = true;
        this.device = deviceService.getDeviceFromCache(productKey, deviceName);
        connectionManager.addConnection(String.valueOf(device.getId()), socket);

        // 4. 发送上线消息
        IotDeviceMessage message = IotDeviceMessage.buildStateUpdateOnline();
        messageService.sendDeviceMessage(message, productKey, deviceName, serverId);
        log.info("[handleAuthentication][认证成功]");
    }

    private void handleMessage(Buffer buffer) {
        // 1. 解码消息
        IotDeviceMessage message = messageService.decodeDeviceMessage(buffer.getBytes(),
                device.getProductKey(), device.getDeviceName());
        if (message == null) {
            log.warn("[handleMessage][解码消息失败]");
            return;
        }
        // 2. 发送消息到队列
        messageService.sendDeviceMessage(message, device.getProductKey(), device.getDeviceName(), serverId);
    }

    private void handleConnectionClose() {
        // 1. 移除连接
        connectionManager.removeConnection(socket);
        // 2. 发送离线消息
        if (device != null) {
            IotDeviceMessage message = IotDeviceMessage.buildStateOffline();
            messageService.sendDeviceMessage(message, device.getProductKey(), device.getDeviceName(), serverId);
        }
    }

    private void handleException(Throwable e) {
        log.error("[handleException][连接({}) 发生异常]", socket.remoteAddress(), e);
        socket.close();
    }

}