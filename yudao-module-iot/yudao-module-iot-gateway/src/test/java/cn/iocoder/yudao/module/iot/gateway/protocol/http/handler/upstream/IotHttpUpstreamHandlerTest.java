package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_OUTPUT_INVALID;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_REJECTED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotHttpUpstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotHttpUpstreamHandlerTest extends BaseMockitoUnitTest {

    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";
    private static final String TOKEN = "token";
    private static final String SERVER_ID = "http-server";
    private static final byte[] PAYLOAD = new byte[]{0x01, 0x02, 0x03};

    @Mock
    private IotDeviceTokenService deviceTokenService;
    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private IotHttpProtocol protocol;
    @Mock
    private RoutingContext context;
    @Mock
    private HttpServerRequest request;
    @Mock
    private RequestBody requestBody;

    private final IotGatewayProperties gatewayProperties = new IotGatewayProperties();
    /**
     * 响应是否已结束：end 之后 ended() 返回 true，与真实的 HttpServerResponse 一致
     */
    private final AtomicBoolean responseEnded = new AtomicBoolean();
    private HttpServerResponse response;
    private MockedStatic<SpringUtil> springUtilMockedStatic;
    private Vertx vertx;

    @BeforeEach
    public void setUp() {
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceTokenService.class)).thenReturn(deviceTokenService);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceMessageService.class)).thenReturn(deviceMessageService);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotGatewayProperties.class)).thenReturn(gatewayProperties);
        when(protocol.getServerId()).thenReturn(SERVER_ID);
        // 链式调用（setStatusCode、putHeader）返回自身
        response = mock(HttpServerResponse.class, RETURNS_SELF);
        lenient().when(response.ended()).thenAnswer(invocation -> responseEnded.get());
        lenient().doAnswer(invocation -> {
            responseEnded.set(true);
            return null;
        }).when(response).end(anyString());
        vertx = Vertx.vertx();
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
        vertx.close();
    }

    @Test
    public void testHandle_rawPostSuccess() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法：记录执行线程
        AtomicBoolean onWorker = new AtomicBoolean();
        when(deviceMessageService.sendRawMessage(aryEq(PAYLOAD), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID),
                eq(IotProtocolTypeEnum.HTTP), anyLong())).thenAnswer(invocation -> {
            onWorker.set(Context.isOnWorkerThread());
            return "M1";
        });

        // 调用
        handleOnEventLoop(handler);

        // 断言：只写一次 JSON 结果，data = {messageId}
        Map<String, Object> json = captureResponse();
        assertEquals(0, json.get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) json.get("data");
        assertEquals("M1", data.get("messageId"));
        assertEquals(1, data.size());
        verify(response, never()).end(any(Buffer.class));
        // 断言：在 worker 上处理
        assertTrue(onWorker.get());
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    @Test
    public void testHandle_rawPostReceivedBeforeQueue() throws Exception {
        // 准备参数：worker 池只有 1 个线程，并被占住，请求只能排队
        vertx.close();
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        vertx.executeBlocking(() -> {
            blocked.countDown();
            return release.await(10, TimeUnit.SECONDS);
        }, false);
        assertTrue(blocked.await(5, TimeUnit.SECONDS));
        // mock 方法
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenReturn("M5");

        long beginNanos;
        long queuedNanos;
        try {
            // 调用：请求排在 worker 队列中
            beginNanos = System.nanoTime();
            handleOnEventLoop(handler);
            queuedNanos = System.nanoTime();
            verify(deviceMessageService, never()).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
        } finally {
            release.countDown();
        }

        // 断言：放行后照常响应；传给 service 的收到时刻在交给 worker 之前取得（排队的时间计入截止），不是 worker 开始执行时
        assertEquals(0, captureResponse().get("code"));
        ArgumentCaptor<Long> receivedCaptor = ArgumentCaptor.forClass(Long.class);
        verify(deviceMessageService).sendRawMessage(aryEq(PAYLOAD), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID),
                eq(IotProtocolTypeEnum.HTTP), receivedCaptor.capture());
        assertTrue(receivedCaptor.getValue() >= beginNanos);
        assertTrue(receivedCaptor.getValue() <= queuedNanos);
    }

    @Test
    public void testHandle_rawPostTimerCancelled() throws Exception {
        // 准备参数：同步截止 100ms，处理很快完成
        gatewayProperties.getScript().setDeadline(Duration.ofMillis(100));
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenReturn("M2");

        // 调用
        handleOnEventLoop(handler);

        // 断言：先返回成功；过了截止时间也不再写超时
        assertEquals(0, captureResponse().get("code"));
        Thread.sleep(300);
        verify(response, times(1)).end(anyString());
    }

    @Test
    public void testHandle_rawPostServiceException() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法：脚本繁忙（service 不投递，只记错误日志）
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(exception(SCRIPT_REJECTED));

        // 调用
        handleOnEventLoop(handler);

        // 断言：返回对应的错误码
        Map<String, Object> json = captureResponse();
        assertEquals(SCRIPT_REJECTED.getCode(), json.get("code"));
        assertEquals(SCRIPT_REJECTED.getMsg(), json.get("msg"));
    }

    @Test
    public void testHandle_rawPostOutputInvalid() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(exception(SCRIPT_OUTPUT_INVALID, "脚本输出 不允许包含字段 raw"));

        // 调用
        handleOnEventLoop(handler);

        // 断言
        Map<String, Object> json = captureResponse();
        assertEquals(SCRIPT_OUTPUT_INVALID.getCode(), json.get("code"));
        assertEquals("数据解析脚本输出不合法：脚本输出 不允许包含字段 raw", json.get("msg"));
    }

    @Test
    public void testHandle_rawPostPayloadInvalid() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法：载荷长度不合法（不投递）
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(invalidParamException("raw 载荷长度必须在 1~{} 字节之间", 4096));

        // 调用
        handleOnEventLoop(handler);

        // 断言
        assertEquals(BAD_REQUEST.getCode(), captureResponse().get("code"));
    }

    @Test
    public void testHandle_rawPostUnknownException() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalStateException("boom"));

        // 调用
        handleOnEventLoop(handler);

        // 断言
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), captureResponse().get("code"));
    }

    @Test
    public void testHandle_rawPostSyncDeadline() throws Exception {
        // 准备参数：同步截止 100ms，处理阻塞到截止之后
        gatewayProperties.getScript().setDeadline(Duration.ofMillis(100));
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        CountDownLatch release = new CountDownLatch(1);
        // mock 方法
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenAnswer(invocation -> {
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return "M-late";
        });

        try {
            // 调用
            handleOnEventLoop(handler);

            // 断言：到期先返回 SCRIPT_TIMEOUT
            Map<String, Object> json = captureResponse();
            assertEquals(SCRIPT_TIMEOUT.getCode(), json.get("code"));
        } finally {
            release.countDown();
        }
        // 断言：之后完成的结果不再写（第二次检查 ended() 时丢弃）
        verify(response, timeout(5000).times(2)).ended();
        verify(response, times(1)).end(anyString());
        verify(response, never()).end(any(Buffer.class));
    }

    @Test
    public void testHandle_rawPostClientClosed() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockRawPostRequest();
        // mock 方法：客户端在处理期间断开
        when(response.closed()).thenReturn(true);
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenReturn("M3");

        // 调用
        handleOnEventLoop(handler);

        // 断言：丢弃结果，不写响应
        verify(response, timeout(5000)).closed();
        verify(response, never()).end(anyString());
        verify(response, never()).end(any(Buffer.class));
    }

    @Test
    public void testHandle_standardPost() throws Exception {
        // 准备参数：标准 JSON 上报
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockStandardPostRequest();
        // mock 方法：记录执行线程
        AtomicBoolean onWorker = new AtomicBoolean();
        doAnswer(invocation -> {
            onWorker.set(Context.isOnWorkerThread());
            ((IotDeviceMessage) invocation.getArgument(0)).setId("M4");
            return null;
        }).when(deviceMessageService).sendDeviceMessage(any(), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));

        // 调用
        handleOnEventLoop(handler);

        // 断言：发送完成后才响应，data = {messageId}
        Map<String, Object> json = captureResponse();
        assertEquals(0, json.get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) json.get("data");
        assertEquals("M4", data.get("messageId"));
        verify(response, times(1)).end(anyString());
        // 断言：获取设备、发送总线在 worker 上执行，不阻塞 event loop
        assertTrue(onWorker.get());
        verify(deviceMessageService, never()).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    public void testHandle_standardPostServiceException() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockStandardPostRequest();
        // mock 方法：设备不存在
        doThrow(invalidParamException("设备不存在")).when(deviceMessageService)
                .sendDeviceMessage(any(), any(), any(), any());

        // 调用
        handleOnEventLoop(handler);

        // 断言：与原来一致，返回 CommonResult 的错误码（HTTP 状态码不变）
        Map<String, Object> json = captureResponse();
        assertEquals(BAD_REQUEST.getCode(), json.get("code"));
        assertEquals("设备不存在", json.get("msg"));
    }

    @Test
    public void testHandle_standardPostUnknownException() throws Exception {
        // 准备参数
        IotHttpUpstreamHandler handler = new IotHttpUpstreamHandler(protocol);
        mockStandardPostRequest();
        // mock 方法
        doThrow(new IllegalStateException("boom")).when(deviceMessageService)
                .sendDeviceMessage(any(), any(), any(), any());

        // 调用
        handleOnEventLoop(handler);

        // 断言
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), captureResponse().get("code"));
    }

    // ========== 辅助方法 ==========

    private void mockRawPostRequest() {
        mockRequest("thing/raw/post", "application/octet-stream", PAYLOAD);
        when(context.vertx()).thenReturn(vertx);
    }

    private void mockStandardPostRequest() {
        mockRequest("thing/property/post", "application/json",
                "{\"method\":\"thing.property.post\",\"params\":{\"t\":1}}".getBytes());
        when(context.vertx()).thenReturn(vertx);
    }

    private void mockRequest(String method, String contentType, byte[] body) {
        // 请求：已认证（token 与路径一致）
        when(context.request()).thenReturn(request);
        when(context.response()).thenReturn(response);
        lenient().when(request.path()).thenReturn("/topic/sys/" + PRODUCT_KEY + "/" + DEVICE_NAME + "/" + method);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TOKEN);
        lenient().when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentType);
        when(context.pathParam("productKey")).thenReturn(PRODUCT_KEY);
        when(context.pathParam("deviceName")).thenReturn(DEVICE_NAME);
        when(context.pathParam("*")).thenReturn(method);
        when(context.body()).thenReturn(requestBody);
        when(requestBody.buffer()).thenReturn(Buffer.buffer(body));
        when(deviceTokenService.verifyToken(TOKEN)).thenReturn(new IotDeviceIdentity(PRODUCT_KEY, DEVICE_NAME));
    }

    /**
     * 在 Vert.x event loop 上执行 handle，与真实的 HTTP 请求一致
     */
    private void handleOnEventLoop(IotHttpUpstreamHandler handler) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        vertx.runOnContext(v -> {
            try {
                handler.handle(context);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * 等待并返回写出的 JSON 响应（只写一次）
     */
    private Map<String, Object> captureResponse() {
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, timeout(5000)).end(jsonCaptor.capture());
        return JsonUtils.parseMap(jsonCaptor.getValue());
    }

}
