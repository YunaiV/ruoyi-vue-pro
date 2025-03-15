package cn.iocoder.yudao.module.iot.plugin.http.upstream;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.http.config.IotPluginHttpProperties;
import cn.iocoder.yudao.module.iot.plugin.http.upstream.router.IotDeviceUpstreamVertxHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 设备下行服务端，接收来自 device 设备的请求，转发给 server 服务器
 *
 * 协议：HTTP
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamServer {

    private final Vertx vertx;
    private final HttpServer server;
    private final IotPluginHttpProperties properties;

    public IotDeviceUpstreamServer(IotPluginHttpProperties properties,
                                   IotDeviceUpstreamApi deviceUpstreamApi) {
        this.properties = properties;
        // 创建 Vertx 实例
        this.vertx = Vertx.vertx();
        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body

        // 使用统一的 Handler 处理所有上行请求
        IotDeviceUpstreamVertxHandler upstreamHandler = new IotDeviceUpstreamVertxHandler(deviceUpstreamApi);
        router.post(IotDeviceUpstreamVertxHandler.PROPERTY_PATH).handler(upstreamHandler);
        router.post(IotDeviceUpstreamVertxHandler.EVENT_PATH).handler(upstreamHandler);

        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);
    }

    /**
     * 启动 HTTP 服务器
     */
    public void start() {
        log.info("[start][开始启动]");
        server.listen(properties.getServerPort())
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
}
