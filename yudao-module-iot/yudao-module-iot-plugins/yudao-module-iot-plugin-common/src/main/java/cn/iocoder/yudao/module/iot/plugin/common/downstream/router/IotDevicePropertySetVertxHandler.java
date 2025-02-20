package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertySetReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 设备服务设置 Vertx Handler
 *
 * 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDevicePropertySetVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/service/property/set";

    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDevicePropertySetReqDTO reqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            String requestId = body.getString("requestId");
            Map<String, Object> properties = (Map<String, Object>) body.getMap().get("properties");
            reqDTO = ((IotDevicePropertySetReqDTO) new IotDevicePropertySetReqDTO()
                    .setRequestId(requestId).setProductKey(productKey).setDeviceName(deviceName))
                    .setProperties(properties);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(BAD_REQUEST));
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.setDeviceProperty(reqDTO);
            IotPluginCommonUtils.writeJson(routingContext, result);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 属性设置异常]", reqDTO, e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

}
