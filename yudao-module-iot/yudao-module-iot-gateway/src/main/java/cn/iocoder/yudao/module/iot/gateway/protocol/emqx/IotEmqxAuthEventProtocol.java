package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.router.IotEmqxAuthEventHandler;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 EMQX 认证事件协议服务
 * <p>
 * 为 EMQX 提供 HTTP 接口服务，包括：
 * 1. 设备认证接口 - 对应 EMQX HTTP 认证插件
 * 2. 设备事件处理接口 - 对应 EMQX Webhook 事件通知
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxAuthEventProtocol {

    private final IotGatewayProperties.EmqxProperties emqxProperties;

    private final String serverId;

    private final Vertx vertx;

    private HttpServer httpServer;

    public IotEmqxAuthEventProtocol(IotGatewayProperties.EmqxProperties emqxProperties,
                                    Vertx vertx) {
        this.emqxProperties = emqxProperties;
        this.vertx = vertx;
        this.serverId = IotDeviceMessageUtils.generateServerId(emqxProperties.getMqttPort());
    }

    @PostConstruct
    public void start() {
        try {
            startHttpServer();
            log.info("[start][IoT 网关 EMQX 认证事件协议服务启动成功, 端口: {}]", emqxProperties.getHttpPort());
        } catch (Exception e) {
            log.error("[start][IoT 网关 EMQX 认证事件协议服务启动失败]", e);
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        stopHttpServer();
        log.info("[stop][IoT 网关 EMQX 认证事件协议服务已停止]");
    }

    /**
     * 启动 HTTP 服务器
     */
    private void startHttpServer() {
        int port = emqxProperties.getHttpPort();

        // 1. 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 2. 创建处理器，传入 serverId
        IotEmqxAuthEventHandler handler = new IotEmqxAuthEventHandler(serverId);
        router.post(IotMqttTopicUtils.MQTT_AUTH_PATH).handler(handler::handleAuth);
        router.post(IotMqttTopicUtils.MQTT_EVENT_PATH).handler(handler::handleEvent);
        // TODO @haohao：/mqtt/acl 需要处理么？
        // TODO @芋艿：已在 EMQX 处理，如果是“设备直连”模式需要处理

        // 3. 启动 HTTP 服务器
        try {
            httpServer = vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(port)
                    .result();
        } catch (Exception e) {
            log.error("[startHttpServer][HTTP 服务器启动失败, 端口: {}]", port, e);
            throw e;
        }
    }

    /**
     * 停止 HTTP 服务器
     */
    private void stopHttpServer() {
        if (httpServer == null) {
            return;
        }

        try {
            httpServer.close().result();
            log.info("[stopHttpServer][HTTP 服务器已停止]");
        } catch (Exception e) {
            log.error("[stopHttpServer][HTTP 服务器停止失败]", e);
        }
    }

}