package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.router.IotWebSocketUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 WebSocket 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotWebSocketUpstreamProtocol {

    private final IotGatewayProperties.WebSocketProperties wsProperties;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final IotWebSocketConnectionManager connectionManager;

    private final Vertx vertx;

    @Getter
    private final String serverId;

    private HttpServer httpServer;

    public IotWebSocketUpstreamProtocol(IotGatewayProperties.WebSocketProperties wsProperties,
                                        IotDeviceService deviceService,
                                        IotDeviceMessageService messageService,
                                        IotWebSocketConnectionManager connectionManager,
                                        Vertx vertx) {
        this.wsProperties = wsProperties;
        this.deviceService = deviceService;
        this.messageService = messageService;
        this.connectionManager = connectionManager;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(wsProperties.getPort());
    }

    @PostConstruct
    @SuppressWarnings("deprecation")
    public void start() {
        // 1.1 创建服务器选项
        HttpServerOptions options = new HttpServerOptions()
                .setPort(wsProperties.getPort())
                .setIdleTimeout(wsProperties.getIdleTimeoutSeconds())
                .setMaxWebSocketFrameSize(wsProperties.getMaxFrameSize())
                .setMaxWebSocketMessageSize(wsProperties.getMaxMessageSize());
        // 1.2 配置 SSL（如果启用）
        if (Boolean.TRUE.equals(wsProperties.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(wsProperties.getSslKeyPath())
                    .setCertPath(wsProperties.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 2. 创建服务器并设置 WebSocket 处理器
        httpServer = vertx.createHttpServer(options);
        httpServer.webSocketHandler(socket -> {
            // 验证路径
            if (ObjUtil.notEqual(wsProperties.getPath(), socket.path())) {
                log.warn("[webSocketHandler][WebSocket 路径不匹配，拒绝连接，路径: {}，期望: {}]",
                        socket.path(), wsProperties.getPath());
                socket.reject();
                return;
            }
            // 创建上行处理器
            IotWebSocketUpstreamHandler handler = new IotWebSocketUpstreamHandler(this,
                    messageService, deviceService, connectionManager);
            handler.handle(socket);
        });

        // 3. 启动服务器
        try {
            httpServer.listen().result();
            log.info("[start][IoT 网关 WebSocket 协议启动成功，端口：{}，路径：{}]", wsProperties.getPort(), wsProperties.getPath());
        } catch (Exception e) {
            log.error("[start][IoT 网关 WebSocket 协议启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (httpServer != null) {
            try {
                httpServer.close().result();
                log.info("[stop][IoT 网关 WebSocket 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 WebSocket 协议停止失败]", e);
            }
        }
    }

}
