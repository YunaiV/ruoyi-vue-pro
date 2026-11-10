package cn.iocoder.yudao.module.ai.framework.ai.core.model.suno;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SunoApi} 的单元测试类
 *
 * 通过 mock {@link ExchangeFunction} 截获真实 {@link WebClient} 发出的 {@link ClientRequest}，不访问外部服务。
 * 覆盖 New API /suno/* 的请求地址、鉴权、请求体，以及 {code, message, data} 返回的解析与异常
 *
 * @author 芋道源码
 */
public class SunoApiUnitTest extends BaseMockitoUnitTest {

    private static final String BASE_URL = "https://relay.example.com/";
    private static final String API_KEY = "test-suno-key";
    private static final String APIMAX_BASE_URL = "https://api.apimax.ai";

    @Mock
    private ExchangeFunction exchangeFunction;

    private SunoApi sunoApi;

    @BeforeEach
    public void setUp() {
        sunoApi = createSunoApi(API_KEY);
    }

    // ========== submitMusic ==========

    @Test
    public void testSubmitMusic_inspiration() {
        // mock 方法
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"message\":\"\",\"data\":\"task-1\"}");
        // 准备参数
        SunoApi.MusicRequest request = new SunoApi.MusicRequest("夏日海边", null, null, null,
                "chirp-v4", false, null, null, null);

        // 调用
        String taskId = sunoApi.submitMusic(request);
        // 断言：返回
        assertEquals("task-1", taskId);
        // 断言：请求
        ClientRequest clientRequest = captureRequest();
        assertRequest(clientRequest, "/suno/submit/music");
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("夏日海边", body.get("gpt_description_prompt").asText());
        assertEquals("chirp-v4", body.get("mv").asText());
        assertFalse(body.get("make_instrumental").asBoolean());
        // 空字段不传，避免中转误判为自定义或续写
        assertFalse(body.has("prompt"));
        assertFalse(body.has("task_id"));
        assertFalse(body.has("continue_clip_id"));
        assertFalse(body.has("continue_at"));
    }

    @Test
    public void testSubmitMusic_extend() {
        // mock 方法
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task-2\"}");
        // 准备参数
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null, "[Verse]\n歌词", "pop", "续写",
                "chirp-v4", false, "task-1", "clip-1", 60);

        // 调用
        String taskId = sunoApi.submitMusic(request);
        // 断言
        assertEquals("task-2", taskId);
        JsonNode body = JsonUtils.parseTree(readBody(captureRequest()));
        assertEquals("task-1", body.get("task_id").asText());
        assertEquals("clip-1", body.get("continue_clip_id").asText());
        assertEquals(60, body.get("continue_at").asInt());
        assertEquals("[Verse]\n歌词", body.get("prompt").asText());
    }

    @Test
    public void testSubmitMusic_withoutApiKey() {
        // 准备参数
        sunoApi = createSunoApi(null);
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task-1\"}");

        // 调用
        sunoApi.submitMusic(new SunoApi.MusicRequest("a", null, null, null, "chirp-v4", false, null, null, null));
        // 断言：不带 Authorization
        assertNull(captureRequest().headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testSubmitMusic_codeFail() {
        // mock 方法：中转返回业务失败
        mockResponse(HttpStatus.OK, "{\"code\":\"fail\",\"message\":\"余额不足\",\"data\":null}");

        // 调用并断言：原因原样抛出
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sunoApi.submitMusic(
                new SunoApi.MusicRequest("a", null, null, null, "chirp-v4", false, null, null, null)));
        assertEquals("余额不足", exception.getMessage());
    }

    @Test
    public void testSubmitMusic_withoutTaskId() {
        // mock 方法：成功但没有 data
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":null}");

        // 调用并断言
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sunoApi.submitMusic(
                new SunoApi.MusicRequest("a", null, null, null, "chirp-v4", false, null, null, null)));
        assertTrue(exception.getMessage().contains("未返回任务编号"));
    }

    @Test
    public void testSubmitMusic_httpError() {
        // mock 方法：HTTP 401，New API 的 error.message 格式
        mockResponse(HttpStatus.UNAUTHORIZED, "{\"error\":{\"message\":\"无效的令牌\"}}");

        // 调用并断言
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sunoApi.submitMusic(
                new SunoApi.MusicRequest("a", null, null, null, "chirp-v4", false, null, null, null)));
        assertTrue(exception.getMessage().contains("401"), exception.getMessage());
        assertTrue(exception.getMessage().contains("无效的令牌"), exception.getMessage());
    }

    @Test
    public void testSubmitMusic_notJson() {
        // mock 方法：网关返回 HTML
        mockResponse(HttpStatus.OK, "<html>bad gateway</html>");

        // 调用并断言
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sunoApi.submitMusic(
                new SunoApi.MusicRequest("a", null, null, null, "chirp-v4", false, null, null, null)));
        assertEquals("中转返回格式不正确", exception.getMessage());
    }

    // ========== submitMusic：apimax ==========

    @Test
    public void testSubmitMusic_apimaxInspiration() {
        // 准备参数
        sunoApi = createSunoApi(APIMAX_BASE_URL, API_KEY);
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"message\":\"\",\"data\":\"task_1\"}");
        SunoApi.MusicRequest request = new SunoApi.MusicRequest("原创纯音乐", null, null, null,
                "suno_music", true, null, null, null);

        // 调用
        String taskId = sunoApi.submitMusic(request);
        // 断言：走原生接口
        assertEquals("task_1", taskId);
        ClientRequest clientRequest = captureRequest();
        assertEquals(URI.create("https://api.apimax.ai/v1/music/generations"), clientRequest.url());
        assertEquals("Bearer test-suno-key", clientRequest.headers().getFirst(HttpHeaders.AUTHORIZATION));
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("suno_music", body.get("model").asText());
        assertEquals("原创纯音乐", body.get("prompt").asText());
        assertTrue(body.get("instrumental").asBoolean());
        assertFalse(body.has("lyrics"));
        assertFalse(body.has("gpt_description_prompt"));
        assertFalse(body.has("mv"));
    }

    @Test
    public void testSubmitMusic_apimaxCustom() {
        // 准备参数：根地址带结尾的 /
        sunoApi = createSunoApi(APIMAX_BASE_URL + "/", API_KEY);
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task_2\"}");
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null, "[Verse]\n歌词", "pop", "夏日",
                "suno_music", false, null, null, null);

        // 调用
        sunoApi.submitMusic(request);
        // 断言：歌词放 lyrics，风格放 style
        ClientRequest clientRequest = captureRequest();
        assertEquals(URI.create("https://api.apimax.ai/v1/music/generations"), clientRequest.url());
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("[Verse]\n歌词", body.get("lyrics").asText());
        assertEquals("pop", body.get("style").asText());
        assertEquals("夏日", body.get("title").asText());
        assertFalse(body.get("instrumental").asBoolean());
        assertFalse(body.has("prompt"));
        assertFalse(body.has("tags"));
    }

    @Test
    public void testSubmitMusic_apimaxCustomInstrumental() {
        // 准备参数：与 AiMusicServiceImpl 自定义纯音乐的请求形状一致，歌词为空字符串
        sunoApi = createSunoApi(APIMAX_BASE_URL, API_KEY);
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task_4\"}");
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null, "", "lofi, piano", "夜晚",
                "suno_music", true, null, null, null);

        // 调用
        sunoApi.submitMusic(request);
        // 断言：风格作为描述 prompt，不传空 lyrics，style、title 照常传
        ClientRequest clientRequest = captureRequest();
        assertEquals(URI.create("https://api.apimax.ai/v1/music/generations"), clientRequest.url());
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("lofi, piano", body.get("prompt").asText());
        assertEquals("lofi, piano", body.get("style").asText());
        assertEquals("夜晚", body.get("title").asText());
        assertTrue(body.get("instrumental").asBoolean());
        assertFalse(body.has("lyrics"));
    }

    @Test
    public void testSubmitMusic_apimaxExtend() {
        // 准备参数：续写不走原生接口
        sunoApi = createSunoApi(APIMAX_BASE_URL, API_KEY);
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task_3\"}");
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null, "[Verse]\n歌词", "pop", "续写",
                "suno_music", false, "task_1", "clip-1", 60);

        // 调用
        sunoApi.submitMusic(request);
        // 断言：仍走兼容路径，续写字段原样传递
        ClientRequest clientRequest = captureRequest();
        assertEquals(URI.create("https://api.apimax.ai/suno/submit/music"), clientRequest.url());
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("task_1", body.get("task_id").asText());
        assertEquals("clip-1", body.get("continue_clip_id").asText());
        assertEquals("[Verse]\n歌词", body.get("prompt").asText());
        assertFalse(body.has("lyrics"));
    }

    @Test
    public void testSubmitMusic_apimaxSimilarDomain() {
        // 准备参数：相近域名不是 apimax 官方 API
        sunoApi = createSunoApi("https://api.apimax.ai.example.com", API_KEY);
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task-1\"}");

        // 调用
        sunoApi.submitMusic(new SunoApi.MusicRequest("a", null, null, null, "chirp-v4", false, null, null, null));
        // 断言：走兼容路径
        assertEquals(URI.create("https://api.apimax.ai.example.com/suno/submit/music"), captureRequest().url());
    }

    // ========== submitConcat、uploadAudioUrl ==========

    @Test
    public void testSubmitConcat() {
        // mock 方法
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task-3\"}");

        // 调用
        String taskId = sunoApi.submitConcat("clip-2");
        // 断言
        assertEquals("task-3", taskId);
        ClientRequest clientRequest = captureRequest();
        assertRequest(clientRequest, "/suno/submit/concat");
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("clip-2", body.get("clip_id").asText());
        assertFalse(body.get("is_infill").asBoolean());
    }

    @Test
    public void testUploadAudioUrl() {
        // mock 方法
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":\"task-4\"}");

        // 调用
        String taskId = sunoApi.uploadAudioUrl("https://example.com/a.mp3");
        // 断言
        assertEquals("task-4", taskId);
        ClientRequest clientRequest = captureRequest();
        assertRequest(clientRequest, "/suno/uploads/audio-url");
        assertEquals("https://example.com/a.mp3", JsonUtils.parseTree(readBody(clientRequest)).get("url").asText());
    }

    @Test
    public void testUploadAudioUrl_unsupported() {
        // mock 方法：中转未实现上传扩展路径（例如官方 SunoAPI 插件）；响应需带上请求地址，用于错误信息中的路径
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest request = invocation.getArgument(0);
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND)
                    .request(new org.springframework.mock.http.client.MockClientHttpRequest(request.method(), request.url()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":{\"message\":\"Invalid URL (POST /suno/uploads/audio-url)\"}}").build());
        });

        // 调用并断言：只给出路径，不暴露中转地址
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sunoApi.uploadAudioUrl("https://example.com/a.mp3"));
        assertEquals("当前中转不支持该接口（HTTP 404）：/suno/uploads/audio-url", exception.getMessage());
    }

    // ========== fetchTasks ==========

    @Test
    public void testFetchTasks_success() {
        // mock 方法：一首成功、一首失败
        mockResponse(HttpStatus.OK, """
                {"code":"success","data":[{"task_id":"task-1","action":"MUSIC","status":"SUCCESS","progress":"100%",
                  "data":[{"id":"clip-1","title":"夏日","status":"complete","audio_url":"https://cdn.example.com/1.mp3",
                    "image_url":"https://cdn.example.com/1.png","image_large_url":"https://cdn.example.com/1-large.png",
                    "video_url":"","model_name":"chirp-v4","metadata":{"tags":"pop","prompt":"[Verse]","duration":176.5,"error_message":null}},
                   {"id":"clip-2","status":"error","metadata":{"error_message":"内容违规"}}]}]}
                """);

        // 调用
        List<SunoApi.Task> tasks = sunoApi.fetchTasks(List.of("task-1"));
        // 断言：请求
        ClientRequest clientRequest = captureRequest();
        assertRequest(clientRequest, "/suno/fetch");
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("task-1", body.get("ids").get(0).asText());
        assertEquals("MUSIC", body.get("action").asText());
        // 断言：任务
        assertEquals(1, tasks.size());
        SunoApi.Task task = tasks.get(0);
        assertTrue(task.isSuccess());
        assertFalse(task.isFailure());
        assertEquals("100%", task.progress());
        // 断言：歌曲
        SunoApi.Clip clip1 = task.data().get(0);
        assertEquals("clip-1", clip1.id());
        assertEquals("https://cdn.example.com/1-large.png", clip1.coverUrl());
        assertEquals(176.5, clip1.metadata().duration());
        assertEquals("[Verse]", clip1.metadata().prompt());
        assertNull(clip1.errorMessage());
        SunoApi.Clip clip2 = task.data().get(1);
        assertEquals("内容违规", clip2.errorMessage());
    }

    @Test
    public void testFetchTasks_failure() {
        // mock 方法
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":[{\"task_id\":\"task-1\",\"status\":\"FAILURE\",\"fail_reason\":\"超时\"}]}");

        // 调用
        List<SunoApi.Task> tasks = sunoApi.fetchTasks(List.of("task-1"));
        // 断言
        assertTrue(tasks.get(0).isFailure());
        assertEquals("超时", tasks.get(0).failReason());
    }

    @Test
    public void testFetchTasks_empty() {
        // 调用：空参数不发请求
        assertTrue(sunoApi.fetchTasks(Collections.emptyList()).isEmpty());
        verifyNoInteractions(exchangeFunction);
    }

    @Test
    public void testFetchTasks_dataNotArray() {
        // mock 方法
        mockResponse(HttpStatus.OK, "{\"code\":\"success\",\"data\":{\"task_id\":\"task-1\"}}");

        // 调用并断言
        assertThrows(IllegalStateException.class, () -> sunoApi.fetchTasks(List.of("task-1")));
    }

    @Test
    public void testFetchTasks_mixed() {
        // mock 方法：同一批次，已提交任务的 data 是对象；缺少 task_id 的任务；成功任务里有空值、非对象、格式错误的歌曲
        mockResponse(HttpStatus.OK, """
                {"code":"success","data":[
                  {"task_id":"task-1","action":"MUSIC","status":"SUBMITTED","progress":"0%","data":{"clip_ids":["a","b"]}},
                  {"status":"SUCCESS","data":[]},
                  null,
                  {"task_id":null,"status":"SUCCESS"},
                  {"task_id":"task-2","action":"MUSIC","status":"SUCCESS","progress":"100%","fail_reason":null,
                   "data":[null,"clip-x",{"id":"clip-bad","metadata":{"duration":"很长"}},
                     {"id":"clip-1","status":"complete","audio_url":"https://cdn.example.com/1.mp3"}]}]}
                """);

        // 调用
        List<SunoApi.Task> tasks = sunoApi.fetchTasks(List.of("task-1", "task-2"));
        // 断言：只跳过无法识别的任务，其它任务照常返回
        assertEquals(2, tasks.size());
        assertEquals("task-1", tasks.get(0).taskId());
        assertEquals("0%", tasks.get(0).progress());
        assertTrue(tasks.get(0).data().isEmpty());
        assertFalse(tasks.get(0).isSuccess());
        // 断言：成功任务只保留可解析的歌曲
        SunoApi.Task task2 = tasks.get(1);
        assertTrue(task2.isSuccess());
        assertNull(task2.failReason());
        assertEquals(List.of("clip-1"), task2.data().stream().map(SunoApi.Clip::id).toList());
    }

    @Test
    public void testFetchTasks_infai() {
        // mock 方法：InfAI 的歌曲，id 是中转的记录编号、clipId 才是歌曲编号，status 为整数，
        // 地址为 cld2*Url，风格、歌词、时长在歌曲顶层
        mockResponse(HttpStatus.OK, """
                {"code":"success","data":[{"task_id":"task-1","action":"MUSIC","status":"SUCCESS","progress":"100%",
                  "data":[{"id":"2120087722226888705","clipId":"clip-1","title":"夏日","status":30,"progress":100,
                    "cld2AudioUrl":"https://cdn.example.com/1.m4a","cld2ImageUrl":"https://cdn.example.com/1.jpeg",
                    "cld2VideoUrl":"https://cdn.example.com/1-video.mp4","duration":183.56,"tags":"pop","prompt":"[Verse] 1"},
                   {"id":"2120087722226888706","clipId":"clip-2","title":"夏日","status":30,"progress":100,
                    "cld2AudioUrl":"https://cdn.example.com/2.m4a","cld2ImageUrl":"https://cdn.example.com/2.jpeg",
                    "cld2VideoUrl":"","duration":"175.2","tags":"pop","makeInstrumental":true}]}]}
                """);

        // 调用
        List<SunoApi.Task> tasks = sunoApi.fetchTasks(List.of("task-1"));
        // 断言：请求
        ClientRequest clientRequest = captureRequest();
        assertRequest(clientRequest, "/suno/fetch");
        JsonNode body = JsonUtils.parseTree(readBody(clientRequest));
        assertEquals("task-1", body.get("ids").get(0).asText());
        assertEquals("MUSIC", body.get("action").asText());
        // 断言：两首歌曲
        assertEquals(1, tasks.size());
        assertTrue(tasks.get(0).isSuccess());
        List<SunoApi.Clip> clips = tasks.get(0).data();
        assertEquals(2, clips.size());
        // 断言：歌曲编号取 clipId，不取中转的记录编号 id；整数 status 读为字符串
        SunoApi.Clip clip1 = clips.get(0);
        assertEquals("clip-1", clip1.id());
        assertEquals("30", clip1.status());
        assertEquals("https://cdn.example.com/1.m4a", clip1.audioUrl());
        assertEquals("https://cdn.example.com/1.jpeg", clip1.coverUrl());
        assertEquals("https://cdn.example.com/1-video.mp4", clip1.videoUrl());
        assertEquals(183.56, clip1.metadata().duration());
        assertEquals("pop", clip1.metadata().tags());
        assertEquals("[Verse] 1", clip1.metadata().prompt());
        assertNull(clip1.errorMessage());
        SunoApi.Clip clip2 = clips.get(1);
        assertEquals("clip-2", clip2.id());
        assertEquals("30", clip2.status());
        assertEquals("https://cdn.example.com/2.m4a", clip2.audioUrl());
        assertEquals("https://cdn.example.com/2.jpeg", clip2.coverUrl());
        assertEquals("", clip2.videoUrl());
        assertEquals(175.2, clip2.metadata().duration());
        assertNull(clip2.metadata().prompt());
        assertNull(clip2.errorMessage());
    }

    @Test
    public void testFetchTasks_salpx() {
        // mock 方法：SalpX 的 data 为对象，clips 以歌曲编号为键，sunoData 是同一批歌曲
        mockResponse(HttpStatus.OK, """
                {"code":"success","data":[{"task_id":"task-1","action":"MUSIC","status":"SUCCESS","progress":"100%",
                  "data":{"task_id":"task-1","status":"SUCCESS",
                    "clips":{
                      "clip-1":{"id":"clip-1","clip_id":"clip-1","title":"夏日","status":"complete",
                        "audio_url":"https://cdn.example.com/1.mp3","image_url":"https://cdn.example.com/1.png",
                        "image_large_url":"https://cdn.example.com/1-large.png","video_url":"","model_name":"chirp-v6",
                        "metadata":{"tags":"pop","prompt":"[Verse] 1","duration":201.5,"error_message":null}},
                      "clip-2":{"id":"clip-2","clip_id":"clip-2","title":"夏日","status":"complete",
                        "audio_url":"https://cdn.example.com/2.mp3","image_url":"https://cdn.example.com/2.png",
                        "metadata":{"tags":"pop","prompt":"[Verse] 2","duration":198.0}}},
                    "sunoData":[{"id":"clip-1","audio_url":"https://cdn.example.com/1.mp3"},
                                {"id":"clip-2","audio_url":"https://cdn.example.com/2.mp3"}]}}]}
                """);

        // 调用
        List<SunoApi.Task> tasks = sunoApi.fetchTasks(List.of("task-1"));
        // 断言：只解析 clips，不因 sunoData 重复
        assertEquals(1, tasks.size());
        assertTrue(tasks.get(0).isSuccess());
        List<SunoApi.Clip> clips = tasks.get(0).data();
        assertEquals(List.of("clip-1", "clip-2"), clips.stream().map(SunoApi.Clip::id).toList());
        SunoApi.Clip clip1 = clips.get(0);
        assertEquals("https://cdn.example.com/1.mp3", clip1.audioUrl());
        assertEquals("https://cdn.example.com/1-large.png", clip1.coverUrl());
        assertEquals(201.5, clip1.metadata().duration());
        assertEquals("pop", clip1.metadata().tags());
        assertEquals("[Verse] 1", clip1.metadata().prompt());
        assertNull(clip1.errorMessage());
        SunoApi.Clip clip2 = clips.get(1);
        assertEquals("https://cdn.example.com/2.mp3", clip2.audioUrl());
        assertEquals("https://cdn.example.com/2.png", clip2.coverUrl());
        assertEquals(198.0, clip2.metadata().duration());
    }

    // ========== 工具方法 ==========

    private SunoApi createSunoApi(String apiKey) {
        return createSunoApi(BASE_URL, apiKey);
    }

    private SunoApi createSunoApi(String baseUrl, String apiKey) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        try (MockedStatic<WebClient> webClientMock = mockStatic(WebClient.class)) {
            webClientMock.when(WebClient::builder).thenReturn(builder);
            return new SunoApi(baseUrl, apiKey);
        }
    }

    private void mockResponse(HttpStatus status, String body) {
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(
                ClientResponse.create(status).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(body).build()));
    }

    private ClientRequest captureRequest() {
        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction).exchange(requestCaptor.capture());
        return requestCaptor.getValue();
    }

    /**
     * 把请求体写入 {@link MockClientHttpRequest}，读取为字符串
     */
    private static String readBody(ClientRequest request) {
        MockClientHttpRequest httpRequest = new MockClientHttpRequest(request.method(), request.url());
        BodyInserter<?, ? super ClientHttpRequest> inserter = request.body();
        inserter.insert(httpRequest, new BodyInserter.Context() {

            @Override
            public List<org.springframework.http.codec.HttpMessageWriter<?>> messageWriters() {
                return ExchangeStrategies.withDefaults().messageWriters();
            }

            @Override
            public Optional<org.springframework.http.server.reactive.ServerHttpRequest> serverRequest() {
                return Optional.empty();
            }

            @Override
            public java.util.Map<String, Object> hints() {
                return Collections.emptyMap();
            }

        }).block();
        return httpRequest.getBodyAsString().block();
    }

    private static void assertRequest(ClientRequest request, String path) {
        assertEquals(HttpMethod.POST, request.method());
        assertEquals(URI.create("https://relay.example.com" + path), request.url());
        assertEquals(MediaType.APPLICATION_JSON, request.headers().getContentType());
        assertEquals("Bearer test-suno-key", request.headers().getFirst(HttpHeaders.AUTHORIZATION));
    }

}
