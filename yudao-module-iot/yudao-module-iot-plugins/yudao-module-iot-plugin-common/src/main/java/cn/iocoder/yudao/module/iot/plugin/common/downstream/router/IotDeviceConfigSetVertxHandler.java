package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDeviceConfigSetReqDTO;
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
 * IoT 设备配置设置 Vertx Handler
 *
 * 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDeviceConfigSetVertxHandler implements Handler<RoutingContext> {

    // TODO @haohao：是不是可以把 PATH、Method 所有的，抽到一个枚举类里？因为 topic、path、method 相当于不同的几个表达？
    public static final String PATH = "/sys/:productKey/:deviceName/thing/service/config/set";
    public static final String METHOD = "thing.service.config.set";

    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDeviceConfigSetReqDTO reqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            String requestId = body.getString("requestId");
            Map<String, Object> config = (Map<String, Object>) body.getMap().get("config");
            reqDTO = ((IotDeviceConfigSetReqDTO) new IotDeviceConfigSetReqDTO()
                    .setRequestId(requestId).setProductKey(productKey).setDeviceName(deviceName))
                    .setConfig(config);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    null, METHOD, BAD_REQUEST.getCode(), BAD_REQUEST.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.setDeviceConfig(reqDTO);

            // 3. 响应结果
            IotStandardResponse response = result.isSuccess() ?
                    IotStandardResponse.success(reqDTO.getRequestId(), METHOD, result.getData())
                    : IotStandardResponse.error(reqDTO.getRequestId(), METHOD, result.getCode(), result.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 配置设置异常]", reqDTO, e);
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    reqDTO.getRequestId(), METHOD, INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }

}
