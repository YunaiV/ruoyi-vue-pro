package cn.iocoder.yudao.module.iot.plugin.http.upstream.router;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceStateUpdateReqDTO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.plugin.common.pojo.IotStandardResponse;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 设备事件上报的 Vert.x Handler
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceEventReportVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/event/:identifier/post";
    private static final String VERSION = "1.0";
    private static final String EVENT_METHOD_PREFIX = "thing.event.";
    private static final String EVENT_METHOD_SUFFIX = ".post";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDeviceEventReportReqDTO reportReqDTO;
        String identifier = null;
        String requestId = IdUtil.fastSimpleUUID();
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            identifier = routingContext.pathParam("identifier");
            JsonObject body = routingContext.body().asJsonObject();
            requestId = ObjUtil.defaultIfBlank(body.getString("id"), requestId);

            // 按照标准JSON格式处理事件参数
            Map<String, Object> params;

            // 优先使用params字段，符合标准
            if (body.getJsonObject("params") != null) {
                params = body.getJsonObject("params").getMap();
            } else {
                // 兼容旧格式
                params = new HashMap<>();
            }

            reportReqDTO = ((IotDeviceEventReportReqDTO) new IotDeviceEventReportReqDTO().setRequestId(requestId)
                    .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                    .setProductKey(productKey).setDeviceName(deviceName))
                    .setIdentifier(identifier).setParams(params);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            // 使用IotStandardResponse实体类返回错误
            String method = identifier != null ? EVENT_METHOD_PREFIX + identifier + EVENT_METHOD_SUFFIX
                    : "thing.event.unknown.post";
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    requestId, method, BAD_REQUEST.getCode(), BAD_REQUEST.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
            return;
        }

        try {
            // 2. 设备上线
            deviceUpstreamApi.updateDeviceState(
                    ((IotDeviceStateUpdateReqDTO) new IotDeviceStateUpdateReqDTO().setRequestId(IdUtil.fastSimpleUUID())
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(reportReqDTO.getProductKey()).setDeviceName(reportReqDTO.getDeviceName()))
                            .setState(IotDeviceStateEnum.ONLINE.getState()));

            // 3.1 事件上报
            CommonResult<Boolean> result = deviceUpstreamApi.reportDeviceEvent(reportReqDTO);

            // 3.2 返回结果 - 使用IotStandardResponse实体类
            String method = EVENT_METHOD_PREFIX + reportReqDTO.getIdentifier() + EVENT_METHOD_SUFFIX;
            IotStandardResponse response;
            if (result.isSuccess()) {
                response = IotStandardResponse.success(reportReqDTO.getRequestId(), method, result.getData());
            } else {
                response = IotStandardResponse.error(
                        reportReqDTO.getRequestId(), method, result.getCode(), result.getMsg());
            }
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 事件上报异常]", reportReqDTO, e);

            // 构建错误响应 - 使用IotStandardResponse实体类
            String method = EVENT_METHOD_PREFIX + reportReqDTO.getIdentifier() + EVENT_METHOD_SUFFIX;
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    reportReqDTO.getRequestId(), method,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }
}
