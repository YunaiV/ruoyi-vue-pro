package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttHttpAuthHandler;
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
 * IoT 网关 MQTT HTTP 认证协议：提供 HTTP 认证接口供 EMQX 调用
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttHttpAuthProtocol extends AbstractVerticle {

    private final IotGatewayProperties.EmqxProperties emqxProperties;

    private HttpServer httpServer;
    private Vertx vertx;

    @Override
    @PostConstruct
    public void start() {
        log.info("[start][开始启动 MQTT HTTP 认证协议服务]");

        // 创建 Vertx 实例
        this.vertx = Vertx.vertx();

        // 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 创建 MQTT 认证处理器
        IotMqttHttpAuthHandler authHandler = new IotMqttHttpAuthHandler();

        // 添加 MQTT 认证路由
        router.post("/mqtt/auth/authenticate").handler(authHandler::authenticate);
        router.post("/mqtt/auth/connected").handler(authHandler::connected);
        router.post("/mqtt/auth/disconnected").handler(authHandler::disconnected);

        // 启动 HTTP 服务器，使用独立端口
        int authPort = getAuthPort();
        try {
            httpServer = vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(authPort)
                    .result();
            log.info("[start][MQTT HTTP 认证协议启动成功，端口：{}]", authPort);
        } catch (Exception e) {
            log.error("[start][MQTT HTTP 认证协议启动失败]", e);
            throw e;
        }
    }

    @Override
    @PreDestroy
    public void stop() {
        if (httpServer != null) {
            try {
                httpServer.close().result();
                log.info("[stop][MQTT HTTP 认证协议已停止]");
            } catch (Exception e) {
                log.error("[stop][MQTT HTTP 认证协议停止失败]", e);
            }
        }

        if (vertx != null) {
            try {
                vertx.close();
                log.info("[stop][Vertx 已关闭]");
            } catch (Exception e) {
                log.warn("[stop][关闭 Vertx 异常]", e);
            }
        }
    }

    /**
     * 获取认证服务端口
     * 从配置中获取，默认使用 8090 端口
     */
    private int getAuthPort() {
        return emqxProperties.getHttpAuthPort() != null ? emqxProperties.getHttpAuthPort() : 8090;
    }

    public String getServerId() {
        return "mqtt_auth_gateway_" + getAuthPort();
    }
}