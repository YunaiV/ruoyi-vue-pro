package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.time.Duration;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_TIMEOUT;

/**
 * IoT 网关 HTTP 协议的【上行】处理器
 * <p>
 * 透传上报：POST /topic/sys/{productKey}/{deviceName}/thing/raw/post，Content-Type 为 application/octet-stream，
 * 请求体是原始字节；同一请求内应答 JSON 结果（与标准上报一致，data 为 messageId）
 *
 * @author 芋道源码
 */
@Slf4j
public class IotHttpUpstreamHandler extends IotHttpAbstractHandler {

    public static final String PATH = "/topic/sys/:productKey/:deviceName/*";

    private final String serverId;

    /**
     * 透传上报的应答计时时长：同一请求内应答，到期先返回超时（截止判断由设备消息 Service 负责）
     */
    private final Duration deadline;

    private final IotDeviceMessageService deviceMessageService;

    public IotHttpUpstreamHandler(IotHttpProtocol protocol) {
        this.serverId = protocol.getServerId();
        this.deadline = SpringUtil.getBean(IotGatewayProperties.class).getScript().getDeadline();
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    protected CommonResult<Object> handle0(RoutingContext context) {
        // 1.1 解析通用参数
        String productKey = context.pathParam("productKey");
        String deviceName = context.pathParam("deviceName");
        String method = context.pathParam("*").replaceAll(StrPool.SLASH, StrPool.DOT);
        // 1.2 透传上报：在 worker 上处理，响应异步写一次
        if (isRawPost(context, method)) {
            return handleRawPost(context, productKey, deviceName);
        }
        // 1.3 根据 Content-Type 反序列化消息
        IotDeviceMessage message = deserializeRequest(context, IotDeviceMessage.class);
        Assert.notNull(message, "请求参数不能为空");
        Assert.equals(method, message.getMethod(), "method 不匹配");

        // 2. 发送消息：获取设备、发送总线会阻塞，交给 worker 执行，完成后在 event loop 上响应（与原来一样等发送完成）
        context.vertx().executeBlocking(() -> {
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);
            return message.getId();
        }, false).onComplete(ar -> writeAsyncResponse(context, buildResult(context, ar)));
        return ASYNC_RESPONSE;
    }

    /**
     * 是否透传上报：路径为 thing/raw/post，并且 Content-Type 为 application/octet-stream
     * <p>
     * 其它 Content-Type 走 JSON 分支，由标准路径拒绝透传方法
     */
    private boolean isRawPost(RoutingContext context, String method) {
        if (!StrUtil.equals(IotDeviceMessageMethodEnum.RAW_POST.getMethod(), method)) {
            return false;
        }
        String contentType = context.request().getHeader(HttpHeaders.CONTENT_TYPE);
        return StrUtil.equalsIgnoreCase(StrUtil.trim(StrUtil.subBefore(contentType, ';', false)),
                MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    /**
     * 透传上报：身份来自前置认证（token 与路径一致），与 JSON 分支相同
     * <p>
     * 解析与发送会阻塞，交给 worker 执行；截止时间到期先返回超时（101），之后完成的结果不再响应。
     * 计时器与 worker 的结果都回到请求所在的 event loop，以「响应是否已结束」保证只响应一次
     */
    private CommonResult<Object> handleRawPost(RoutingContext context, String productKey, String deviceName) {
        byte[] body = context.body().buffer() != null ? context.body().buffer().getBytes() : null;
        // 截止时间从收到（交给 worker 之前）开始计算
        long receivedNanos = System.nanoTime();
        Vertx vertx = context.vertx();
        long timerId = vertx.setTimer(Math.max(1L, deadline.toMillis()),
                id -> writeAsyncResponse(context, CommonResult.error(SCRIPT_TIMEOUT)));
        vertx.executeBlocking(() -> deviceMessageService.sendRawMessage(body, productKey, deviceName, serverId,
                IotProtocolTypeEnum.HTTP, receivedNanos), false).onComplete(ar -> {
            vertx.cancelTimer(timerId);
            writeAsyncResponse(context, buildResult(context, ar));
        });
        return ASYNC_RESPONSE;
    }

    /**
     * worker 的处理结果转为响应：与基类的异常处理一致
     */
    private static CommonResult<Object> buildResult(RoutingContext context, AsyncResult<String> ar) {
        if (ar.succeeded()) {
            return CommonResult.success(MapUtil.of("messageId", ar.result()));
        }
        if (ar.cause() instanceof ServiceException) {
            ServiceException e = (ServiceException) ar.cause();
            return CommonResult.error(e.getCode(), e.getMessage());
        }
        if (ar.cause() instanceof IllegalArgumentException) {
            return CommonResult.error(BAD_REQUEST.getCode(), ar.cause().getMessage());
        }
        log.error("[buildResult][path({}) 处理异常]", context.request().path(), ar.cause());
        return CommonResult.error(INTERNAL_SERVER_ERROR);
    }

    /**
     * 写 worker 处理结果的响应（event loop 上执行）：响应已结束（透传已超时返回）或连接已关闭时丢弃
     */
    private static void writeAsyncResponse(RoutingContext context, CommonResult<Object> result) {
        if (context.response().ended() || context.response().closed()) {
            log.warn("[writeAsyncResponse][path({}) 响应已结束或连接已关闭，丢弃结果({})]",
                    context.request().path(), result.getCode());
            return;
        }
        try {
            writeResponse(context, result);
        } catch (Exception e) {
            log.error("[writeAsyncResponse][path({}) 写响应异常]", context.request().path(), e);
        }
    }

}
