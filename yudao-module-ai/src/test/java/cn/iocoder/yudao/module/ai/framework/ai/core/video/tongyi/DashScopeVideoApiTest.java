package cn.iocoder.yudao.module.ai.framework.ai.core.video.tongyi;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoHttpClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskStatus;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.VideoMockServer;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阿里云百炼视频（{@link HappyHorseVideoApi}、{@link TongYiWanVideoApi}、{@link DashScopeVideoTaskClient}）的契约测试；
 * 请求经官方 SDK（dashscope-sdk-java）发出，断言以本地服务实际收到的地址、请求头、请求体为准
 *
 * @author 芋道源码
 */
public class DashScopeVideoApiTest {

    private static final String API_KEY = "sk-test-dashscope";

    // ========== HappyHorse 参数 ==========

    @Test
    public void testHappyHorse_t2v() {
        ObjectNode body = HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v")
                .setPrompt("一只猫").setResolution("480P").setRatio("16:9").setDuration(3).setWatermark(false).setSeed(1L));
        assertEquals("happyhorse-1.1-t2v", body.get("model").asText());
        assertEquals("一只猫", body.get("input").get("prompt").asText());
        assertFalse(body.get("input").has("media"));
        JsonNode parameters = body.get("parameters");
        assertEquals("480P", parameters.get("resolution").asText());
        assertEquals("16:9", parameters.get("ratio").asText());
        assertEquals(3, parameters.get("duration").asInt());
        assertFalse(parameters.get("watermark").asBoolean());
    }

    @Test
    public void testHappyHorse_i2vAndR2v() {
        ObjectNode i2v = HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.0-i2v")
                .setFirstFrame("https://a.com/1.png"));
        JsonNode media = i2v.get("input").get("media").get(0);
        assertEquals("first_frame", media.get("type").asText());
        assertEquals("https://a.com/1.png", media.get("url").asText());

        ObjectNode r2v = HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-r2v")
                .setPrompt("[图1]跳舞").setReferenceImages(ListUtil.toList("https://a.com/1.png", "https://a.com/2.png")));
        assertEquals(2, r2v.get("input").get("media").size());
        assertEquals("reference_image", r2v.get("input").get("media").get(1).get("type").asText());
    }

    @Test
    public void testHappyHorse_invalid() {
        // 图生不支持 ratio
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-i2v")
                .setFirstFrame("https://a.com/1.png").setRatio("16:9"))).isInstanceOf(IllegalArgumentException.class);
        // 图生需要首帧
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-i2v")))
                .isInstanceOf(IllegalArgumentException.class);
        // 文生不支持首帧、参考图
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v")
                .setPrompt("x").setFirstFrame("https://a.com/1.png"))).isInstanceOf(IllegalArgumentException.class);
        // 参考生需要 1 ~ 9 张
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-r2v")
                .setPrompt("x"))).isInstanceOf(IllegalArgumentException.class);
        // 时长 3 ~ 15
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v")
                .setPrompt("x").setDuration(2))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v")
                .setPrompt("x").setResolution("4K"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-2.0")
                .setPrompt("x"))).isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 通义万相参数 ==========

    @Test
    public void testWan_families() {
        assertEquals(TongYiWanVideoApi.Family.T2V_V27, TongYiWanVideoApi.getFamily("wan2.7-t2v"));
        assertEquals(TongYiWanVideoApi.Family.I2V_V27, TongYiWanVideoApi.getFamily("wan2.7-i2v"));
        assertEquals(TongYiWanVideoApi.Family.T2V_LEGACY, TongYiWanVideoApi.getFamily("wan2.2-t2v-plus"));
        assertEquals(TongYiWanVideoApi.Family.I2V_LEGACY, TongYiWanVideoApi.getFamily("wanx2.1-i2v-turbo"));
        assertThatThrownBy(() -> TongYiWanVideoApi.getFamily("wan9-t2v")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWan_t2vV27() {
        ObjectNode body = TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-t2v")
                .setPrompt("一只猫").setResolution("720P").setRatio("16:9").setDuration(2).setPromptExtend(false));
        assertEquals("720P", body.get("parameters").get("resolution").asText());
        assertEquals("16:9", body.get("parameters").get("ratio").asText());
        assertFalse(body.get("parameters").has("size"));
        // 2.7 不支持 size
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-t2v")
                .setPrompt("x").setSize("1280*720"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-t2v")
                .setPrompt("x").setResolution("480P"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-t2v")
                .setPrompt("x").setFirstFrame("https://a.com/1.png"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWan_i2vV27Media() {
        ObjectNode body = TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-i2v")
                .setFirstFrame("https://a.com/1.png").setLastFrame("oss://bucket/2.png").setDrivingAudio("https://a.com/a.mp3"));
        JsonNode media = body.get("input").get("media");
        assertEquals("first_frame", media.get(0).get("type").asText());
        assertEquals("last_frame", media.get(1).get("type").asText());
        assertEquals("driving_audio", media.get(2).get("type").asText());
        // 首帧与首段视频二选一；首段视频不能配驱动音频
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-i2v")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-i2v")
                .setFirstClip("https://a.com/c.mp4").setDrivingAudio("https://a.com/a.mp3")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.7-i2v")
                .setFirstFrame("https://a.com/1.png").setRatio("16:9"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWan_legacy() {
        ObjectNode t2v = TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wanx2.1-t2v-turbo")
                .setPrompt("一只猫").setSize("832*480").setDuration(5));
        assertEquals("832*480", t2v.get("parameters").get("size").asText());
        assertFalse(t2v.get("parameters").has("resolution"));
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wanx2.1-t2v-turbo")
                .setPrompt("x").setResolution("720P"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wanx2.1-t2v-turbo")
                .setPrompt("x").setSize("16:9"))).isInstanceOf(IllegalArgumentException.class);

        ObjectNode i2v = TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wanx2.1-i2v-turbo")
                .setFirstFrame("https://a.com/1.png").setResolution("480P").setDuration(3));
        assertEquals("https://a.com/1.png", i2v.get("input").get("img_url").asText());
        assertFalse(i2v.get("input").has("media"));
        // 分辨率、时长按模型校验
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wanx2.1-i2v-plus")
                .setFirstFrame("https://a.com/1.png").setResolution("480P"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.2-i2v-plus")
                .setFirstFrame("https://a.com/1.png").setDuration(10))).isInstanceOf(IllegalArgumentException.class);
        // 旧版图生不支持尾帧；shot_type 只有 2.6
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.6-i2v")
                .setFirstFrame("https://a.com/1.png").setLastFrame("https://a.com/2.png"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.2-t2v-plus")
                .setPrompt("x").setShotType("multi"))).isInstanceOf(IllegalArgumentException.class);
        assertEquals("multi", TongYiWanVideoApi.buildRequestBody(new TongYiWanVideoRequest().setModel("wan2.6-t2v")
                .setPrompt("x").setShotType("multi")).get("parameters").get("shot_type").asText());
    }

    // ========== 提交、查询 ==========

    @Test
    public void testSubmit() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"0385dc79-5ff8\"},\"request_id\":\"r-1\"}");
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url() + "/", API_KEY);

            AiVideoTaskRef ref = api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt("一只猫"));

            assertEquals("0385dc79-5ff8", ref.getTaskId());
            assertEquals("r-1", ref.getRequestId());
            assertEquals(HappyHorseVideoApi.PLATFORM, ref.getPlatform());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertEquals("/api/v1/services/aigc/video-generation/video-synthesis", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertEquals("enable", request.header("X-DashScope-Async"));
            assertEquals("happyhorse-1.1-t2v", AiVideoUtils.parseJson(request.body()).get("model").asText());
        }
    }

    @Test
    public void testSubmit_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(401, "{\"code\":\"InvalidApiKey\",\"message\":\"Invalid API-key provided: " + API_KEY + "\",\"request_id\":\"r\"}");
            server.enqueueJson(400, "{\"code\":\"Arrearage\",\"message\":\"Access denied, please make sure your account is in good standing.\"}");
            server.enqueueJson(403, "{\"code\":\"AccessDenied\",\"message\":\"Model access denied.\"}");
            server.enqueueJson(400, "{\"code\":\"DataInspectionFailed\",\"message\":\"Input data may contain inappropriate content.\"}");
            server.enqueueJson(429, "{\"code\":\"Throttling.RateQuota\",\"message\":\"Requests rate limit exceeded\"}");
            server.enqueueJson(500, "{\"code\":\"InternalError\",\"message\":\"internal\"}");
            server.enqueueJson(200, "{\"output\":{}}");
            TongYiWanVideoApi api = new TongYiWanVideoApi(server.url(), API_KEY);
            TongYiWanVideoRequest request = new TongYiWanVideoRequest().setModel("wan2.7-t2v").setPrompt("一只猫");

            assertKind(api, request, AiVideoException.Kind.AUTHENTICATION, false);
            assertKind(api, request, AiVideoException.Kind.INSUFFICIENT_BALANCE, false);
            assertKind(api, request, AiVideoException.Kind.PERMISSION, false);
            assertKind(api, request, AiVideoException.Kind.CONTENT_REJECTED, false);
            assertKind(api, request, AiVideoException.Kind.RATE_LIMITED, false);
            assertKind(api, request, AiVideoException.Kind.SERVER_ERROR, true);
            assertKind(api, request, AiVideoException.Kind.INVALID_RESPONSE, true);
            assertEquals(7, server.requests().size());
        }
    }

    private static void assertKind(TongYiWanVideoApi api, TongYiWanVideoRequest request, AiVideoException.Kind kind,
                                   boolean unknown) {
        assertThatThrownBy(() -> api.createTask(request)).isInstanceOfSatisfying(AiVideoException.class, ex -> {
            assertEquals(kind, ex.getKind());
            assertEquals(unknown, ex.isSubmitOutcomeUnknown());
            assertThat(ex.getMessage()).doesNotContain(API_KEY);
        });
    }

    @Test
    public void testSubmit_timeoutNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY, Duration.ofMillis(500));
            assertThatThrownBy(() -> api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt("x")))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                    });
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    // ========== SDK 适配契约：实际发出的请求、不重发、地域隔离 ==========

    @Test
    public void testSubmit_sdkRequestBody() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"t-1\"},\"request_id\":\"r-1\"}");
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY);
            // 接近上限的长提示词：SDK 不截断
            String prompt = "猫" + StrUtil.repeat('a', 4998);

            api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt(prompt));

            VideoMockServer.Request request = server.lastRequest();
            assertThat(request.header("Content-Type")).startsWith("application/json");
            assertThat(request.header("X-DashScope-OssResourceResolve")).isNull();
            JsonNode body = AiVideoUtils.parseJson(request.body());
            assertEquals(prompt, body.get("input").get("prompt").asText());
            // 不发送 SDK 为旧模型设置的默认值（extend_prompt、with_audio），未设置的尺寸、时长等也不发送
            assertEquals(1, body.get("input").size());
            JsonNode parameters = body.get("parameters");
            assertThat(parameters == null || parameters.isEmpty()).as("parameters: %s", parameters).isTrue();
            // buildRequestBody 与 SDK 实际发送的内容一致
            assertEquals(HappyHorseVideoApi.buildRequestBody(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v")
                    .setPrompt(prompt)), body);
        }
    }

    @Test
    public void testSubmit_sdkMediaAndOssHeader() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"t-1\"}}");
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"t-2\"}}");
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"t-3\"}}");
            TongYiWanVideoApi wan = new TongYiWanVideoApi(server.url(), API_KEY);
            HappyHorseVideoApi happyHorse = new HappyHorseVideoApi(server.url(), API_KEY);

            // 万相 2.7 图生：media 数组；含 oss:// 临时地址时加解析请求头
            wan.createTask(new TongYiWanVideoRequest().setModel("wan2.7-i2v").setFirstFrame("https://a.com/1.png")
                    .setLastFrame("oss://bucket/2.png").setResolution("720P").setDuration(2));
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("enable", request.header("X-DashScope-OssResourceResolve"));
            JsonNode body = AiVideoUtils.parseJson(request.body());
            assertEquals("oss://bucket/2.png", body.get("input").get("media").get(1).get("url").asText());
            assertEquals("720P", body.get("parameters").get("resolution").asText());
            assertEquals(2, body.get("parameters").get("duration").asInt());
            assertFalse(body.get("input").has("extend_prompt"));

            // 万相旧版图生：img_url
            wan.createTask(new TongYiWanVideoRequest().setModel("wanx2.1-i2v-turbo").setFirstFrame("https://a.com/1.png")
                    .setPromptExtend(false).setSeed(7L));
            body = AiVideoUtils.parseJson(server.lastRequest().body());
            assertEquals("https://a.com/1.png", body.get("input").get("img_url").asText());
            assertFalse(body.get("parameters").get("prompt_extend").asBoolean());
            assertEquals(7, body.get("parameters").get("seed").asInt());
            assertThat(server.lastRequest().header("X-DashScope-OssResourceResolve")).isNull();

            // HappyHorse 参考生：reference_image
            happyHorse.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-r2v").setPrompt("[图1]跳舞")
                    .setReferenceImages(ListUtil.toList("https://a.com/1.png", "https://a.com/2.png")));
            body = AiVideoUtils.parseJson(server.lastRequest().body());
            assertEquals("reference_image", body.get("input").get("media").get(0).get("type").asText());
            assertEquals("https://a.com/2.png", body.get("input").get("media").get(1).get("url").asText());
            assertEquals(3, server.requests().size());
        }
    }

    @Test
    public void testSubmit_regionIsolation() throws Exception {
        String globalBaseUrl = Constants.baseHttpApiUrl;
        try (VideoMockServer beijing = new VideoMockServer(); VideoMockServer singapore = new VideoMockServer()) {
            beijing.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"bj\"}}");
            singapore.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"sg\"}}");
            beijing.enqueueJson(200, "{\"output\":{\"task_id\":\"bj\",\"task_status\":\"RUNNING\"}}");
            TongYiWanVideoApi beijingApi = new TongYiWanVideoApi(beijing.url(), API_KEY);
            TongYiWanVideoApi singaporeApi = new TongYiWanVideoApi(singapore.url(), "sk-test-singapore");
            TongYiWanVideoRequest request = new TongYiWanVideoRequest().setModel("wan2.7-t2v").setPrompt("一只猫");

            assertEquals("bj", beijingApi.createTask(request).getTaskId());
            assertEquals("sg", singaporeApi.createTask(request).getTaskId());
            assertEquals(AiVideoTaskStatus.RUNNING, beijingApi.getTask("bj").getStatus());

            assertEquals(2, beijing.requests().size());
            assertEquals(1, singapore.requests().size());
            assertEquals("Bearer " + API_KEY, beijing.requests().get(0).header("Authorization"));
            assertEquals("Bearer sk-test-singapore", singapore.requests().get(0).header("Authorization"));
            // 不修改 SDK 的全局地址
            assertEquals(globalBaseUrl, Constants.baseHttpApiUrl);
        }
    }

    @Test
    public void testSubmit_503RetryAfterNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            // OkHttp 默认会对「503 + Retry-After: 0」自动重发
            server.enqueue(VideoMockServer.Response.json(503, "{\"code\":\"ServiceUnavailable\",\"message\":\"busy\"}")
                    .header("Retry-After", "0"));
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"t\"}}");
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY);
            assertThatThrownBy(() -> api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt("x")))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(503, ex.getHttpStatus());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                    });
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testSubmit_disconnectNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.disconnect());
            server.enqueueJson(200, "{\"output\":{\"task_status\":\"PENDING\",\"task_id\":\"t\"}}");
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY);
            assertThatThrownBy(() -> api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt("x")))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.NETWORK, ex.getKind());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                        assertThat(ex.getMessage()).doesNotContain(API_KEY);
                    });
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testSubmit_connectionRefusedIsNotUnknown() throws Exception {
        String url;
        try (VideoMockServer server = new VideoMockServer()) {
            url = server.url();
        }
        HappyHorseVideoApi api = new HappyHorseVideoApi(url, API_KEY);
        assertThatThrownBy(() -> api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt("x")))
                .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                    assertEquals(AiVideoException.Kind.NETWORK, ex.getKind());
                    assertFalse(ex.isSubmitOutcomeUnknown());
                });
    }

    @Test
    public void testSubmit_responseTooLarge() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"pad\":\""
                    + StrUtil.repeat('a', (int) AiVideoHttpClient.DEFAULT_MAX_RESPONSE_SIZE) + "\"}}");
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY);
            assertThatThrownBy(() -> api.createTask(new HappyHorseVideoRequest().setModel("happyhorse-1.1-t2v").setPrompt("x")))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.INVALID_RESPONSE, ex.getKind());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                    });
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testQuery_disconnectAndTimeout() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.disconnect());
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY, Duration.ofMillis(500));
            assertThatThrownBy(() -> api.getTask("t")).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.NETWORK, ex.getKind());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
            assertThatThrownBy(() -> api.getTask("t")).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
            Thread.sleep(300);
            assertEquals(2, server.requests().size());
        }
    }

    @Test
    public void testQuery_statuses() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"task_status\":\"PENDING\"}}");
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"task_status\":\"RUNNING\"}}");
            server.enqueueJson(200, "{\"request_id\":\"r\",\"output\":{\"task_id\":\"t\",\"task_status\":\"SUCCEEDED\","
                    + "\"video_url\":\"https://dashscope-result.oss-cn-beijing.aliyuncs.com/v.mp4?Signature=x\"},"
                    + "\"usage\":{\"duration\":3,\"output_video_duration\":3,\"SR\":480}}");
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"task_status\":\"SUCCEEDED\"}}");
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"task_status\":\"FAILED\",\"code\":\"DataInspectionFailed\","
                    + "\"message\":\"Output data may contain inappropriate content.\"}}");
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"task_status\":\"CANCELED\"}}");
            server.enqueueJson(200, "{\"output\":{\"task_id\":\"t\",\"task_status\":\"UNKNOWN\"}}");
            HappyHorseVideoApi api = new HappyHorseVideoApi(server.url(), API_KEY);

            assertEquals(AiVideoTaskStatus.QUEUED, api.getTask("t").getStatus());
            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask("t").getStatus());
            AiVideoTaskResult success = api.getTask("t");
            assertEquals(AiVideoTaskStatus.SUCCEEDED, success.getStatus());
            assertThat(success.getFirstVideoUrl()).startsWith("https://dashscope-result");
            assertEquals(3.0, success.getVideos().get(0).getDurationSeconds());
            assertEquals("480", success.getMetadata().get("usage.SR"));
            AiVideoTaskResult noOutput = api.getTask("t");
            assertEquals(AiVideoTaskStatus.FAILED, noOutput.getStatus());
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, noOutput.getErrorCode());
            AiVideoTaskResult failed = api.getTask("t");
            assertEquals(AiVideoTaskStatus.FAILED, failed.getStatus());
            assertEquals("DataInspectionFailed", failed.getErrorCode());
            assertEquals(AiVideoTaskStatus.CANCELLED, api.getTask("t").getStatus());
            AiVideoTaskResult unknown = api.getTask("t");
            assertEquals(AiVideoTaskStatus.UNKNOWN, unknown.getStatus());
            assertFalse(unknown.isFinished());

            assertEquals("GET", server.lastRequest().method());
            assertEquals("/api/v1/tasks/t", server.lastRequest().path());
            assertEquals("Bearer " + API_KEY, server.lastRequest().header("Authorization"));
            assertThat(server.lastRequest().header("X-DashScope-Async")).isNull();
            // 只按 task_id 查询：无请求体、无查询参数
            assertEquals(0, server.lastRequest().body().length);
            assertThat(server.lastRequest().query()).isNull();
            assertEquals(7, server.requests().size());
        }
    }

    @Test
    public void testQuery_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(401, "{\"code\":\"InvalidApiKey\",\"message\":\"Invalid API-key provided.\"}");
            server.enqueueJson(200, "{\"request_id\":\"r\"}");
            TongYiWanVideoApi api = new TongYiWanVideoApi(server.url(), API_KEY);
            assertThatThrownBy(() -> api.getTask("t")).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.AUTHENTICATION, ex.getKind());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
            assertThatThrownBy(() -> api.getTask("t")).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.INVALID_RESPONSE, ex.getKind()));
            assertThatThrownBy(() -> api.getTask("a/b")).isInstanceOf(IllegalArgumentException.class);
            assertEquals(2, server.requests().size());
        }
    }

    @Test
    public void testWorkspaceBaseUrl() {
        assertEquals("https://ws-abc.cn-beijing.maas.aliyuncs.com",
                DashScopeVideoTaskClient.workspaceBaseUrl("ws-abc", "cn-beijing"));
        assertThatThrownBy(() -> DashScopeVideoTaskClient.workspaceBaseUrl("evil.com/", "cn-beijing"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
