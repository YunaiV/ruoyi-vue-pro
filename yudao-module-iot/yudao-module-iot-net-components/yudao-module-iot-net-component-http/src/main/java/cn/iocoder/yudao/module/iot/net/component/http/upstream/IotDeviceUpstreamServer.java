package cn.iocoder.yudao.module.iot.net.component.http.upstream;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.net.component.http.config.IotNetComponentHttpProperties;
import cn.iocoder.yudao.module.iot.net.component.http.upstream.router.IotDeviceUpstreamVertxHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 设备上行服务器
 * <p>
 * 处理设备通过 HTTP 方式接入的上行消息
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceUpstreamServer extends AbstractVerticle {

    private final Vertx vertx;

    private final IotNetComponentHttpProperties httpProperties;

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    private final IotDeviceMessageProducer deviceMessageProducer;

    @Override
    public void start() {
        start(Promise.promise());
    }

    // TODO @haohao：这样貌似初始化不到；我临时拷贝上去了
    @Override
    public void start(Promise<Void> startPromise) {
        // 创建路由
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 创建处理器
        IotDeviceUpstreamVertxHandler upstreamHandler = new IotDeviceUpstreamVertxHandler(
                deviceUpstreamApi, deviceMessageProducer);

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
