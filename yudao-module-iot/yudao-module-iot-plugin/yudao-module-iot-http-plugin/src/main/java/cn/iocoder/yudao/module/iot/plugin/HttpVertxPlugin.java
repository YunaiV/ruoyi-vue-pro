package cn.iocoder.yudao.module.iot.plugin;

import cn.iocoder.yudao.module.iot.api.ServiceRegistry;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpVertxPlugin extends Plugin {

    private static final int PORT = 8092;
    private Vertx vertx;
    private DeviceDataApi deviceDataApi;

    public HttpVertxPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("HttpVertxPlugin.start()");

        // 获取 DeviceDataApi 实例
        deviceDataApi = ServiceRegistry.getService(DeviceDataApi.class);
        if (deviceDataApi == null) {
            log.error("未能从 ServiceRegistry 获取 DeviceDataApi 实例，请确保主程序已正确注册！");
            return;
        }

        // 初始化 Vert.x
        vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        // 处理 Body
        router.route().handler(BodyHandler.create());

        // 设置路由
        router.post("/sys/:productKey/:deviceName/thing/event/property/post")
                .handler(new HttpVertxHandler(deviceDataApi));

        // 启动 HTTP 服务器
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(PORT, http -> {
                    if (http.succeeded()) {
                        log.info("HTTP 服务器启动成功，端口为: {}", PORT);
                    } else {
                        log.error("HTTP 服务器启动失败", http.cause());
                    }
                });
    }

    @Override
    public void stop() {
        log.info("HttpVertxPlugin.stop()");
        if (vertx != null) {
            vertx.close(ar -> {
                if (ar.succeeded()) {
                    log.info("Vert.x 关闭成功");
                } else {
                    log.error("Vert.x 关闭失败", ar.cause());
                }
            });
        }
    }
}