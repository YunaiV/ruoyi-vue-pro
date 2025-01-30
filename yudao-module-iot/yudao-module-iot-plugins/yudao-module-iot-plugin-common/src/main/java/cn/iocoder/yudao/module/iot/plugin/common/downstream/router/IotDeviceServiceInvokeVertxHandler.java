package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class IotDeviceServiceInvokeVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/service/:identifier";

    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    @Override
    public void handle(RoutingContext routingContext) {
        // TODO 芋艿：这里没实现
        deviceDownstreamHandler.invokeDeviceService(null);
    }

}
