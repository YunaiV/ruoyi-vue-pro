package cn.iocoder.yudao.module.iot.plugin.http.upstream.router;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceStateUpdateReqDTO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
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

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDeviceEventReportReqDTO reportReqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            String identifier = routingContext.pathParam("identifier");
            JsonObject body = routingContext.body().asJsonObject();
            String id = ObjUtil.defaultIfBlank(body.getString("id"), IdUtil.fastSimpleUUID());
            Map<String, Object> params = (Map<String, Object>) body.getMap().get("params");
            reportReqDTO = ((IotDeviceEventReportReqDTO)
                    new IotDeviceEventReportReqDTO().setRequestId(id)
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(productKey).setDeviceName(deviceName))
                    .setIdentifier(identifier).setParams(params);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(BAD_REQUEST));
            return;
        }

        try {
            // 2. 设备上线
            deviceUpstreamApi.updateDeviceState(((IotDeviceStateUpdateReqDTO)
                    new IotDeviceStateUpdateReqDTO().setRequestId(IdUtil.fastSimpleUUID())
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(reportReqDTO.getProductKey()).setDeviceName(reportReqDTO.getDeviceName()))
                    .setState(IotDeviceStateEnum.ONLINE.getState()));

            // 3.1 属性上报
            CommonResult<Boolean> result = deviceUpstreamApi.reportDeviceEvent(reportReqDTO);
            // 3.2 返回结果
            IotPluginCommonUtils.writeJson(routingContext, result);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 时间上报异常]", reportReqDTO, e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

}
