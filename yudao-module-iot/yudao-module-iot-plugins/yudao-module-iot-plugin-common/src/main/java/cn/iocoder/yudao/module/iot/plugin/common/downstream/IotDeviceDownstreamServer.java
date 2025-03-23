package cn.iocoder.yudao.module.iot.plugin.common.downstream;

import cn.iocoder.yudao.module.iot.plugin.common.config.IotPluginCommonProperties;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.router.*;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 设备下行服务端，接收来自 server 服务器的请求，转发给 device 设备
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceDownstreamServer {

    private final Vertx vertx;
    private final HttpServer server;
    private final IotPluginCommonProperties properties;

    public IotDeviceDownstreamServer(IotPluginCommonProperties properties,
                                     IotDeviceDownstreamHandler deviceDownstreamHandler) {
        this.properties = properties;
        // 创建 Vertx 实例
        this.vertx = Vertx.vertx();
        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body
        router.post(IotDeviceServiceInvokeVertxHandler.PATH)
                .handler(new IotDeviceServiceInvokeVertxHandler(deviceDownstreamHandler));
        router.post(IotDevicePropertySetVertxHandler.PATH)
                .handler(new IotDevicePropertySetVertxHandler(deviceDownstreamHandler));
        router.post(IotDevicePropertyGetVertxHandler.PATH)
                .handler(new IotDevicePropertyGetVertxHandler(deviceDownstreamHandler));
        router.post(IotDeviceConfigSetVertxHandler.PATH)
                .handler(new IotDeviceConfigSetVertxHandler(deviceDownstreamHandler));
        router.post(IotDeviceOtaUpgradeVertxHandler.PATH)
                .handler(new IotDeviceOtaUpgradeVertxHandler(deviceDownstreamHandler));
        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);
    }

    /**
     * 启动 HTTP 服务器
     */
    public void start() {
        log.info("[start][开始启动]");
        server.listen(properties.getDownstreamPort())
              .toCompletionStage()
              .toCompletableFuture()
              .join();
        log.info("[start][启动完成，端口({})]", this.server.actualPort());
    }

    /**
     * 停止所有
     */
    public void stop() {
        log.info("[stop][开始关闭]");
        try {
            // 关闭 HTTP 服务器
            if (server != null) {
                server.close()
                      .toCompletionStage()
                      .toCompletableFuture()
                      .join();
            }

            // 关闭 Vertx 实例
            if (vertx != null) {
                vertx.close()
                     .toCompletionStage()
                     .toCompletableFuture()
                     .join();
            }
            log.info("[stop][关闭完成]");
        } catch (Exception e) {
            log.error("[stop][关闭异常]", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得端口
     *
     * @return 端口
     */
    public int getPort() {
        return this.server.actualPort();
    }

}
