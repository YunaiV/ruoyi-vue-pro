package cn.iocoder.yudao.module.iot.net.component.http.upstream;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.net.component.http.config.IotNetComponentHttpProperties;
import cn.iocoder.yudao.module.iot.net.component.http.upstream.router.IotDeviceUpstreamVertxHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

/**
 * IoT 设备上行服务器
 * <p>
 * 处理设备通过 HTTP 方式接入的上行消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceUpstreamServer extends AbstractVerticle {

    /**
     * Vert.x 实例
     */
    private final Vertx vertx;

    /**
     * HTTP 组件配置属性
     */
    private final IotNetComponentHttpProperties httpProperties;

    /**
     * 设备上行 API
     */
    private final IotDeviceUpstreamApi deviceUpstreamApi;

    /**
     * Spring 应用上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * 构造函数
     *
     * @param vertx              Vert.x 实例
     * @param httpProperties     HTTP 组件配置属性
     * @param deviceUpstreamApi  设备上行 API
     * @param applicationContext Spring 应用上下文
     */
    public IotDeviceUpstreamServer(
            @Lazy Vertx vertx,
            IotNetComponentHttpProperties httpProperties,
            IotDeviceUpstreamApi deviceUpstreamApi,
            ApplicationContext applicationContext) {
        this.vertx = vertx;
        this.httpProperties = httpProperties;
        this.deviceUpstreamApi = deviceUpstreamApi;
        this.applicationContext = applicationContext;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        // 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 创建处理器
        IotDeviceUpstreamVertxHandler upstreamHandler = new IotDeviceUpstreamVertxHandler(
                deviceUpstreamApi, applicationContext);

        // 添加路由处理器
        router.post(IotDeviceUpstreamVertxHandler.PROPERTY_PATH).handler(upstreamHandler::handle);
        router.post(IotDeviceUpstreamVertxHandler.EVENT_PATH).handler(upstreamHandler::handle);

        // 启动 HTTP 服务器
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(httpProperties.getServerPort(), result -> {
                    if (result.succeeded()) {
                        log.info("[start][IoT 设备上行服务器启动成功，端口:{}]", httpProperties.getServerPort());
                        startPromise.complete();
                    } else {
                        log.error("[start][IoT 设备上行服务器启动失败]", result.cause());
                        startPromise.fail(result.cause());
                    }
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        log.info("[stop][IoT 设备上行服务器已停止]");
        stopPromise.complete();
    }
}
