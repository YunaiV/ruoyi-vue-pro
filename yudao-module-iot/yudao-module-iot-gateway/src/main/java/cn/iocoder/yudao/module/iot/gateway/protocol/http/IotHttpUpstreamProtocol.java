package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpRegisterHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpRegisterSubHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpUpstreamHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * IoT 网关 HTTP 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotHttpUpstreamProtocol {

    private final IotGatewayProperties.HttpProperties httpProperties;

    private final Vertx vertx;

    private HttpServer httpServer;

    @Getter
    private final String serverId;

    public IotHttpUpstreamProtocol(IotGatewayProperties.HttpProperties httpProperties, Vertx vertx) {
        this.httpProperties = httpProperties;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(httpProperties.getServerPort());
    }

    @PostConstruct
    public void start() {
        // 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 创建处理器，添加路由处理器
        IotHttpAuthHandler authHandler = new IotHttpAuthHandler(this);
        router.post(IotHttpAuthHandler.PATH).handler(authHandler);
        IotHttpRegisterHandler registerHandler = new IotHttpRegisterHandler();
        router.post(IotHttpRegisterHandler.PATH).handler(registerHandler);
        IotHttpRegisterSubHandler registerSubHandler = new IotHttpRegisterSubHandler();
        router.post(IotHttpRegisterSubHandler.PATH).handler(registerSubHandler);
        IotHttpUpstreamHandler upstreamHandler = new IotHttpUpstreamHandler(this);
        router.post(IotHttpUpstreamHandler.PATH).handler(upstreamHandler);

        // 启动 HTTP 服务器
        HttpServerOptions options = new HttpServerOptions()
                .setPort(httpProperties.getServerPort());
        if (Boolean.TRUE.equals(httpProperties.getSslEnabled())) {
            PemKeyCertOptions pemKeyCertOptions = new PemKeyCertOptions().setKeyPath(httpProperties.getSslKeyPath())
                    .setCertPath(httpProperties.getSslCertPath());
            options = options.setSsl(true).setKeyCertOptions(pemKeyCertOptions);
        }
        try {
            httpServer = vertx.createHttpServer(options)
                    .requestHandler(router)
                    .listen()
                    .result();
            log.info("[start][IoT 网关 HTTP 协议启动成功，端口：{}]", httpProperties.getServerPort());
        } catch (Exception e) {
            log.error("[start][IoT 网关 HTTP 协议启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (httpServer != null) {
            try {
                httpServer.close().result();
                log.info("[stop][IoT 网关 HTTP 协议已停止]");
            } catch (Exception e) {
                log.error("[stop][IoT 网关 HTTP 协议停止失败]", e);
            }
        }
    }

}
