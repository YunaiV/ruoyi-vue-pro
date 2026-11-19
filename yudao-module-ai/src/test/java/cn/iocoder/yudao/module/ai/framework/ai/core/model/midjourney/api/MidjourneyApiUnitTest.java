package cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link MidjourneyApi} 的单元测试类
 *
 * 通过 mock {@link ExchangeFunction} 截获真实 {@link WebClient} 发出的 {@link ClientRequest}，不访问外部服务、不监听本地端口。
 * 重点覆盖 Midjourney Proxy 的鉴权请求头：
 * 1. 保留原有的 Authorization: Bearer 请求头，兼容托管服务
 * 2. 补充 mj-api-secret 请求头，兼容 novicezk/midjourney-proxy 开启 mj.api-secret 后的鉴权
 * 3. 非 2xx 且无响应 body 时，仍抛出异常，不被吞掉
 * 4. 融图、换脸、局部重绘的请求路径与请求体；中转返回 404 时，转为明确的错误
 *
 * @author 芋道源码
 */
public class MidjourneyApiUnitTest extends BaseMockitoUnitTest {

    private static final String BASE_URL = "https://mj-proxy.example.com/mj";
    private static final String API_KEY = "test-mj-api-secret";
    private static final String NOTIFY_URL = "https://admin.example.com/admin-api/ai/image/midjourney/notify";

    @Mock
    private ExchangeFunction exchangeFunction;

    private MidjourneyApi midjourneyApi;

    @BeforeEach
    public void setUp() {
        midjourneyApi = createMidjourneyApi(API_KEY);
    }

    // ========== imagine ==========

    @Test
    public void testImagine_success() {
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "{\"code\":\"1\",\"description\":\"提交成功\",\"result\":\"task-1\"}");
        // 准备参数
        MidjourneyApi.ImagineRequest request = new MidjourneyApi.ImagineRequest(null, "一只猫", null, null);

        // 调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.imagine(request);
        // 断言：返回
        assertEquals("1", response.code());
        assertEquals("提交成功", response.description());
        assertEquals("task-1", response.result());
        assertEquals(NOTIFY_URL, request.getNotifyHook());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/submit/imagine");
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testImagine_withoutApiKey(String apiKey) {
        // 准备参数（未配置 apiKey）
        MidjourneyApi api = createMidjourneyApi(apiKey);
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "{\"code\":\"1\",\"description\":\"提交成功\",\"result\":\"task-1\"}");

        // 调用
        MidjourneyApi.SubmitResponse response = api.imagine(
                new MidjourneyApi.ImagineRequest(null, "一只猫", null, null));
        // 断言：返回
        assertEquals("task-1", response.result());
        // 断言：请求（Authorization 保持原行为，不补充 mj-api-secret）
        ClientRequest clientRequest = captureRequest();
        assertEquals(URI.create("https://mj-proxy.example.com/mj/submit/imagine"), clientRequest.url());
        assertEquals("Bearer " + apiKey, clientRequest.headers().getFirst(HttpHeaders.AUTHORIZATION));
        assertFalse(clientRequest.headers().containsKey("mj-api-secret"));
    }

    @Test
    public void testImagine_unauthorized() {
        // mock 方法（Midjourney Proxy 鉴权失败）
        mockResponse(HttpStatus.UNAUTHORIZED, MediaType.TEXT_PLAIN_VALUE, "Unauthorized");

        // 调用，并断言异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> midjourneyApi.imagine(
                new MidjourneyApi.ImagineRequest(null, "一只猫", null, null)));
        assertEquals("[midjourney-api] 调用失败！", exception.getMessage());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/submit/imagine");
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 500})
    public void testImagine_errorWithoutBody(int status) {
        // mock 方法（Midjourney Proxy 失败，且无响应 body）
        mockResponseWithoutBody(status);

        // 调用，并断言异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> midjourneyApi.imagine(
                new MidjourneyApi.ImagineRequest(null, "一只猫", null, null)));
        assertEquals("[midjourney-api] 调用失败！", exception.getMessage());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/submit/imagine");
    }

    // ========== action ==========

    @Test
    public void testAction_success() {
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "{\"code\":\"1\",\"description\":\"提交成功\",\"result\":\"task-2\"}");
        // 准备参数
        MidjourneyApi.ActionRequest request = new MidjourneyApi.ActionRequest("task-1",
                "MJ::JOB::upsample::1::task-1", null);

        // 调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.action(request);
        // 断言：返回
        assertEquals("1", response.code());
        assertEquals("task-2", response.result());
        assertEquals(NOTIFY_URL, request.getNotifyHook());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/submit/action");
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 500})
    public void testAction_errorWithoutBody(int status) {
        // mock 方法（Midjourney Proxy 失败，且无响应 body）
        mockResponseWithoutBody(status);

        // 调用，并断言异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> midjourneyApi.action(
                new MidjourneyApi.ActionRequest("task-1", "MJ::JOB::upsample::1::task-1", null)));
        assertEquals("[midjourney-api] 调用失败！", exception.getMessage());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/submit/action");
    }

    // ========== getTaskList ==========

    @Test
    public void testGetTaskList_success() {
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "[{\"id\":\"task-1\",\"status\":\"SUCCESS\",\"imageUrl\":\"https://cdn.example.com/task-1.png\"}]");

        // 调用
        List<MidjourneyApi.Notify> taskList = midjourneyApi.getTaskList(Collections.singletonList("task-1"));
        // 断言：返回
        assertEquals(1, taskList.size());
        assertEquals("task-1", taskList.get(0).id());
        assertEquals("SUCCESS", taskList.get(0).status());
        assertEquals("https://cdn.example.com/task-1.png", taskList.get(0).imageUrl());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/task/list-by-condition");
    }

    @Test
    public void testGetTaskList_successWithoutBody() {
        // mock 方法（Midjourney Proxy 成功，但无响应 body）
        mockResponseWithoutBody(200);

        // 调用
        List<MidjourneyApi.Notify> taskList = midjourneyApi.getTaskList(Collections.singletonList("task-1"));
        // 断言：返回（保持原有行为，返回空列表）
        assertTrue(taskList.isEmpty());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/task/list-by-condition");
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 500})
    public void testGetTaskList_errorWithoutBody(int status) {
        // mock 方法（Midjourney Proxy 失败，且无响应 body）
        mockResponseWithoutBody(status);

        // 调用，并断言异常（不能被当成空列表吞掉）
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> midjourneyApi.getTaskList(Collections.singletonList("task-1")));
        assertEquals("[midjourney-api] 调用失败！", exception.getMessage());
        // 断言：请求
        assertAuthorizedRequest(captureRequest(), "/task/list-by-condition");
    }

    // ========== blend、swapFace、modal ==========

    @Test
    public void testBlend_success() {
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "{\"code\":\"1\",\"description\":\"提交成功\",\"result\":\"task-3\"}");

        // 调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.blend(new MidjourneyApi.BlendRequest(
                Arrays.asList("data:image/png;base64,AAA", "data:image/png;base64,BBB"), "PORTRAIT", null, null));
        // 断言：返回
        assertEquals("task-3", response.result());
        // 断言：请求，未传回调地址时，使用默认的回调地址
        ClientRequest clientRequest = captureRequest();
        assertAuthorizedRequest(clientRequest, "/submit/blend");
        Map<String, Object> body = readBody(clientRequest);
        assertEquals(Arrays.asList("data:image/png;base64,AAA", "data:image/png;base64,BBB"), body.get("base64Array"));
        assertEquals("PORTRAIT", body.get("dimensions"));
        assertEquals(NOTIFY_URL, body.get("notifyHook"));
    }

    @Test
    public void testSwapFace_success() {
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "{\"code\":\"1\",\"description\":\"提交成功\",\"result\":\"task-4\"}");

        // 调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.swapFace(new MidjourneyApi.SwapFaceRequest(
                "data:image/png;base64,SRC", "data:image/png;base64,DST", "https://other/notify", null));
        // 断言：返回
        assertEquals("task-4", response.result());
        // 断言：请求，传了回调地址时，保持不变
        ClientRequest clientRequest = captureRequest();
        assertAuthorizedRequest(clientRequest, "/insight-face/swap");
        Map<String, Object> body = readBody(clientRequest);
        assertEquals("data:image/png;base64,SRC", body.get("sourceBase64"));
        assertEquals("data:image/png;base64,DST", body.get("targetBase64"));
        assertEquals("https://other/notify", body.get("notifyHook"));
    }

    @Test
    public void testModal_success() {
        // mock 方法（Midjourney Proxy 返回）
        mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE,
                "{\"code\":\"1\",\"description\":\"提交成功\",\"result\":\"task-5\"}");

        // 调用
        MidjourneyApi.SubmitResponse response = midjourneyApi.modal(new MidjourneyApi.ModalRequest(
                "task-modal", "换成红色的帽子", "data:image/png;base64,MASK"));
        // 断言：返回
        assertEquals("task-5", response.result());
        // 断言：请求，局部重绘不传回调地址
        ClientRequest clientRequest = captureRequest();
        assertAuthorizedRequest(clientRequest, "/submit/modal");
        Map<String, Object> body = readBody(clientRequest);
        assertEquals("task-modal", body.get("taskId"));
        assertEquals("换成红色的帽子", body.get("prompt"));
        assertEquals("data:image/png;base64,MASK", body.get("maskBase64"));
        assertFalse(body.containsKey("notifyHook"));
    }

    @Test
    public void testSwapFace_notFound() {
        // mock 方法（中转没有换脸接口）
        mockResponse(HttpStatus.NOT_FOUND, MediaType.TEXT_PLAIN_VALUE, "Not Found");

        // 调用，并断言异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> midjourneyApi.swapFace(
                new MidjourneyApi.SwapFaceRequest("data:image/png;base64,SRC", "data:image/png;base64,DST", null, null)));
        assertEquals("[midjourney-api] 中转不支持该接口：/mj/insight-face/swap", exception.getMessage());
    }

    // ========== 辅助方法 ==========

    /**
     * 构造真实的 {@link MidjourneyApi}，仅在构造期间将 {@link WebClient#builder()} 替换为使用 mock {@link ExchangeFunction} 的 builder
     */
    private MidjourneyApi createMidjourneyApi(String apiKey) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        try (MockedStatic<WebClient> webClientMock = mockStatic(WebClient.class)) {
            webClientMock.when(WebClient::builder).thenReturn(builder);
            return new MidjourneyApi(BASE_URL, apiKey, NOTIFY_URL);
        }
    }

    /**
     * mock 返回；与真实 {@link WebClient} 一致，响应关联本次请求，否则 {@link ClientResponse#request()} 的地址为空
     */
    private void mockResponse(HttpStatus status, String contentType, String body) {
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            return Mono.just(ClientResponse.create(status).header(HttpHeaders.CONTENT_TYPE, contentType).body(body)
                    .request(new org.springframework.mock.http.client.MockClientHttpRequest(request.method(), request.url()))
                    .build());
        });
    }

    /**
     * mock 无响应 body 的返回，即 body 为空 Flux，而不是空字符串
     */
    private void mockResponseWithoutBody(int status) {
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(
                ClientResponse.create(HttpStatus.valueOf(status)).build()));
    }

    private ClientRequest captureRequest() {
        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction).exchange(requestCaptor.capture());
        return requestCaptor.getValue();
    }

    /**
     * 读取请求体：把 {@link ClientRequest#body()} 写入 {@link MockClientHttpRequest}
     */
    private static Map<String, Object> readBody(ClientRequest request) {
        MockClientHttpRequest httpRequest = new MockClientHttpRequest(request.method(), request.url());
        request.body().insert(httpRequest, new BodyInserter.Context() {

            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return ExchangeStrategies.withDefaults().messageWriters();
            }

            @Override
            public Optional<ServerHttpRequest> serverRequest() {
                return Optional.empty();
            }

            @Override
            public Map<String, Object> hints() {
                return Collections.emptyMap();
            }

        }).block();
        return JsonUtils.parseMap(httpRequest.getBodyAsString().block());
    }

    private static void assertAuthorizedRequest(ClientRequest request, String path) {
        assertEquals(HttpMethod.POST, request.method());
        assertEquals(URI.create("https://mj-proxy.example.com/mj" + path), request.url());
        assertEquals(MediaType.APPLICATION_JSON, request.headers().getContentType());
        assertEquals("Bearer test-mj-api-secret", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("test-mj-api-secret", request.headers().getFirst("mj-api-secret"));
    }

}
