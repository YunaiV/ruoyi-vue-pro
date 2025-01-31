package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertyGetReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IOT 设备服务获取 Vertx Handler
 *
 * 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDevicePropertyGetVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/service/property/get";

    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDevicePropertyGetReqDTO reqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            String requestId = body.getString("requestId");
            List<String> identifiers = (List<String>) body.getMap().get("identifiers");
            reqDTO = ((IotDevicePropertyGetReqDTO) new IotDevicePropertyGetReqDTO()
                    .setRequestId(requestId).setProductKey(productKey).setDeviceName(deviceName))
                    .setIdentifiers(identifiers);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(BAD_REQUEST));
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.getDeviceProperty(reqDTO);
            IotPluginCommonUtils.writeJson(routingContext, result);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 属性获取异常]", reqDTO, e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

}
