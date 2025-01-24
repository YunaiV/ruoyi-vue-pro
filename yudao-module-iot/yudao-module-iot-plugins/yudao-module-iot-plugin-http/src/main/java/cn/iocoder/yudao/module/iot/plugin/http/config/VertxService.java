package cn.iocoder.yudao.module.iot.plugin.http.config;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

/**
 * 封装 Vert.x HTTP 服务的启动/关闭逻辑
 */
@Slf4j
public class VertxService {

    private final Integer port;
    private final Vertx vertx;
    private final Router router;

    public VertxService(Integer port, Vertx vertx, Router router) {
        this.port = port;
        this.vertx = vertx;
        this.router = router;
    }

    /**
     * 启动 HTTP 服务器
     */
    public void startServer() {
        vertx.createHttpServer()
             .requestHandler(router)
             .listen(port, http -> {
                 if (http.succeeded()) {
                     log.info("[VertxService] HTTP 服务器启动成功, 端口: {}", port);
                 } else {
                     log.error("[VertxService] HTTP 服务器启动失败", http.cause());
                 }
             });
    }

    /**
     * 关闭 HTTP 服务器
     */
    public void stopServer() {
        if (vertx != null) {
            vertx.close(ar -> {
                if (ar.succeeded()) {
                    log.info("[VertxService] Vert.x 关闭成功");
                } else {
                    log.error("[VertxService] Vert.x 关闭失败", ar.cause());
                }
            });
        }
    }
}
