package cn.iocoder.yudao.module.iot.plugin.http.upstream.router;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceEventReportReqDTO;
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

/**
 * IoT 设备上行统一处理的 Vert.x Handler
 * <p>
 * 统一处理设备属性上报和事件上报的请求
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceUpstreamVertxHandler implements Handler<RoutingContext> {

    // TODO @haohao：要不要类似 IotDeviceConfigSetVertxHandler 写的，把这些 PATH、METHOD 之类的抽走
    /**
     * 属性上报路径
     */
    public static final String PROPERTY_PATH = "/sys/:productKey/:deviceName/thing/event/property/post";
    /**
     * 事件上报路径
     */
    public static final String EVENT_PATH = "/sys/:productKey/:deviceName/thing/event/:identifier/post";

    private static final String PROPERTY_METHOD = "thing.event.property.post";
    private static final String EVENT_METHOD_PREFIX = "thing.event.";
    private static final String EVENT_METHOD_SUFFIX = ".post";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    // TODO @haohao：要不要分成多个 Handler？每个只解决一个问题哈。
    @Override
    public void handle(RoutingContext routingContext) {
        String path = routingContext.request().path();
        String requestId = IdUtil.fastSimpleUUID();

        try {
            // 1. 解析通用参数
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            requestId = ObjUtil.defaultIfBlank(body.getString("id"), requestId);

            // 2. 根据路径模式处理不同类型的请求
            CommonResult<Boolean> result;
            String method;
            if (path.matches(".*/thing/event/property/post")) {
                // 处理属性上报
                IotDevicePropertyReportReqDTO reportReqDTO = parsePropertyReportRequest(productKey, deviceName, requestId, body);

                // 设备上线
                updateDeviceState(reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());

                // 属性上报
                result = deviceUpstreamApi.reportDeviceProperty(reportReqDTO);
                method = PROPERTY_METHOD;
            } else if (path.matches(".*/thing/event/.+/post")) {
                // 处理事件上报
                String identifier = routingContext.pathParam("identifier");
                IotDeviceEventReportReqDTO reportReqDTO = parseEventReportRequest(productKey, deviceName, identifier, requestId, body);

                // 设备上线
                updateDeviceState(reportReqDTO.getProductKey(), reportReqDTO.getDeviceName());

                // 事件上报
                result = deviceUpstreamApi.reportDeviceEvent(reportReqDTO);
                method = EVENT_METHOD_PREFIX + identifier + EVENT_METHOD_SUFFIX;
            } else {
                // 不支持的请求路径
                IotStandardResponse errorResponse = IotStandardResponse.error(requestId, "unknown", BAD_REQUEST.getCode(), "不支持的请求路径");
                IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
                return;
            }

            // 3. 返回标准响应
            IotStandardResponse response;
            if (result.isSuccess()) {
                response = IotStandardResponse.success(requestId, method, result.getData());
            } else {
                response = IotStandardResponse.error(requestId, method, result.getCode(), result.getMsg());
            }
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][处理上行请求异常] path={}", path, e);
            String method = path.contains("/property/") ? PROPERTY_METHOD
                    : EVENT_METHOD_PREFIX + (routingContext.pathParams().containsKey("identifier")
                    ? routingContext.pathParam("identifier")
                    : "unknown") + EVENT_METHOD_SUFFIX;
            IotStandardResponse errorResponse = IotStandardResponse.error(requestId, method, INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }

    /**
     * 更新设备状态
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     */
    private void updateDeviceState(String productKey, String deviceName) {
        deviceUpstreamApi.updateDeviceState(((IotDeviceStateUpdateReqDTO) new IotDeviceStateUpdateReqDTO()
                .setRequestId(IdUtil.fastSimpleUUID()).setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                .setProductKey(productKey).setDeviceName(deviceName)).setState(IotDeviceStateEnum.ONLINE.getState()));
    }

    /**
     * 解析属性上报请求
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param requestId  请求 ID
     * @param body       请求体
     * @return 属性上报请求 DTO
     */
    @SuppressWarnings("unchecked")
    private IotDevicePropertyReportReqDTO parsePropertyReportRequest(String productKey, String deviceName, String requestId, JsonObject body) {
        // 按照标准 JSON 格式处理属性数据
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> params = body.getJsonObject("params") != null ? body.getJsonObject("params").getMap() : null;
        if (params != null) {
            // 将标准格式的 params 转换为平台需要的 properties 格式
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object valueObj = entry.getValue();
                // 如果是复杂结构（包含 value 和 time）
                if (valueObj instanceof Map) {
                    Map<String, Object> valueMap = (Map<String, Object>) valueObj;
                    properties.put(key, valueMap.getOrDefault("value", valueObj));
                } else {
                    properties.put(key, valueObj);
                }
            }
        }

        // 构建属性上报请求 DTO
        return ((IotDevicePropertyReportReqDTO) new IotDevicePropertyReportReqDTO().setRequestId(requestId)
                .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                .setProductKey(productKey).setDeviceName(deviceName)).setProperties(properties);
    }

    /**
     * 解析事件上报请求
     *
     * @param productKey 产品K ey
     * @param deviceName 设备名称
     * @param identifier 事件标识符
     * @param requestId  请求 ID
     * @param body       请求体
     * @return 事件上报请求 DTO
     */
    private IotDeviceEventReportReqDTO parseEventReportRequest(String productKey, String deviceName, String identifier, String requestId, JsonObject body) {
        // 按照标准 JSON 格式处理事件参数
        Map<String, Object> params;
        if (body.containsKey("params")) {
            params = body.getJsonObject("params").getMap();
        } else {
            // 兼容旧格式
            params = new HashMap<>();
        }

        // 构建事件上报请求 DTO
        return ((IotDeviceEventReportReqDTO) new IotDeviceEventReportReqDTO().setRequestId(requestId)
                .setProcessId(IotPluginCommonUtils.getProcessId()).setReportTime(LocalDateTime.now())
                .setProductKey(productKey).setDeviceName(deviceName)).setIdentifier(identifier).setParams(params);
    }
}