package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.TOO_MANY_REQUESTS;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.SCRIPT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link IotCoapUpstreamHandler} 的单元测试
 *
 * @author 芋道源码
 */
public class IotCoapUpstreamHandlerTest extends BaseMockitoUnitTest {

    private static final String PRODUCT_KEY = "pk";
    private static final String DEVICE_NAME = "dn";
    private static final String TOKEN = "token";
    private static final String SERVER_ID = "coap-server";
    private static final byte[] PAYLOAD = new byte[]{0x01, 0x02, 0x03};
    private static final List<String> RAW_POST_PATH = Arrays.asList("topic", "sys", PRODUCT_KEY, DEVICE_NAME,
            "thing", "raw", "post");
    private static final List<String> PROPERTY_POST_PATH = Arrays.asList("topic", "sys", PRODUCT_KEY, DEVICE_NAME,
            "thing", "property", "post");

    @Mock
    private IotDeviceTokenService deviceTokenService;
    @Mock
    private IotDeviceMessageService deviceMessageService;
    @Mock
    private CoapExchange exchange;
    @Mock
    private Exchange advancedExchange;
    @Mock
    private OptionSet options;
    @Mock
    private Option tokenOption;

    private final IotGatewayProperties gatewayProperties = new IotGatewayProperties();
    private MockedStatic<SpringUtil> springUtilMockedStatic;
    private IotCoapUpstreamHandler handler;

    @BeforeEach
    public void setUp() {
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceTokenService.class)).thenReturn(deviceTokenService);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotDeviceMessageService.class)).thenReturn(deviceMessageService);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(IotGatewayProperties.class)).thenReturn(gatewayProperties);
        handler = new IotCoapUpstreamHandler(SERVER_ID);
    }

    @AfterEach
    public void tearDown() {
        handler.stop();
        springUtilMockedStatic.close();
    }

    @Test
    public void testHandle_rawPostConfirmable() {
        // 准备参数
        mockRawPostRequest(CoAP.Type.CON);
        // mock 方法
        when(deviceMessageService.sendRawMessage(aryEq(PAYLOAD), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID),
                eq(IotProtocolTypeEnum.COAP), anyLong())).thenReturn("M1");

        // 调用
        handler.handle(exchange);

        // 断言：CON 先回空 ACK，再（在透传 worker 上）处理，最后以分离响应返回一次 JSON 结果，data = {messageId}
        Map<String, Object> json = captureResponse();
        InOrder inOrder = inOrder(exchange, deviceMessageService);
        inOrder.verify(exchange).accept();
        inOrder.verify(deviceMessageService).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
        inOrder.verify(exchange).respond(any(CoAP.ResponseCode.class), anyString(), anyInt());
        assertEquals(0, json.get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) json.get("data");
        assertEquals("M1", data.get("messageId"));
        assertEquals(1, data.size());
        verify(exchange, times(1)).respond(any(CoAP.ResponseCode.class), anyString(), anyInt());
        verify(exchange, never()).respond(any(CoAP.ResponseCode.class), any(byte[].class), anyInt());
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    @Test
    public void testHandle_rawPostNonConfirmable() {
        // 准备参数
        mockRawPostRequest(CoAP.Type.NON);
        // mock 方法
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenReturn("M2");

        // 调用
        handler.handle(exchange);

        // 断言：NON 不回空 ACK，直接响应一次 JSON 结果
        verify(exchange, never()).accept();
        Map<String, Object> json = captureResponse();
        assertEquals(0, json.get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) json.get("data");
        assertEquals("M2", data.get("messageId"));
        verify(exchange, never()).respond(any(CoAP.ResponseCode.class), any(byte[].class), anyInt());
    }

    @Test
    public void testHandle_rawPostServiceException() {
        // 准备参数
        mockRawPostRequest(CoAP.Type.CON);
        // mock 方法：处理超时（service 不投递，只记错误日志）
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(exception(SCRIPT_TIMEOUT));

        // 调用
        handler.handle(exchange);

        // 断言：CON 已先回空 ACK；截止前失败，响应一次错误 JSON
        verify(exchange).accept();
        Map<String, Object> json = captureResponse();
        assertEquals(SCRIPT_TIMEOUT.getCode(), json.get("code"));
        assertEquals(SCRIPT_TIMEOUT.getMsg(), json.get("msg"));
        verify(exchange, never()).respond(any(CoAP.ResponseCode.class), any(byte[].class), anyInt());
    }

    @Test
    public void testHandle_rawPostDeadline() throws Exception {
        // 准备参数：截止时长 100ms
        gatewayProperties.getScript().setDeadline(Duration.ofMillis(100));
        IotCoapUpstreamHandler deadlineHandler = new IotCoapUpstreamHandler(SERVER_ID);
        mockRawPostRequest(CoAP.Type.CON);
        // mock 方法：处理阻塞，直到放行后才返回（迟到的结果）
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong())).thenAnswer(invocation -> {
            entered.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS));
            return "LATE";
        });

        // mock 方法：与 Californium 一致，非处理线程写的响应排到 Exchange 的串行执行器（owner）上才发出
        ExecutorService owner = Executors.newSingleThreadExecutor();
        List<String> sent = new CopyOnWriteArrayList<>();
        CountDownLatch sentLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            String json = invocation.getArgument(1);
            owner.execute(() -> {
                sent.add(json);
                sentLatch.countDown();
            });
            return null;
        }).when(exchange).respond(any(CoAP.ResponseCode.class), anyString(), anyInt());

        try {
            // 调用：在 owner 上处理请求；处理入口立即返回，不等待阻塞中的发送
            owner.submit(() -> deadlineHandler.handle(exchange)).get(5, TimeUnit.SECONDS);
            assertTrue(entered.await(5, TimeUnit.SECONDS));

            // 断言：发送仍阻塞时，截止计时器的超时响应已经在 owner 上发出
            assertTrue(sentLatch.await(5, TimeUnit.SECONDS));
            assertEquals(1, release.getCount());
            Map<String, Object> json = JsonUtils.parseMap(sent.get(0));
            assertEquals(SCRIPT_TIMEOUT.getCode(), json.get("code"));
            assertEquals(SCRIPT_TIMEOUT.getMsg(), json.get("msg"));

            // 放行：等透传 worker 处理完，迟到的结果丢弃，不再响应
            release.countDown();
            ThreadPoolExecutor rawExecutor = getRawExecutor(deadlineHandler);
            rawExecutor.shutdown();
            assertTrue(rawExecutor.awaitTermination(5, TimeUnit.SECONDS));
            owner.submit(() -> null).get(5, TimeUnit.SECONDS);
            assertEquals(1, sent.size());
            verify(exchange).accept();
            verify(exchange, times(1)).respond(any(CoAP.ResponseCode.class), anyString(), anyInt());
            verify(deviceMessageService, times(1)).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
        } finally {
            release.countDown();
            deadlineHandler.stop();
            owner.shutdownNow();
        }
    }

    @Test
    public void testHandle_rawPostQueueExpired() throws Exception {
        // 准备参数：截止时长 100ms；透传 worker 只留 1 个线程，并被占住
        gatewayProperties.getScript().setDeadline(Duration.ofMillis(100));
        IotCoapUpstreamHandler deadlineHandler = new IotCoapUpstreamHandler(SERVER_ID);
        mockRawPostRequest(CoAP.Type.NON);
        ThreadPoolExecutor rawExecutor = getRawExecutor(deadlineHandler);
        rawExecutor.setCorePoolSize(1);
        rawExecutor.setMaximumPoolSize(1);
        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        rawExecutor.execute(() -> {
            blocked.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(blocked.await(5, TimeUnit.SECONDS));
        // mock 方法：与 service 的截止分支一致（已过截止：不执行脚本、不投递，只记错误日志，并返回超时）
        List<Long> nowAtCall = new CopyOnWriteArrayList<>();
        when(deviceMessageService.sendRawMessage(aryEq(PAYLOAD), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID),
                eq(IotProtocolTypeEnum.COAP), anyLong())).thenAnswer(invocation -> {
            nowAtCall.add(System.nanoTime());
            throw exception(SCRIPT_TIMEOUT);
        });

        try {
            // 调用：请求排在 worker 队列中
            long beginNanos = System.nanoTime();
            deadlineHandler.handle(exchange);
            long queuedNanos = System.nanoTime();

            // 断言：排队期间到期，计时器响应一次超时
            Map<String, Object> json = captureResponse();
            assertEquals(SCRIPT_TIMEOUT.getCode(), json.get("code"));

            // 放行：排队的请求带着入队之前的收到时刻交给 service（已过截止：不执行脚本、不投递，只记错误日志并抛出超时）；
            //      协议不再响应第二次
            release.countDown();
            rawExecutor.shutdown();
            assertTrue(rawExecutor.awaitTermination(5, TimeUnit.SECONDS));
            ArgumentCaptor<Long> receivedCaptor = ArgumentCaptor.forClass(Long.class);
            verify(deviceMessageService, times(1)).sendRawMessage(any(), any(), any(), any(), any(),
                    receivedCaptor.capture());
            assertTrue(receivedCaptor.getValue() >= beginNanos);
            assertTrue(receivedCaptor.getValue() <= queuedNanos);
            assertTrue(nowAtCall.get(0) >= receivedCaptor.getValue() + TimeUnit.MILLISECONDS.toNanos(100));
            verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
            verify(exchange, times(1)).respond(any(CoAP.ResponseCode.class), anyString(), anyInt());
        } finally {
            release.countDown();
            deadlineHandler.stop();
        }
    }

    @Test
    public void testHandle_rawPostRejected() {
        // 准备参数：透传 worker 拒绝（已关闭）
        mockRawPostRequest(CoAP.Type.NON);
        getRawExecutor(handler).shutdown();

        // 调用
        handler.handle(exchange);

        // 断言：立即响应一次 429，不调用发送
        assertEquals(TOO_MANY_REQUESTS.getCode(), captureResponse().get("code"));
        verify(deviceMessageService, never()).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    public void testHandle_rawPostUnknownException() {
        // 准备参数
        mockRawPostRequest(CoAP.Type.NON);
        // mock 方法：未知异常
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalStateException("boom"));

        // 调用
        handler.handle(exchange);

        // 断言：响应一次 500
        assertEquals(INTERNAL_SERVER_ERROR.getCode(), captureResponse().get("code"));
    }

    @Test
    public void testHandle_rawPostPayloadInvalid() {
        // 准备参数
        mockRawPostRequest(CoAP.Type.NON);
        // mock 方法：载荷长度不合法（不投递）
        when(deviceMessageService.sendRawMessage(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(invalidParamException("raw 载荷长度必须在 1~{} 字节之间", 4096));

        // 调用
        handler.handle(exchange);

        // 断言
        assertEquals(BAD_REQUEST.getCode(), captureResponse().get("code"));
    }

    @Test
    public void testHandle_rawPathNotOctetStream() {
        // 准备参数：raw/post 路径，但 content-format 不是 42：走 JSON 分支，交给标准路径（由它拒绝透传方法）
        mockRequest(RAW_POST_PATH, "{\"method\":\"thing.raw.post\"}".getBytes(StandardCharsets.UTF_8));
        when(options.getContentFormat()).thenReturn(MediaTypeRegistry.APPLICATION_JSON);

        // 调用
        handler.handle(exchange);

        // 断言：不走透传
        verify(deviceMessageService, never()).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
        verify(deviceMessageService).sendDeviceMessage(any(), eq(PRODUCT_KEY), eq(DEVICE_NAME), eq(SERVER_ID));
        verify(exchange, never()).accept();
    }

    // ========== 标准上报、认证 ==========

    @Test
    public void testHandle_propertyPost() {
        // 准备参数：JSON 属性上报
        mockRequest(PROPERTY_POST_PATH, "{\"method\":\"thing.property.post\",\"params\":{\"t\":25}}"
                .getBytes(StandardCharsets.UTF_8));

        // 调用
        handler.handle(exchange);

        // 断言：同步响应成功；按路径的设备身份交给标准路径，不走透传
        assertEquals(0, captureResponse().get("code"));
        ArgumentCaptor<IotDeviceMessage> messageCaptor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(deviceMessageService).sendDeviceMessage(messageCaptor.capture(), eq(PRODUCT_KEY), eq(DEVICE_NAME),
                eq(SERVER_ID));
        assertEquals("thing.property.post", messageCaptor.getValue().getMethod());
        assertEquals(25, ((Map<?, ?>) messageCaptor.getValue().getParams()).get("t"));
        verify(deviceMessageService, never()).sendRawMessage(any(), any(), any(), any(), any(), anyLong());
        verify(exchange, never()).accept();
    }

    @Test
    public void testHandle_propertyPostEmptyPayload() {
        // 准备参数：没有请求体
        mockRequest(PROPERTY_POST_PATH, new byte[0]);

        // 调用
        handler.handle(exchange);

        // 断言：400，不投递
        assertEquals(BAD_REQUEST.getCode(), captureResponse().get("code"));
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    @Test
    public void testHandle_methodMismatch() {
        // 准备参数：路径是属性上报，请求体是事件上报
        mockRequest(PROPERTY_POST_PATH, "{\"method\":\"thing.event.post\",\"params\":{}}"
                .getBytes(StandardCharsets.UTF_8));

        // 调用
        handler.handle(exchange);

        // 断言：400，不投递
        assertEquals(BAD_REQUEST.getCode(), captureResponse().get("code"));
        verify(deviceMessageService, never()).sendDeviceMessage(any(), any(), any(), any());
    }

    @Test
    public void testHandle_tokenMissing() {
        // 准备参数：没有 token
        when(exchange.getRequestOptions()).thenReturn(options);
        when(options.getOthers()).thenReturn(Collections.emptyList());

        // 调用
        handler.handle(exchange);

        // 断言：401，不投递
        assertEquals(UNAUTHORIZED.getCode(), captureResponse().get("code"));
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testHandle_tokenInvalid() {
        // 准备参数：token 校验不通过
        mockToken();
        when(deviceTokenService.verifyToken(TOKEN)).thenReturn(null);

        // 调用
        handler.handle(exchange);

        // 断言：401，不投递
        assertEquals(UNAUTHORIZED.getCode(), captureResponse().get("code"));
        verifyNoInteractions(deviceMessageService);
    }

    @Test
    public void testHandle_deviceMismatch() {
        // 准备参数：token 属于其它设备
        mockToken();
        when(options.getUriPath()).thenReturn(PROPERTY_POST_PATH);
        when(deviceTokenService.verifyToken(TOKEN)).thenReturn(new IotDeviceIdentity(PRODUCT_KEY, "other"));

        // 调用
        handler.handle(exchange);

        // 断言：403，不投递
        assertEquals(FORBIDDEN.getCode(), captureResponse().get("code"));
        verifyNoInteractions(deviceMessageService);
    }

    // ========== 辅助方法 ==========

    private void mockRawPostRequest(CoAP.Type type) {
        // 请求：thing/raw/post、content-format 42
        mockRequest(RAW_POST_PATH, PAYLOAD);
        when(options.getContentFormat()).thenReturn(MediaTypeRegistry.APPLICATION_OCTET_STREAM);
        when(exchange.advanced()).thenReturn(advancedExchange);
        when(advancedExchange.getRequest()).thenReturn(new Request(CoAP.Code.POST, type));
    }

    private void mockRequest(List<String> uriPath, byte[] payload) {
        // 请求：已认证（token 与路径一致）
        mockToken();
        when(options.getUriPath()).thenReturn(uriPath);
        when(exchange.getRequestPayload()).thenReturn(payload);
        when(deviceTokenService.verifyToken(TOKEN)).thenReturn(new IotDeviceIdentity(PRODUCT_KEY, DEVICE_NAME));
    }

    private void mockToken() {
        // 请求：带 token 的自定义 Option
        when(exchange.getRequestOptions()).thenReturn(options);
        when(options.getOthers()).thenReturn(Collections.singletonList(tokenOption));
        when(tokenOption.getNumber()).thenReturn(IotCoapAbstractHandler.OPTION_TOKEN);
        when(tokenOption.getValue()).thenReturn(TOKEN.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 等待（透传在 worker 上异步响应）并断言只响应一次 JSON，返回它
     */
    private Map<String, Object> captureResponse() {
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(exchange, timeout(5000)).respond(eq(CoAP.ResponseCode.CONTENT), jsonCaptor.capture(),
                eq(MediaTypeRegistry.APPLICATION_JSON));
        verify(exchange, times(1)).respond(any(CoAP.ResponseCode.class), anyString(), anyInt());
        return JsonUtils.parseMap(jsonCaptor.getValue());
    }

    /**
     * 获取处理器的透传 worker
     */
    private static ThreadPoolExecutor getRawExecutor(IotCoapUpstreamHandler handler) {
        return (ThreadPoolExecutor) ReflectUtil.getFieldValue(handler, "rawExecutor");
    }

}
