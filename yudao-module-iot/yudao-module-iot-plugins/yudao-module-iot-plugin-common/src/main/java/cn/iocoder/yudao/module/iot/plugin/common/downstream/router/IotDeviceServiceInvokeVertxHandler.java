package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDeviceServiceInvokeReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.pojo.IotStandardResponse;
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
 * IoT 设备服务调用 Vertx Handler
 *
 * 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDeviceServiceInvokeVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/service/:identifier";
    public static final String METHOD_PREFIX = "thing.service.";
    public static final String METHOD_SUFFIX = "";

    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDeviceServiceInvokeReqDTO reqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            String identifier = routingContext.pathParam("identifier");
            JsonObject body = routingContext.body().asJsonObject();
            String requestId = body.getString("requestId");
            Map<String, Object> params = (Map<String, Object>) body.getMap().get("params");
            reqDTO = ((IotDeviceServiceInvokeReqDTO) new IotDeviceServiceInvokeReqDTO()
                    .setRequestId(requestId).setProductKey(productKey).setDeviceName(deviceName))
                    .setIdentifier(identifier).setParams(params);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            String method = METHOD_PREFIX + routingContext.pathParam("identifier") + METHOD_SUFFIX;
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    null, method, BAD_REQUEST.getCode(), BAD_REQUEST.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.invokeDeviceService(reqDTO);

            // 3. 响应结果
            String method = METHOD_PREFIX + reqDTO.getIdentifier() + METHOD_SUFFIX;
            IotStandardResponse response;
            if (result.isSuccess()) {
                response = IotStandardResponse.success(reqDTO.getRequestId(), method, result.getData());
            } else {
                response = IotStandardResponse.error(reqDTO.getRequestId(), method, result.getCode(), result.getMsg());
            }
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 服务调用异常]", reqDTO, e);
            String method = METHOD_PREFIX + reqDTO.getIdentifier() + METHOD_SUFFIX;
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    reqDTO.getRequestId(), method, INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }

}
