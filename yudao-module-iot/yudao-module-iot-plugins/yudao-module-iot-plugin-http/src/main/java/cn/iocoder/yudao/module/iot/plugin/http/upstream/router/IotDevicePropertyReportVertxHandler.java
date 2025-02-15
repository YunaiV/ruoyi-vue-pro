package cn.iocoder.yudao.module.iot.plugin.http.upstream.router;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDevicePropertyReportReqDTO;
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

// TODO @芋艿：【待定 005】要不要简化成，解析后，统一处理？只有一个 Handler！！！
/**
 * IoT 设备属性上报的 Vert.x Handler
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotDevicePropertyReportVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/sys/:productKey/:deviceName/thing/event/property/post";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDevicePropertyReportReqDTO reportReqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            String id = ObjUtil.defaultIfBlank(body.getString("id"), IdUtil.fastSimpleUUID());
            Map<String, Object> properties = (Map<String, Object>) body.getMap().get("properties");
            reportReqDTO = ((IotDevicePropertyReportReqDTO)
                    new IotDevicePropertyReportReqDTO().setRequestId(id)
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(productKey).setDeviceName(deviceName))
                    .setProperties(properties);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(BAD_REQUEST));
            return;
        }

        // TODO @芋艿：secret 校验。目前的想法：
        //      方案一：请求的时候，带上 secret 参数，然后进行校验，减少请求的频次。不过可能要看下 mqtt 能不能复用！
        //      方案二：本地有设备信息的缓存，异步刷新。这样可能 mqtt 的校验，和 http 校验都容易适配。

        try {
            // 2. 设备上线
            deviceUpstreamApi.updateDeviceState(((IotDeviceStateUpdateReqDTO)
                    new IotDeviceStateUpdateReqDTO().setRequestId(IdUtil.fastSimpleUUID())
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(reportReqDTO.getProductKey()).setDeviceName(reportReqDTO.getDeviceName()))
                    .setState(IotDeviceStateEnum.ONLINE.getState()));

            // 3.1 属性上报
            CommonResult<Boolean> result = deviceUpstreamApi.reportDeviceProperty(reportReqDTO);
            // 3.2 返回结果
            IotPluginCommonUtils.writeJson(routingContext, result);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 属性上报异常]", reportReqDTO, e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

}
