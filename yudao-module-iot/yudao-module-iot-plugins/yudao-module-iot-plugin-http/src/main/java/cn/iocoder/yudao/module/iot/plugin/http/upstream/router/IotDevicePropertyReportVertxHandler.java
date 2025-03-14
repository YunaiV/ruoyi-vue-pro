package cn.iocoder.yudao.module.iot.plugin.http.upstream.router;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDevicePropertyReportReqDTO;
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
    private static final String VERSION = "1.0";
    private static final String METHOD = "thing.event.property.post";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDevicePropertyReportReqDTO reportReqDTO;
        String requestId = IdUtil.fastSimpleUUID();
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            requestId = ObjUtil.defaultIfBlank(body.getString("id"), requestId);

            // 按照标准JSON格式处理属性数据
            Map<String, Object> properties = new HashMap<>();

            // 优先使用params字段，符合标准
            Map<String, Object> params = body.getJsonObject("params") != null ? body.getJsonObject("params").getMap()
                    : null;

            if (params != null) {
                // 将标准格式的params转换为平台需要的properties格式
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();

                    // 如果是复杂结构（包含value和time）
                    if (valueObj instanceof Map) {
                        Map<String, Object> valueMap = (Map<String, Object>) valueObj;
                        if (valueMap.containsKey("value")) {
                            properties.put(key, valueMap.get("value"));
                        } else {
                            properties.put(key, valueObj);
                        }
                    } else {
                        properties.put(key, valueObj);
                    }
                }
            } else {
                // 兼容旧格式，直接使用properties字段
                properties = body.getJsonObject("properties") != null ? body.getJsonObject("properties").getMap()
                        : new HashMap<>();
            }

            reportReqDTO = ((IotDevicePropertyReportReqDTO) new IotDevicePropertyReportReqDTO().setRequestId(requestId)
                    .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                    .setProductKey(productKey).setDeviceName(deviceName))
                    .setProperties(properties);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            // 使用IotStandardResponse实体类返回错误
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    requestId, METHOD, BAD_REQUEST.getCode(), BAD_REQUEST.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
            return;
        }

        // TODO @芋艿：secret 校验。目前的想法：
        // 方案一：请求的时候，带上 secret 参数，然后进行校验，减少请求的频次。不过可能要看下 mqtt 能不能复用！
        // 方案二：本地有设备信息的缓存，异步刷新。这样可能 mqtt 的校验，和 http 校验都容易适配。

        try {
            // 2. 设备上线
            deviceUpstreamApi.updateDeviceState(
                    ((IotDeviceStateUpdateReqDTO) new IotDeviceStateUpdateReqDTO().setRequestId(IdUtil.fastSimpleUUID())
                            .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                            .setProductKey(reportReqDTO.getProductKey()).setDeviceName(reportReqDTO.getDeviceName()))
                            .setState(IotDeviceStateEnum.ONLINE.getState()));

            // 3.1 属性上报
            CommonResult<Boolean> result = deviceUpstreamApi.reportDeviceProperty(reportReqDTO);

            // 3.2 返回结果 - 使用IotStandardResponse实体类
            IotStandardResponse response;
            if (result.isSuccess()) {
                response = IotStandardResponse.success(reportReqDTO.getRequestId(), METHOD, result.getData());
            } else {
                response = IotStandardResponse.error(
                        reportReqDTO.getRequestId(), METHOD, result.getCode(), result.getMsg());
            }
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) 属性上报异常]", reportReqDTO, e);

            // 构建错误响应 - 使用IotStandardResponse实体类
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    reportReqDTO.getRequestId(), METHOD,
                    INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }
}
