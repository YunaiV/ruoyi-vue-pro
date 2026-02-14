package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * IoT 网关 HTTP 协议的处理器抽象基类：提供通用的前置处理（认证）、全局的异常捕获等
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class IotHttpAbstractHandler implements Handler<RoutingContext> {

    private final IotDeviceTokenService deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);

    @Override
    public final void handle(RoutingContext context) {
        try {
            // 1. 前置处理
            beforeHandle(context);

            // 2. 执行逻辑
            CommonResult<Object> result = handle0(context);
            writeResponse(context, result);
        } catch (ServiceException e) {
            // 已知异常，返回对应的错误码和错误信息
            writeResponse(context, CommonResult.error(e.getCode(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 参数校验异常，返回 400 错误
            writeResponse(context, CommonResult.error(BAD_REQUEST.getCode(), e.getMessage()));
        } catch (Exception e) {
            // 其他未知异常，返回 500 错误
            log.error("[handle][path({}) 处理异常]", context.request().path(), e);
            writeResponse(context, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 处理 HTTP 请求（子类实现）
     *
     * @param context RoutingContext 对象
     * @return 处理结果
     */
    protected abstract CommonResult<Object> handle0(RoutingContext context);

    /**
     * 前置处理：认证等
     *
     * @param context RoutingContext 对象
     */
    private void beforeHandle(RoutingContext context) {
        // 如果不需要认证，则不走前置处理
        String path = context.request().path();
        if (ObjectUtils.equalsAny(path, IotHttpAuthHandler.PATH, IotHttpRegisterHandler.PATH)) {
            return;
        }

        // 解析参数
        String token = context.request().getHeader(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isEmpty(token)) {
            throw invalidParamException("token 不能为空");
        }
        String productKey = context.pathParam("productKey");
        if (StrUtil.isEmpty(productKey)) {
            throw invalidParamException("productKey 不能为空");
        }
        String deviceName = context.pathParam("deviceName");
        if (StrUtil.isEmpty(deviceName)) {
            throw invalidParamException("deviceName 不能为空");
        }

        // 校验 token
        IotDeviceIdentity deviceInfo = deviceTokenService.verifyToken(token);
        Assert.notNull(deviceInfo, "设备信息不能为空");
        // 校验设备信息是否匹配
        if (ObjUtil.notEqual(productKey, deviceInfo.getProductKey())
                || ObjUtil.notEqual(deviceName, deviceInfo.getDeviceName())) {
            throw exception(FORBIDDEN);
        }
    }

    // ========== 序列化相关方法 ==========

    protected static  <T> T deserializeRequest(RoutingContext context, Class<T> clazz) {
        byte[] body = context.body().buffer() != null ? context.body().buffer().getBytes() : null;
        if (ArrayUtil.isEmpty(body)) {
            throw invalidParamException("请求体不能为空");
        }
        return JsonUtils.parseObject(body, clazz);
    }

    private static String serializeResponse(Object data) {
        return JsonUtils.toJsonString(data);
    }

    @SuppressWarnings("deprecation")
    public static void writeResponse(RoutingContext context, CommonResult<?> data) {
        context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .end(serializeResponse(data));
    }

}
