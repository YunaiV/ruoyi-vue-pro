package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertyGetReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.pojo.IotStandardResponse;
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
 * IoT 设备服务获取 Vertx Handler
 *
 * 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDevicePropertyGetVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/service/property/get";
    public static final String METHOD = "thing.service.property.get";

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
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    null, METHOD, BAD_REQUEST.getCode(), BAD_REQUEST.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.getDeviceProperty(reqDTO);

            // 3. 响应结果
            IotStandardResponse response;
            if (result.isSuccess()) {
                response = IotStandardResponse.success(reqDTO.getRequestId(), METHOD, result.getData());
            } else {
                response = IotStandardResponse.error(reqDTO.getRequestId(), METHOD, result.getCode(), result.getMsg());
            }
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 属性获取异常]", reqDTO, e);
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    reqDTO.getRequestId(), METHOD, INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }

}
