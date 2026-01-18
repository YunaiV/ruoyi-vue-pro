package cn.iocoder.yudao.module.iot.gateway.protocol.mqttws;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.manager.IotMqttWsConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqttws.router.IotMqttWsUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 MQTT WebSocket 协议：接收设备上行消息
 * <p>
 * 基于 Vert.x 实现 MQTT over WebSocket 服务端，支持：
 * - 标准 MQTT 3.1.1 协议
 * - WebSocket 协议升级
 * - SSL/TLS 加密（wss://）
 * - 设备认证与连接管理
 * - QoS 0/1/2 消息质量保证
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttWsUpstreamProtocol {

    private final IotGatewayProperties.MqttWsProperties mqttWsProperties;

    private final IotDeviceMessageService messageService;

    private final IotMqttWsConnectionManager connectionManager;

    private final Vertx vertx;

    @Getter
    private final String serverId;

    private HttpServer httpServer;

    public IotMqttWsUpstreamProtocol(IotGatewayProperties.MqttWsProperties mqttWsProperties,
                                     IotDeviceMessageService messageService,
                                     IotMqttWsConnectionManager connectionManager,
                                     Vertx vertx) {
        this.mqttWsProperties = mqttWsProperties;
        this.messageService = messageService;
        this.connectionManager = connectionManager;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(mqttWsProperties.getPort());
    }

    @PostConstruct
    public void start() {
        // 创建 HTTP 服务器选项
        HttpServerOptions options = new HttpServerOptions()
                .setPort(mqttWsProperties.getPort())
                .setIdleTimeout(mqttWsProperties.getKeepAliveTimeoutSeconds())
                .setMaxWebSocketFrameSize(mqttWsProperties.getMaxFrameSize())
                .setMaxWebSocketMessageSize(mqttWsProperties.getMaxMessageSize())
                // 配置 WebSocket 子协议支持
                .addWebSocketSubProtocol(mqttWsProperties.getSubProtocol());

        // 配置 SSL（如果启用）
        if (Boolean.TRUE.equals(mqttWsProperties.getSslEnabled())) {
            options.setSsl(true)
                    .setKeyCertOptions(mqttWsProperties.getSslOptions().getKeyCertOptions())
                    .setTrustOptions(mqttWsProperties.getSslOptions().getTrustOptions());
            log.info("[start][MQTT WebSocket 已启用 SSL/TLS (wss://)]");
        }

        // 创建 HTTP 服务器
        httpServer = vertx.createHttpServer(options);

        // 设置 WebSocket 处理器
        httpServer.webSocketHandler(this::handleWebSocketConnection);

        // 启动服务器
        try {
            httpServer.listen().result();
            log.info("[start][IoT 网关 MQTT WebSocket 协议启动成功，端口: {}，路径: {}，支持子协议: {}]",
                    mqttWsProperties.getPort(), mqttWsProperties.getPath(),
                    "mqtt, mqttv3.1, " + mqttWsProperties.getSubProtocol());
        } catch (Exception e) {
            log.error("[start][IoT 网关 MQTT WebSocket 协议启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (httpServer != null) {
            try {
                // 关闭所有连接
                connectionManager.closeAllConnections();

                // 关闭服务器
                httpServer.close().result();
                log.info("[stop][IoT 网关 MQTT WebSocket 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 MQTT WebSocket 协议停止失败]", e);
            }
        }
    }

    /**
     * 处理 WebSocket 连接请求
     *
     * @param socket WebSocket 连接
     */
    private void handleWebSocketConnection(ServerWebSocket socket) {
        String path = socket.path();
        String subProtocol = socket.subProtocol();

        log.info("[handleWebSocketConnection][收到 WebSocket 连接请求，path: {}，subProtocol: {}，remoteAddress: {}]",
                path, subProtocol, socket.remoteAddress());

        // 验证路径
        if (!mqttWsProperties.getPath().equals(path)) {
            log.warn("[handleWebSocketConnection][WebSocket 路径不匹配，拒绝连接，path: {}，期望: {}]",
                    path, mqttWsProperties.getPath());
            socket.close();
            return;
        }

        // 验证子协议
        // Vert.x 已经自动进行了子协议协商，这里只需要验证是否为 MQTT 相关协议
        if (subProtocol != null && !subProtocol.startsWith("mqtt")) {
            log.warn("[handleWebSocketConnection][WebSocket 子协议不支持，拒绝连接，subProtocol: {}]", subProtocol);
            socket.close();
            return;
        }

        log.info("[handleWebSocketConnection][WebSocket 连接已接受，remoteAddress: {}，subProtocol: {}]",
                socket.remoteAddress(), subProtocol);

        // 创建处理器并处理连接
        IotMqttWsUpstreamHandler handler = new IotMqttWsUpstreamHandler(
                this, messageService, connectionManager);
        handler.handle(socket);
    }

}
