package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * IoT 网关 HTTP 协议的处理器抽象基类：提供通用的前置处理（认证）、全局的异常捕获等
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
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
            writeResponse(context, CommonResult.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("[handle][path({}) 处理异常]", context.request().path(), e);
            writeResponse(context, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }

    protected abstract CommonResult<Object> handle0(RoutingContext context);

    private void beforeHandle(RoutingContext context) {
        // 如果不需要认证，则不走前置处理
        String path = context.request().path();
        if (ObjUtil.equal(path, IotHttpAuthHandler.PATH)) {
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
        IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.verifyToken(token);
        Assert.notNull(deviceInfo, "设备信息不能为空");
        // 校验设备信息是否匹配
        if (ObjUtil.notEqual(productKey, deviceInfo.getProductKey())
                || ObjUtil.notEqual(deviceName, deviceInfo.getDeviceName())) {
            throw exception(FORBIDDEN);
        }
    }

    @SuppressWarnings("deprecation")
    public static void writeResponse(RoutingContext context, Object data) {
        context.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .end(JsonUtils.toJsonString(data));
    }

}
