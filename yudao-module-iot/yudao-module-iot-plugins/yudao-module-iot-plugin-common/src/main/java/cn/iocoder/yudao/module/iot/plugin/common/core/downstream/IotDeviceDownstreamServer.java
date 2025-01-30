package cn.iocoder.yudao.module.iot.plugin.common.core.downstream;

import cn.iocoder.yudao.module.iot.plugin.common.core.downstream.router.IotDeviceServiceInvokeVertxHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IotDeviceDownstreamServer {

    private final Vertx vertx;
    private final HttpServer server;

    public IotDeviceDownstreamServer(IotDeviceDownstreamHandler deviceDownstreamHandler) {
        // 创建 Vertx 实例
        this.vertx = Vertx.vertx();
        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body
        router.post(IotDeviceServiceInvokeVertxHandler.PATH).handler(
                new IotDeviceServiceInvokeVertxHandler(deviceDownstreamHandler)); // 处理 Service Invoke
        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);
    }

    public void start() {
        log.info("[start][开始启动]");
        server.listen(0) // 通过 0 自动选择端口
              .toCompletionStage()
              .toCompletableFuture()
              .join();
        log.info("[start][启动完成，端口({})]", this.server.actualPort());
    }

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
