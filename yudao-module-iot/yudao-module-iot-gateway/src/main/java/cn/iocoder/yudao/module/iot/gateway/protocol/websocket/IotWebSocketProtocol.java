package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.downstream.IotWebSocketDownstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.downstream.IotWebSocketDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.handler.upstream.IotWebSocketUpstreamHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.websocket.manager.IotWebSocketConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializerManager;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT WebSocket 协议实现
 * <p>
 * 基于 Vert.x 实现 WebSocket 服务器，接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotWebSocketProtocol implements IotProtocol {

    /**
     * 协议配置
     */
    private final ProtocolProperties properties;
    /**
     * 服务器 ID（用于消息追踪，全局唯一）
     */
    @Getter
    private final String serverId;

    /**
     * 运行状态
     */
    @Getter
    private volatile boolean running = false;

    /**
     * Vert.x 实例
     */
    private Vertx vertx;
    /**
     * WebSocket 服务器
     */
    private HttpServer httpServer;
    /**
     * WebSocket 连接管理器
     */
    private final IotWebSocketConnectionManager connectionManager;

    /**
     * 下行消息订阅者
     */
    private IotWebSocketDownstreamSubscriber downstreamSubscriber;

    /**
     * 消息序列化器
     */
    private final IotMessageSerializer serializer;

    public IotWebSocketProtocol(ProtocolProperties properties) {
        Assert.notNull(properties, "协议实例配置不能为空");
        Assert.notNull(properties.getWebsocket(), "WebSocket 协议配置（websocket）不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());

        // 初始化序列化器
        IotSerializeTypeEnum serializeType = IotSerializeTypeEnum.of(properties.getSerialize());
        Assert.notNull(serializeType, "不支持的序列化类型：" + properties.getSerialize());
        IotMessageSerializerManager serializerManager = SpringUtil.getBean(IotMessageSerializerManager.class);
        this.serializer = serializerManager.get(serializeType);

        // 初始化连接管理器
        this.connectionManager = new IotWebSocketConnectionManager();

    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.WEBSOCKET;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start() {
        if (running) {
            log.warn("[start][IoT WebSocket 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例
        this.vertx = Vertx.vertx();

        // 1.2 创建服务器选项
        IotWebSocketConfig wsConfig = properties.getWebsocket();
        HttpServerOptions options = new HttpServerOptions()
                .setPort(properties.getPort())
                .setIdleTimeout(wsConfig.getIdleTimeoutSeconds())
                .setMaxWebSocketFrameSize(wsConfig.getMaxFrameSize())
                .setMaxWebSocketMessageSize(wsConfig.getMaxMessageSize());
        IotGatewayProperties.SslConfig sslConfig = properties.getSsl();
        if (sslConfig != null && Boolean.TRUE.equals(sslConfig.getSsl())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(sslConfig.getSslKeyPath())
                    .setCertPath(sslConfig.getSslCertPath());
            options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }

        // 1.3 创建服务器并设置 WebSocket 处理器
        httpServer = vertx.createHttpServer(options);
        httpServer.webSocketHandler(socket -> {
            // 验证路径
            if (ObjUtil.notEqual(wsConfig.getPath(), socket.path())) {
                log.warn("[webSocketHandler][WebSocket 路径不匹配，拒绝连接，路径: {}，期望: {}]",
                        socket.path(), wsConfig.getPath());
                socket.reject();
                return;
            }
            // 创建上行处理器
            IotWebSocketUpstreamHandler handler = new IotWebSocketUpstreamHandler(serverId, serializer, connectionManager);
            handler.handle(socket);
        });

        // 1.4 启动服务器
        try {
            httpServer.listen().result();
            running = true;
            log.info("[start][IoT WebSocket 协议 {} 启动成功，端口：{}，路径：{}，serverId：{}]",
                    getId(), properties.getPort(), wsConfig.getPath(), serverId);

            // 2. 启动下行消息订阅者
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            IotWebSocketDownstreamHandler downstreamHandler = new IotWebSocketDownstreamHandler(serializer, connectionManager);
            this.downstreamSubscriber = new IotWebSocketDownstreamSubscriber(this, downstreamHandler, messageBus);
            downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT WebSocket 协议 {} 启动失败]", getId(), e);
            stop0();
            throw e;
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        stop0();
    }

    private void stop0() {
        // 1. 停止下行消息订阅者
        if (downstreamSubscriber != null) {
            try {
                downstreamSubscriber.stop();
                log.info("[stop][IoT WebSocket 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT WebSocket 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 关闭所有连接
        connectionManager.closeAll();
        // 2.2 关闭 WebSocket 服务器
        if (httpServer != null) {
            try {
                httpServer.close().result();
                log.info("[stop][IoT WebSocket 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT WebSocket 协议 {} 服务器停止失败]", getId(), e);
            }
            httpServer = null;
        }
        // 2.3 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT WebSocket 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT WebSocket 协议 {} Vertx 关闭失败]", getId(), e);
            }
            vertx = null;
        }
        running = false;
        log.info("[stop][IoT WebSocket 协议 {} 已停止]", getId());
    }

}
