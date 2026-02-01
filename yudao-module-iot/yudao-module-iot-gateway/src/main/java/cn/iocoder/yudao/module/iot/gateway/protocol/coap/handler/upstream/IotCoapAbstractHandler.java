package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * IoT 网关 CoAP 协议的处理器抽象基类：提供通用的前置处理（认证）、请求解析、响应处理、全局的异常捕获等
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class IotCoapAbstractHandler {

    /**
     * 自定义 CoAP Option 编号，用于携带 Token
     * <p>
     * CoAP Option 范围 2048-65535 属于实验/自定义范围
     */
    public static final int OPTION_TOKEN = 2088;

    private final IotDeviceTokenService deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);

    /**
     * 处理 CoAP 请求（模板方法）
     *
     * @param exchange CoAP 交换对象
     */
    public final void handle(CoapExchange exchange) {
        try {
            // 1. 前置处理
            beforeHandle(exchange);

            // 2. 执行业务逻辑
            CommonResult<Object> result = handle0(exchange);
            writeResponse(exchange, result);
        } catch (ServiceException e) {
            // 业务异常，返回对应的错误码和消息
            writeResponse(exchange, CommonResult.error(e.getCode(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 参数校验异常（hutool Assert 抛出），返回 BAD_REQUEST
            writeResponse(exchange, CommonResult.error(BAD_REQUEST.getCode(), e.getMessage()));
        } catch (Exception e) {
            // 其他未知异常，返回 INTERNAL_SERVER_ERROR
            log.error("[handle][CoAP 请求处理异常]", e);
            writeResponse(exchange, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 处理 CoAP 请求（子类实现）
     *
     * @param exchange CoAP 交换对象
     * @return 处理结果
     */
    protected abstract CommonResult<Object> handle0(CoapExchange exchange);

    /**
     * 前置处理：认证等
     *
     * @param exchange CoAP 交换对象
     */
    private void beforeHandle(CoapExchange exchange) {
        // 1.1 如果不需要认证，则不走前置处理
        if (!requiresAuthentication()) {
            return;
        }
        // 1.2 从自定义 Option 获取 token
        String token = getTokenFromOption(exchange);
        if (StrUtil.isEmpty(token)) {
            throw exception(UNAUTHORIZED);
        }
        // 1.3 校验 token
        IotDeviceIdentity deviceInfo = deviceTokenService.verifyToken(token);
        if (deviceInfo == null) {
            throw exception(UNAUTHORIZED);
        }

        // 2.1 解析 productKey 和 deviceName
        List<String> uriPath = exchange.getRequestOptions().getUriPath();
        String productKey = getProductKey(uriPath);
        String deviceName = getDeviceName(uriPath);
        if (StrUtil.isEmpty(productKey) || StrUtil.isEmpty(deviceName)) {
            throw exception(BAD_REQUEST);
        }
        // 2.2 校验设备信息是否匹配
        if (ObjUtil.notEqual(productKey, deviceInfo.getProductKey())
                || ObjUtil.notEqual(deviceName, deviceInfo.getDeviceName())) {
            throw exception(FORBIDDEN);
        }
    }

    // ========== Token 相关方法 ==========

    /**
     * 是否需要认证（子类可覆盖）
     * <p>
     * 默认不需要认证
     *
     * @return 是否需要认证
     */
    protected boolean requiresAuthentication() {
        return false;
    }

    /**
     * 从 URI 路径中获取 productKey（子类实现）
     * <p>
     * 默认抛出异常，需要认证的子类必须实现此方法
     *
     * @param uriPath URI 路径
     * @return productKey
     */
    protected String getProductKey(List<String> uriPath) {
        throw new UnsupportedOperationException("子类需要实现 getProductKey 方法");
    }

    /**
     * 从 URI 路径中获取 deviceName（子类实现）
     * <p>
     * 默认抛出异常，需要认证的子类必须实现此方法
     *
     * @param uriPath URI 路径
     * @return deviceName
     */
    protected String getDeviceName(List<String> uriPath) {
        throw new UnsupportedOperationException("子类需要实现 getDeviceName 方法");
    }

    /**
     * 从自定义 CoAP Option 中获取 Token
     *
     * @param exchange CoAP 交换对象
     * @return Token 值，如果不存在则返回 null
     */
    protected String getTokenFromOption(CoapExchange exchange) {
        Option option = CollUtil.findOne(exchange.getRequestOptions().getOthers(),
                o -> o.getNumber() == OPTION_TOKEN);
        return option != null ? new String(option.getValue()) : null;
    }

    // ========== 序列化相关方法 ==========

    /**
     * 解析请求体为指定类型
     *
     * @param exchange CoAP 交换对象
     * @param clazz    目标类型
     * @param <T>      目标类型泛型
     * @return 解析后的对象，解析失败返回 null
     */
    protected <T> T deserializeRequest(CoapExchange exchange, Class<T> clazz) {
        byte[] payload = exchange.getRequestPayload();
        if (ArrayUtil.isEmpty(payload)) {
            return null;
        }
        return JsonUtils.parseObject(payload, clazz);
    }

    private static String serializeResponse(Object data) {
        return JsonUtils.toJsonString(data);
    }

    protected void writeResponse(CoapExchange exchange, CommonResult<?> data) {
        String json = serializeResponse(data);
        exchange.respond(CoAP.ResponseCode.CONTENT, json, MediaTypeRegistry.APPLICATION_JSON);
    }

}
