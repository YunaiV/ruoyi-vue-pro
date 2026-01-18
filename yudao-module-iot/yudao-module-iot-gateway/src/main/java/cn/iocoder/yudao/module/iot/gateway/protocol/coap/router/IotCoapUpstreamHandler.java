package cn.iocoder.yudao.module.iot.gateway.protocol.coap.router;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/**
 * IoT 网关 CoAP 协议的【上行】处理器
 *
 * 处理设备通过 CoAP 协议发送的上行消息，包括：
 * 1. 属性上报：POST /topic/sys/{productKey}/{deviceName}/thing/property/post
 * 2. 事件上报：POST /topic/sys/{productKey}/{deviceName}/thing/event/{eventId}/post
 *
 * Token 通过自定义 CoAP Option 2088 携带
 *
 * @author 芋道源码
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IotCoapUpstreamHandler {

    /**
     * 自定义 CoAP Option 编号，用于携带 Token
     * CoAP Option 范围 2048-65535 属于实验/自定义范围
     */
    public static final int OPTION_TOKEN = 2088;

    private final IotDeviceTokenService deviceTokenService;
    private final IotDeviceMessageService deviceMessageService;

    /**
     * 处理 CoAP 请求
     *
     * @param exchange CoAP 交换对象
     * @param httpMethod HTTP 方法
     * @param protocol 协议对象
     */
    public void handle(CoapExchange exchange, String httpMethod, IotCoapUpstreamProtocol protocol) {
        try {
            // TODO @AI：这种路径的解析，不用了，简化下，类似 IotHttpUpstreamHandler 这种就很简洁；
            // 1. 解析 URI 路径：/topic/sys/{productKey}/{deviceName}/thing/...
            // 完整路径是 [topic, sys, productKey, deviceName, thing, ...]
            List<String> uriPath = exchange.getRequestOptions().getUriPath();
            if (uriPath.size() < 6) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST,
                        "URI 路径格式错误，期望：/topic/sys/{productKey}/{deviceName}/...");
                return;
            }

            // 验证路径格式：第一个应该是 "topic"，第二个应该是 "sys"
            if (!"topic".equals(uriPath.get(0)) || !"sys".equals(uriPath.get(1))) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "URI 路径格式错误，期望以 /topic/sys 开头");
                return;
            }

            // 解析 productKey 和 deviceName（索引 2 和 3）
            String productKey = uriPath.get(2);
            String deviceName = uriPath.get(3);

            // 2. 认证：优先从自定义 Option 获取 token，兼容 Query 参数
            String token = getTokenFromOption(exchange);
            if (StrUtil.isEmpty(token)) {
                // 兼容 Query 参数方式
                // TODO @AI：不用兼容 query，简化下；
                token = getQueryParam(exchange, "token");
            }
            if (StrUtil.isEmpty(token)) {
                respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "缺少 token（请使用 Option " + OPTION_TOKEN + " 或 Query 参数携带）");
                return;
            }

            // 验证 token
            // TODO @AI：这里参考 IotHttpAbstractHandler 简化点校验；
            IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.verifyToken(token);
            if (deviceInfo == null) {
                respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "token 无效或已过期");
                return;
            }
            // 验证设备信息匹配
            if (ObjUtil.notEqual(productKey, deviceInfo.getProductKey())
                    || ObjUtil.notEqual(deviceName, deviceInfo.getDeviceName())) {
                respondError(exchange, CoAP.ResponseCode.FORBIDDEN, "设备信息与 token 不匹配");
                return;
            }

            // 3. 解析 method：将 URI 路径转换为 method 格式
            // /topic/sys/pk/dn/thing/property/post -> thing.property.post
            // 路径是 [sys, pk, dn, thing, property, post]，从索引 3 开始
            String method = buildMethod(uriPath);

            // 4. 解析并处理消息体
            byte[] payload = exchange.getRequestPayload();
            if (payload == null || payload.length == 0) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体不能为空");
                return;
            }

            // 5. 解码消息
            IotDeviceMessage message;
            try {
                message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            } catch (Exception e) {
                log.error("[handle][消息解码失败]", e);
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "消息解码失败：" + e.getMessage());
                return;
            }

            // 校验 method
            // TODO @AI：不用校验 method；以 message 解析出来的为主；
            if (!method.equals(message.getMethod())) {
                log.warn("[handle][method 不匹配，URI: {}, 消息: {}]", method, message.getMethod());
            }

            // 6. 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, protocol.getServerId());

            // 7. 返回成功响应
            respondSuccess(exchange, message.getId());
        } catch (Exception e) {
            log.error("[handle][CoAP 请求处理异常]", e);
            respondError(exchange, CoAP.ResponseCode.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

    /**
     * 构建 method 字符串
     *
     * 将 URI 路径转换为 method 格式，例如：
     * [sys, pk, dn, thing, property, post] -> thing.property.post
     *
     * @param uriPath URI 路径列表
     * @return method 字符串
     */
    private String buildMethod(List<String> uriPath) {
        // 跳过 sys, productKey, deviceName，从第4个元素开始
        if (uriPath.size() > 3) {
            return String.join(StrPool.DOT, uriPath.subList(3, uriPath.size()));
        }
        return "";
    }

    // TODO @AI：抽到 coap 的 util 里；
    /**
     * 从自定义 CoAP Option 中获取 Token
     *
     * @param exchange CoAP 交换对象
     * @return Token 值，如果不存在则返回 null
     */
    private String getTokenFromOption(CoapExchange exchange) {
        // 尝试从自定义 Option 2088 获取 Token
        byte[] tokenBytes = exchange.getRequestOptions().getOthers().stream()
                .filter(option -> option.getNumber() == OPTION_TOKEN)
                .findFirst()
                .map(option -> option.getValue())
                .orElse(null);
        if (tokenBytes != null) {
            return new String(tokenBytes);
        }
        return null;
    }

    // TODO @AI：抽到 coap 的 util 里；
    /**
     * 从 URI Query 参数中获取指定 key 的值
     *
     * @param exchange CoAP 交换对象
     * @param key 参数名
     * @return 参数值，如果不存在则返回 null
     */
    private String getQueryParam(CoapExchange exchange, String key) {
        for (String query : exchange.getRequestOptions().getUriQuery()) {
            if (query.startsWith(key + "=")) {
                return query.substring((key + "=").length());
            }
        }
        return null;
    }

    // TODO @AI：抽到 coap 的 util 里；
    /**
     * 返回成功响应
     *
     * @param exchange CoAP 交换对象
     * @param messageId 消息 ID
     */
    private void respondSuccess(CoapExchange exchange, String messageId) {
        CommonResult<Object> result = CommonResult.success(MapUtil.of("messageId", messageId));
        String json = JsonUtils.toJsonString(result);
        exchange.respond(CoAP.ResponseCode.CONTENT, json, MediaTypeRegistry.APPLICATION_JSON);
    }

    // TODO @AI：抽到 coap 的 util 里；
    /**
     * 返回错误响应
     *
     * @param exchange CoAP 交换对象
     * @param code CoAP 响应码
     * @param message 错误消息
     */
    private void respondError(CoapExchange exchange, CoAP.ResponseCode code, String message) {
        // 将 CoAP 响应码映射到业务错误码
        int errorCode = mapCoapCodeToErrorCode(code);
        CommonResult<Object> result = CommonResult.error(errorCode, message);
        String json = JsonUtils.toJsonString(result);
        exchange.respond(code, json, MediaTypeRegistry.APPLICATION_JSON);
    }

    // TODO @AI：兼容 jdk8 的写法；
    /**
     * 将 CoAP 响应码映射到业务错误码
     *
     * @param code CoAP 响应码
     * @return 业务错误码
     */
    private int mapCoapCodeToErrorCode(CoAP.ResponseCode code) {
        return switch (code) {
            case BAD_REQUEST -> BAD_REQUEST.getCode();
            case UNAUTHORIZED -> UNAUTHORIZED.getCode();
            case FORBIDDEN -> FORBIDDEN.getCode();
            default -> INTERNAL_SERVER_ERROR.getCode();
        };
    }

}
