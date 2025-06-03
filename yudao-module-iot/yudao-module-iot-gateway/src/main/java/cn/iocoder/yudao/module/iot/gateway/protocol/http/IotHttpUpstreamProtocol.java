package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpAuthHandler;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpUpstreamHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 HTTP 协议：接收设备上行消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotHttpUpstreamProtocol extends AbstractVerticle {

    private final IotGatewayProperties.HttpProperties httpProperties;

    private HttpServer httpServer;

    @Override
    @PostConstruct
    public void start() {
        // 创建路由
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 创建处理器，添加路由处理器
        IotHttpUpstreamHandler upstreamHandler = new IotHttpUpstreamHandler(this);
        router.post(IotHttpUpstreamHandler.PROPERTY_PATH).handler(upstreamHandler);
        router.post(IotHttpUpstreamHandler.EVENT_PATH).handler(upstreamHandler);
        IotHttpAuthHandler authHandler = new IotHttpAuthHandler(this);
        router.post(IotHttpAuthHandler.PATH).handler(authHandler);

        // 启动 HTTP 服务器
        try {
            httpServer = vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(httpProperties.getServerPort())
                    .result();
            log.info("[start][IoT 网关 HTTP 协议启动成功，端口：{}]", httpProperties.getServerPort());
        } catch (Exception e) {
            log.error("[start][IoT 网关 HTTP 协议启动失败]", e);
            throw e;
        }
    }

    @Override
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

    public String getServerId() {
        return IotDeviceMessageUtils.generateServerId(httpProperties.getServerPort());
    }

}
