package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router.IotTcpConnectionHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotTcpUpstreamProtocol {

    private final Vertx vertx;

    private final IotGatewayProperties gatewayProperties;

    private final IotTcpConnectionManager connectionManager;

    private final IotDeviceMessageService messageService;

    private final IotDeviceService deviceService;

    private final IotDeviceCommonApi deviceApi;

    @Getter
    private String serverId;

    private NetServer netServer;

    @PostConstruct
    public void start() {
        // 1. 初始化参数
        IotGatewayProperties.TcpProperties tcpProperties = gatewayProperties.getProtocol().getTcp();
        this.serverId = IotDeviceMessageUtils.generateServerId(tcpProperties.getServerPort());

        // 2. 创建 TCP 服务器
        netServer = vertx.createNetServer();
        netServer.connectHandler(socket -> {
            new IotTcpConnectionHandler(socket, connectionManager,
                    messageService, deviceService, deviceApi, serverId).start();
        });

        // 3. 启动 TCP 服务器
        netServer.listen(tcpProperties.getServerPort(), "0.0.0.0")
                .onSuccess(server -> log.info("[start][IoT 网关 TCP 服务启动成功，端口：{}]", server.actualPort()))
                .onFailure(e -> log.error("[start][IoT 网关 TCP 服务启动失败]", e));
    }

    @PreDestroy
    public void stop() {
        if (netServer != null) {
            netServer.close()
                    .onSuccess(v -> log.info("[stop][IoT 网关 TCP 服务已停止]"))
                    .onFailure(e -> log.error("[stop][IoT 网关 TCP 服务停止失败]", e));
        }
    }

}