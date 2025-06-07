package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
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

/**
 * IoT 网关 HTTP 协议的【上行】处理器
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

    private final IotHttpUpstreamProtocol protocol;

    private final IotDeviceMessageProducer deviceMessageProducer;

    public IotHttpUpstreamHandler(IotHttpUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceMessageProducer = SpringUtil.getBean(IotDeviceMessageProducer.class);
    }

    @Override
    public void handle(RoutingContext context) {
        String path = context.request().path();
        // 1. 解析通用参数
        String productKey = context.pathParam("productKey");
        String deviceName = context.pathParam("deviceName");
        JsonObject body = context.body().asJsonObject();

        // 2. 根据路径模式处理不同类型的请求
        if (isPropertyPostPath(path)) {
            // 处理属性上报
            handlePropertyPost(context, productKey, deviceName, body);
        } else if (isEventPostPath(path)) {
            // 处理事件上报
            String identifier = context.pathParam("identifier");
            handleEventPost(context, productKey, deviceName, identifier, body);
        }
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

//        // 2. 返回响应
//        sendResponse(routingContext, null);
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