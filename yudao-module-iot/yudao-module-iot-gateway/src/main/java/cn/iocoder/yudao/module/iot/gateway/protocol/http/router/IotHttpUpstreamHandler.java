package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.gateway.enums.IotDeviceTopicEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 网关 HTTP 协议的处理器
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotHttpUpstreamHandler implements Handler<RoutingContext> {

    // TODO @haohao：你说，咱要不要把 "/sys/:productKey/:deviceName"
    //            + IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getTopic()，也抽到 IotDeviceTopicEnum 的 build 这种？尽量都收敛掉？
    /**
     * 属性上报路径
     */
    public static final String PROPERTY_PATH = "/sys/:productKey/:deviceName"
            + IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getTopic();

    /**
     * 事件上报路径
     */
    public static final String EVENT_PATH = "/sys/:productKey/:deviceName"
            + IotDeviceTopicEnum.EVENT_POST_TOPIC_PREFIX.getTopic() + ":identifier"
            + IotDeviceTopicEnum.EVENT_POST_TOPIC_SUFFIX.getTopic();

    /**
     * 事件上报方法前缀
     */
    private static final String EVENT_METHOD_PREFIX = "thing.event.";

    /**
     * 事件上报方法后缀
     */
    private static final String EVENT_METHOD_SUFFIX = ".post";

    private final IotHttpUpstreamProtocol protocol;
//    /**
//     * 设备上行 API
//     */
//    private final IotDeviceUpstreamApi deviceUpstreamApi;
    /**
     * 设备消息生产者
     */
    private final IotDeviceMessageProducer deviceMessageProducer;

    @Override
    public void handle(RoutingContext routingContext) {
        String path = routingContext.request().path();

        try {
            // 1. 解析通用参数
            Map<String, String> params = parseCommonParams(routingContext);
            String productKey = params.get("productKey");
            String deviceName = params.get("deviceName");
            JsonObject body = routingContext.body().asJsonObject();

            // 2. 根据路径模式处理不同类型的请求
            if (isPropertyPostPath(path)) {
                // 处理属性上报
                handlePropertyPost(routingContext, productKey, deviceName, body);
                return;
            }

            if (isEventPostPath(path)) {
                // 处理事件上报
                String identifier = routingContext.pathParam("identifier");
                handleEventPost(routingContext, productKey, deviceName, identifier, body);
                return;
            }

            // 不支持的请求路径
            sendErrorResponse(routingContext,  "unknown", BAD_REQUEST.getCode(), "不支持的请求路径");
        } catch (Exception e) {
            log.error("[handle][处理上行请求异常] path={}", path, e);
            String method = determineMethodFromPath(path, routingContext);
            sendErrorResponse(routingContext, method, INTERNAL_SERVER_ERROR.getCode(),
                    INTERNAL_SERVER_ERROR.getMsg());
        }
    }

    /**
     * 解析通用参数
     *
     * @param routingContext   路由上下文
     * @return 参数映射
     */
    private Map<String, String> parseCommonParams(RoutingContext routingContext) {
        Map<String, String> params = MapUtil.newHashMap();
        params.put("productKey", routingContext.pathParam("productKey"));
        params.put("deviceName", routingContext.pathParam("deviceName"));
        return params;
    }

    /**
     * 判断是否是属性上报路径
     *
     * @param path 路径
     * @return 是否是属性上报路径
     */
    private boolean isPropertyPostPath(String path) {
        return StrUtil.endWith(path, IotDeviceTopicEnum.PROPERTY_POST_TOPIC.getTopic());
    }

    /**
     * 判断是否是事件上报路径
     *
     * @param path 路径
     * @return 是否是事件上报路径
     */
    private boolean isEventPostPath(String path) {
        return StrUtil.contains(path, IotDeviceTopicEnum.EVENT_POST_TOPIC_PREFIX.getTopic())
                && StrUtil.endWith(path, IotDeviceTopicEnum.EVENT_POST_TOPIC_SUFFIX.getTopic());
    }

    /**
     * 处理属性上报请求
     *
     * @param routingContext 路由上下文
     * @param productKey     产品 Key
     * @param deviceName     设备名称
     * @param body           请求体
     */
    private void handlePropertyPost(RoutingContext routingContext, String productKey, String deviceName,
                                    JsonObject body) {
        // 1.1 构建设备消息
        IotDeviceMessage message = IotDeviceMessage.of(productKey, deviceName, protocol.getServerId())
                .ofPropertyReport(parsePropertiesFromBody(body));
        // 1.2 发送消息
        deviceMessageProducer.sendDeviceMessage(message);

        // 2. 返回响应
        sendResponse(routingContext, null);
    }

    /**
     * 处理事件上报请求
     *
     * @param routingContext 路由上下文
     * @param productKey     产品 Key
     * @param deviceName     设备名称
     * @param identifier     事件标识符
     * @param body           请求体
     */
    private void handleEventPost(RoutingContext routingContext, String productKey, String deviceName,
                                 String identifier, JsonObject body) {
//        // 处理事件上报
//        IotDeviceEventReportReqDTO reportReqDTO = parseEventReportRequest(productKey, deviceName, identifier,
//                requestId, body);
//
//        // 事件上报
//        CommonResult<Boolean> result = deviceUpstreamApi.reportDeviceEvent(reportReqDTO);
//        String method = EVENT_METHOD_PREFIX + identifier + EVENT_METHOD_SUFFIX;
//
//        // 返回响应
//        sendResponse(routingContext, requestId, method, result);
    }

    /**
     * 发送响应
     *
     * @param routingContext 路由上下文
     * @param result         结果
     */
    private void sendResponse(RoutingContext routingContext,
                              CommonResult<Boolean> result) {
//        // TODO @芋艿：后续再优化
//        IotStandardResponse response;
//        if (result == null ) {
//            response = IotStandardResponse.success(requestId, method, null);
//        } else if (result.isSuccess()) {
//            response = IotStandardResponse.success(requestId, method, result.getData());
//        } else {
//            response = IotStandardResponse.error(requestId, method, result.getCode(), result.getMsg());
//        }
//        IotNetComponentCommonUtils.writeJsonResponse(routingContext, response);
    }

    /**
     * 发送错误响应
     *
     * @param routingContext 路由上下文
     * @param method         方法名
     * @param code           错误代码
     * @param message        错误消息
     */
    private void sendErrorResponse(RoutingContext routingContext, String method, Integer code,
                                   String message) {
//        IotStandardResponse errorResponse = IotStandardResponse.error(requestId, method, code, message);
//        IotNetComponentCommonUtils.writeJsonResponse(routingContext, errorResponse);
    }

    /**
     * 从路径确定方法名
     *
     * @param path           路径
     * @param routingContext 路由上下文
     * @return 方法名
     */
    private String determineMethodFromPath(String path, RoutingContext routingContext) {
        if (StrUtil.contains(path, "/property/")) {
            return null;
        }

        return EVENT_METHOD_PREFIX
                + (routingContext.pathParams().containsKey("identifier")
                        ? routingContext.pathParam("identifier")
                        : "unknown")
                +
                EVENT_METHOD_SUFFIX;
    }

    // TODO @芋艿：这块在看看
    /**
     * 从请求体解析属性
     *
     * @param body 请求体
     * @return 属性映射
     */
    private Map<String, Object> parsePropertiesFromBody(JsonObject body) {
        Map<String, Object> properties = MapUtil.newHashMap();
        JsonObject params = body.getJsonObject("params");
        if (CollUtil.isEmpty(params)) {
            return properties;
        }

        // 将标准格式的 params 转换为平台需要的 properties 格式
        for (String key : params.fieldNames()) {
            Object valueObj = params.getValue(key);
            // 如果是复杂结构（包含 value 和 time）
            if (valueObj instanceof JsonObject) {
                JsonObject valueJson = (JsonObject) valueObj;
                properties.put(key, valueJson.containsKey("value") ? valueJson.getValue("value") : valueObj);
            } else {
                properties.put(key, valueObj);
            }
        }
        return properties;
    }

//    /**
//     * 解析事件上报请求
//     *
//     * @param productKey 产品 Key
//     * @param deviceName 设备名称
//     * @param identifier 事件标识符
//     * @param requestId  请求 ID
//     * @param body       请求体
//     * @return 事件上报请求 DTO
//     */
//    private IotDeviceEventReportReqDTO parseEventReportRequest(String productKey, String deviceName, String identifier,
//                                                               String requestId, JsonObject body) {
//        // 解析参数
//        Map<String, Object> params = parseParamsFromBody(body);
//
//        // 构建事件上报请求 DTO
//        return ((IotDeviceEventReportReqDTO) new IotDeviceEventReportReqDTO()
//                .setRequestId(requestId)
//                .setProcessId(IotNetComponentCommonUtils.getProcessId())
//                .setReportTime(LocalDateTime.now())
//                .setProductKey(productKey)
//                .setDeviceName(deviceName)).setIdentifier(identifier).setParams(params);
//    }

    /**
     * 从请求体解析参数
     *
     * @param body 请求体
     * @return 参数映射
     */
    private Map<String, Object> parseParamsFromBody(JsonObject body) {
        Map<String, Object> params = MapUtil.newHashMap();
        JsonObject paramsJson = body.getJsonObject("params");
        if (CollUtil.isEmpty(paramsJson)) {
            return params;
        }

        for (String key : paramsJson.fieldNames()) {
            params.put(key, paramsJson.getValue(key));
        }
        return params;
    }
}