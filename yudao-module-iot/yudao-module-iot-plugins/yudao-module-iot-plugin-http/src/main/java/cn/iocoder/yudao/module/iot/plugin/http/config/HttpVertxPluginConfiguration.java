package cn.iocoder.yudao.module.iot.plugin.http.config;

import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.plugin.http.service.HttpVertxHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 插件与独立运行都可复用的配置类
 */
@Slf4j
@Configuration
public class HttpVertxPluginConfiguration {

    // TODO @haohao：这个要不要搞个配置类，更容易维护；
    /**
     * 可在 application.yml 中配置，默认端口 8092
     */
    @Value("${plugin.http.server.port:8092}")
    private Integer port;

    /**
     * 创建 Vert.x 实例
     */
    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    /**
     * 创建路由
     */
    @Bean
    public Router router(Vertx vertx, HttpVertxHandler httpVertxHandler) {
        Router router = Router.router(vertx);

        // 处理 Body
        router.route().handler(BodyHandler.create());

        // 设置路由
        // TODO @haohao：这个后续，我们是多个 Handler ，还是一个哈？
        router.post("/sys/:productKey/:deviceName/thing/event/property/post")
              .handler(httpVertxHandler);

        return router;
    }

    @Bean
    public HttpVertxHandler httpVertxHandler(DeviceDataApi deviceDataApi) {
        return new HttpVertxHandler(deviceDataApi);
    }

    /**
     * 定义一个 VertxService 来管理服务器启动逻辑
     * 无论是独立运行还是插件方式，都可以共用此类
     */
    @Bean
    public VertxService vertxService(Vertx vertx, Router router) {
        return new VertxService(port, vertx, router);
    }

}
