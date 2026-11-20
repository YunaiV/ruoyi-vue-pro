package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.TOO_MANY_REQUESTS;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_TIMEOUT;

/**
 * IoT 网关 CoAP 协议的【上行】处理器
 *
 * 处理设备通过 CoAP 协议发送的上行消息，包括：
 * 1. 属性上报：POST /topic/sys/{productKey}/{deviceName}/thing/property/post
 * 2. 事件上报：POST /topic/sys/{productKey}/{deviceName}/thing/event/post
 * 3. 透传上报：POST /topic/sys/{productKey}/{deviceName}/thing/raw/post，content-format 为 42（octet-stream）
 *
 * Token 通过自定义 CoAP Option 2088 携带
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapUpstreamHandler extends IotCoapAbstractHandler {

    /**
     * worker 的线程数：阻塞在 IO 上，取 CPU 核数的 2 倍
     */
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * 上报最多排队的请求数（标准、透传共用）：超过时立即响应 429
     */
    private static final int WORKER_MAX_PENDING = 1024;

    private final String serverId;

    /**
     * 透传上报的应答计时时长：同一请求内应答（截止判断由设备消息 Service 负责）
     */
    private final Duration deadline;

    /**
     * 透传上报的截止计时器：到期先返回超时，与处理结果谁先到谁响应
     */
    private final ScheduledExecutorService deadlineTimer = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("iot-coap-raw-deadline-", true));
    /**
     * 上报的 worker（有界，标准、透传共用）：解析、发送消息总线、写入离线暂存会阻塞，
     * 不能占着 Californium 处理请求的线程（Exchange 的响应排在它之后）
     */
    private final ThreadPoolExecutor workerExecutor = new ThreadPoolExecutor(WORKER_THREADS, WORKER_THREADS,
            60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(WORKER_MAX_PENDING),
            new NamedThreadFactory("iot-coap-worker-", true));

    private final IotDeviceMessageService deviceMessageService;

    public IotCoapUpstreamHandler(String serverId) {
        this.serverId = serverId;
        this.deadline = SpringUtil.getBean(IotGatewayProperties.class).getScript().getDeadline();
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.workerExecutor.allowCoreThreadTimeOut(true);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected CommonResult<Object> handle0(CoapExchange exchange) {
        // 1.1 解析通用参数
        List<String> uriPath = exchange.getRequestOptions().getUriPath();
        String productKey = getProductKey(uriPath);
        String deviceName = getDeviceName(uriPath);
        String method = String.join(StrPool.DOT, uriPath.subList(4, uriPath.size()));
        // 1.2 透传上报
        if (isRawPost(exchange, method)) {
            return handleRawPost(exchange, productKey, deviceName);
        }
        // 1.3 解析消息：不合法时由基类同步响应
        IotDeviceMessage message = deserializeRequest(exchange, IotDeviceMessage.class);
        Assert.notNull(message, "请求参数不能为空");
        Assert.equals(method, message.getMethod(), "method 不匹配");

        // 2. 发送消息：会阻塞（发送消息总线、写入离线暂存），交给 worker 处理，以分离响应返回
        return handleStandardPost(exchange, message, productKey, deviceName);
    }

    /**
     * 标准上报：发送交给 worker，不阻塞 Californium 处理请求的线程；结果（含失败）与原来的同步响应一致，没有截止计时
     */
    private CommonResult<Object> handleStandardPost(CoapExchange exchange, IotDeviceMessage message,
                                                    String productKey, String deviceName) {
        // 1. CON 请求：先回空 ACK，避免处理期间设备重传；之后的响应作为分离响应发送
        if (exchange.advanced().getRequest().isConfirmable()) {
            exchange.accept();
        }

        // 2. 交给 worker：队列已满时立即响应
        AtomicBoolean responded = new AtomicBoolean();
        try {
            workerExecutor.execute(() -> writeAsyncResponse(exchange, responded,
                    invoke(productKey, deviceName, () -> {
                        deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId,
                                IotProtocolTypeEnum.COAP);
                        return message.getId();
                    })));
        } catch (RejectedExecutionException e) {
            log.warn("[handleStandardPost][productKey({}) deviceName({}) 上报积压，拒绝处理]", productKey, deviceName);
            writeAsyncResponse(exchange, responded, CommonResult.error(TOO_MANY_REQUESTS));
        }
        return ASYNC_RESPONSE;
    }

    /**
     * 是否透传上报：路径为 thing/raw/post，并且 content-format 为 42（application/octet-stream）
     * <p>
     * 其它 content-format 走 JSON 分支，由标准路径拒绝透传方法
     */
    private boolean isRawPost(CoapExchange exchange, String method) {
        return StrUtil.equals(IotDeviceMessageMethodEnum.RAW_POST.getMethod(), method)
                && exchange.getRequestOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_OCTET_STREAM;
    }

    /**
     * 透传上报：身份来自前置认证（token 与路径一致），与 JSON 分支相同
     * <p>
     * Californium 在 Exchange 的串行执行器上处理请求，其它线程写的响应要排在本次处理之后才发出；
     * 所以这里不阻塞：解析与发送交给 worker，立即返回。截止计时归协议：到期先返回超时（101），
     * 之后完成的结果丢弃，只响应一次。已开始的投递无法撤销；其它情况由 service 的截止检查决定是否投递，超时响应不等待它
     */
    private CommonResult<Object> handleRawPost(CoapExchange exchange, String productKey, String deviceName) {
        // 1. CON 请求：先回空 ACK，避免处理期间设备重传；之后的响应作为分离响应发送
        if (exchange.advanced().getRequest().isConfirmable()) {
            exchange.accept();
        }

        // 2. 应答计时：在计时器线程上到期响应；截止时间从收到（交给 worker 之前）开始计算
        AtomicBoolean responded = new AtomicBoolean();
        long receivedNanos = System.nanoTime();
        ScheduledFuture<?> timer = deadlineTimer.schedule(() -> writeAsyncResponse(exchange, responded,
                CommonResult.error(SCRIPT_TIMEOUT)), deadline.toNanos(), TimeUnit.NANOSECONDS);

        // 3. 交给 worker：按产品的数据解析脚本 decode 后发送；队列已满时立即响应
        byte[] payload = exchange.getRequestPayload();
        try {
            workerExecutor.execute(() -> {
                // 排队期间已到截止时，由 service 的截止分支处理：不执行脚本、不投递，只记错误日志；
                // 超时响应已由计时器写出，这里的结果被门闩丢弃
                CommonResult<Object> result = invoke(productKey, deviceName, () -> deviceMessageService.sendRawMessage(
                        payload, productKey, deviceName, serverId, IotProtocolTypeEnum.COAP, receivedNanos));
                timer.cancel(false);
                writeAsyncResponse(exchange, responded, result);
            });
        } catch (RejectedExecutionException e) {
            log.warn("[handleRawPost][productKey({}) deviceName({}) 透传上报积压，拒绝处理]", productKey, deviceName);
            timer.cancel(false);
            writeAsyncResponse(exchange, responded, CommonResult.error(TOO_MANY_REQUESTS));
        }
        return ASYNC_RESPONSE;
    }

    /**
     * 发送上报（worker 上执行）：结果（含失败）转为响应，与基类的异常处理一致
     *
     * @param sender 发送，返回消息编号
     */
    private CommonResult<Object> invoke(String productKey, String deviceName, Supplier<String> sender) {
        try {
            return CommonResult.success(MapUtil.of("messageId", sender.get()));
        } catch (ServiceException e) {
            return CommonResult.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return CommonResult.error(BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[invoke][productKey({}) deviceName({}) 处理异常]", productKey, deviceName, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 写异步的响应：只响应一次（透传上报的截止计时器与处理线程竞争），迟到的结果丢弃
     */
    private void writeAsyncResponse(CoapExchange exchange, AtomicBoolean responded, CommonResult<Object> result) {
        if (!responded.compareAndSet(false, true)) {
            log.warn("[writeAsyncResponse][已响应（超时），丢弃结果({})]", result.getCode());
            return;
        }
        try {
            writeResponse(exchange, result);
        } catch (Exception e) {
            log.error("[writeAsyncResponse][写响应异常]", e);
        }
    }

    /**
     * 停止：关闭 worker（排队中的不再执行）与截止计时器
     */
    public void stop() {
        workerExecutor.shutdownNow();
        deadlineTimer.shutdownNow();
    }

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Override
    protected String getProductKey(List<String> uriPath) {
        // 路径格式：/topic/sys/{productKey}/{deviceName}/...
        return CollUtil.get(uriPath, 2);
    }

    @Override
    protected String getDeviceName(List<String> uriPath) {
        // 路径格式：/topic/sys/{productKey}/{deviceName}/...
        return CollUtil.get(uriPath, 3);
    }

}
