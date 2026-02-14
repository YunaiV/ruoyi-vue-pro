package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties.ProtocolProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.IotProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.downstream.IotHttpDownstreamSubscriber;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream.IotHttpAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream.IotHttpRegisterHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream.IotHttpRegisterSubHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream.IotHttpUpstreamHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT HTTP 协议实现
 * <p>
 * 基于 Vert.x 实现 HTTP 服务器，接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotHttpProtocol implements IotProtocol {

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
     * HTTP 服务器
     */
    private HttpServer httpServer;

    /**
     * 下行消息订阅者
     */
    private IotHttpDownstreamSubscriber downstreamSubscriber;

    public IotHttpProtocol(ProtocolProperties properties) {
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(properties.getPort());
    }

    @Override
    public String getId() {
        return properties.getId();
    }

    @Override
    public IotProtocolTypeEnum getType() {
        return IotProtocolTypeEnum.HTTP;
    }

    @Override
    public void start() {
        if (running) {
            log.warn("[start][IoT HTTP 协议 {} 已经在运行中]", getId());
            return;
        }

        // 1.1 创建 Vertx 实例
        this.vertx = Vertx.vertx();

        // 1.2 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 1.3 创建处理器，添加路由处理器
        IotHttpAuthHandler authHandler = new IotHttpAuthHandler(this);
        router.post(IotHttpAuthHandler.PATH).handler(authHandler);
        IotHttpRegisterHandler registerHandler = new IotHttpRegisterHandler();
        router.post(IotHttpRegisterHandler.PATH).handler(registerHandler);
        IotHttpRegisterSubHandler registerSubHandler = new IotHttpRegisterSubHandler();
        router.post(IotHttpRegisterSubHandler.PATH).handler(registerSubHandler);
        IotHttpUpstreamHandler upstreamHandler = new IotHttpUpstreamHandler(this);
        router.post(IotHttpUpstreamHandler.PATH).handler(upstreamHandler);

        // 1.4 启动 HTTP 服务器
        HttpServerOptions options = new HttpServerOptions().setPort(properties.getPort());
        IotGatewayProperties.SslConfig sslConfig = properties.getSsl();
        if (sslConfig != null && Boolean.TRUE.equals(sslConfig.getSsl())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions()
                    .setKeyPath(sslConfig.getSslKeyPath())
                    .setCertPath(sslConfig.getSslCertPath());
            options = options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }
        try {
            httpServer = vertx.createHttpServer(options)
                    .requestHandler(router)
                    .listen()
                    .result();
            running = true;
            log.info("[start][IoT HTTP 协议 {} 启动成功，端口：{}，serverId：{}]",
                    getId(), properties.getPort(), serverId);

            // 2. 启动下行消息订阅者
            IotMessageBus messageBus = SpringUtil.getBean(IotMessageBus.class);
            this.downstreamSubscriber = new IotHttpDownstreamSubscriber(this, messageBus);
            this.downstreamSubscriber.start();
        } catch (Exception e) {
            log.error("[start][IoT HTTP 协议 {} 启动失败]", getId(), e);
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
                log.info("[stop][IoT HTTP 协议 {} 下行消息订阅者已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT HTTP 协议 {} 下行消息订阅者停止失败]", getId(), e);
            }
            downstreamSubscriber = null;
        }

        // 2.1 关闭 HTTP 服务器
        if (httpServer != null) {
            try {
                httpServer.close().result();
                log.info("[stop][IoT HTTP 协议 {} 服务器已停止]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT HTTP 协议 {} 服务器停止失败]", getId(), e);
            }
            httpServer = null;
        }
        // 2.2 关闭 Vertx 实例
        if (vertx != null) {
            try {
                vertx.close().result();
                log.info("[stop][IoT HTTP 协议 {} Vertx 已关闭]", getId());
            } catch (Exception e) {
                log.error("[stop][IoT HTTP 协议 {} Vertx 关闭失败]", getId(), e);
            }
            vertx = null;
        }
        running = false;
        log.info("[stop][IoT HTTP 协议 {} 已停止]", getId());
    }

}
