package cn.iocoder.yudao.module.iot.gateway.protocol.coap.util;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/**
 * IoT CoAP 协议工具类
 *
 * @author 芋道源码
 */
public class IotCoapUtils {

    /**
     * 自定义 CoAP Option 编号，用于携带 Token
     * <p>
     * CoAP Option 范围 2048-65535 属于实验/自定义范围
     */
    public static final int OPTION_TOKEN = 2088;

    /**
     * 返回成功响应
     *
     * @param exchange CoAP 交换对象
     * @param data     响应数据
     */
    public static void respondSuccess(CoapExchange exchange, Object data) {
        CommonResult<Object> result = CommonResult.success(data);
        String json = JsonUtils.toJsonString(result);
        exchange.respond(CoAP.ResponseCode.CONTENT, json, MediaTypeRegistry.APPLICATION_JSON);
    }

    /**
     * 返回错误响应
     *
     * @param exchange CoAP 交换对象
     * @param code     CoAP 响应码
     * @param message  错误消息
     */
    public static void respondError(CoapExchange exchange, CoAP.ResponseCode code, String message) {
        int errorCode = mapCoapCodeToErrorCode(code);
        CommonResult<Object> result = CommonResult.error(errorCode, message);
        String json = JsonUtils.toJsonString(result);
        exchange.respond(code, json, MediaTypeRegistry.APPLICATION_JSON);
    }

    /**
     * 从自定义 CoAP Option 中获取 Token
     *
     * @param exchange     CoAP 交换对象
     * @param optionNumber Option 编号
     * @return Token 值，如果不存在则返回 null
     */
    public static String getTokenFromOption(CoapExchange exchange, int optionNumber) {
        Option option = CollUtil.findOne(exchange.getRequestOptions().getOthers(),
                o -> o.getNumber() == optionNumber);
        return option != null ? new String(option.getValue()) : null;
    }

    /**
     * 将 CoAP 响应码映射到业务错误码
     *
     * @param code CoAP 响应码
     * @return 业务错误码
     */
    public static int mapCoapCodeToErrorCode(CoAP.ResponseCode code) {
        if (code == CoAP.ResponseCode.BAD_REQUEST) {
            return BAD_REQUEST.getCode();
        } else if (code == CoAP.ResponseCode.UNAUTHORIZED) {
            return UNAUTHORIZED.getCode();
        } else if (code == CoAP.ResponseCode.FORBIDDEN) {
            return FORBIDDEN.getCode();
        } else {
            return INTERNAL_SERVER_ERROR.getCode();
        }
    }

}
