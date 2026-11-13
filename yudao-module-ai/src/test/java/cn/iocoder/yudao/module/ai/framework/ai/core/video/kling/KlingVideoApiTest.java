package cn.iocoder.yudao.module.ai.framework.ai.core.video.kling;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskStatus;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.VideoMockServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link KlingVideoApi}、{@link KlingCredentials} 的契约测试
 *
 * @author 芋道源码
 */
public class KlingVideoApiTest {

    private static final String API_KEY = "test-kling-api-key";

    private static KlingVideoRequest t2v() {
        return new KlingVideoRequest().setModel(KlingVideoApi.MODEL_2_5_TURBO).setPrompt("一只猫")
                .setResolution("720p").setAspectRatio("16:9").setDuration(5);
    }

    // ========== 鉴权 ==========

    @Test
    public void testJwt() {
        Clock clock = Clock.fixed(Instant.ofEpochSecond(1763020800L), ZoneOffset.UTC);
        KlingCredentials credentials = KlingCredentials.ofAccessKey("ak-test", "sk-test", clock);
        String jwt = credentials.createJwt();
        // 独立计算（Python hmac）的期望值
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhay10ZXN0IiwiZXhwIjoxNzYzMDIyNjAwLCJuYmYiOjE3NjMwMjA3OTV9"
                + ".55zTebkVLiSa4Nfibx67uHZ97rf_e1Cc6lFAUvFTjaY", jwt);
        // 再用 Hutool JWT 校验
        assertTrue(JWTUtil.verify(jwt, "sk-test".getBytes(StandardCharsets.UTF_8)));
        JWT parsed = JWTUtil.parseToken(jwt);
        assertEquals("ak-test", parsed.getPayload("iss"));
        assertEquals("Bearer " + jwt, credentials.getAuthorization());
        assertFalse(credentials.isApiKey());
        assertThat(credentials.toString()).doesNotContain("ak-test", "sk-test");

        assertEquals("Bearer " + API_KEY, KlingCredentials.ofApiKey(API_KEY).getAuthorization());
        assertThatThrownBy(() -> KlingCredentials.ofAccessKey("ak", " ")).isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 新版参数 ==========

    @Test
    public void testBuildRequestBody_t2v() {
        Object[] built = KlingVideoApi.buildRequestBody(t2v().setWatermark(false).setExternalTaskId("biz-1"));
        assertEquals(KlingVideoApi.ROUTE_TEXT_TO_VIDEO, built[0]);
        ObjectNode body = (ObjectNode) built[1];
        assertEquals("一只猫", body.get("prompt").asText());
        assertEquals("720p", body.get("settings").get("resolution").asText());
        assertEquals("16:9", body.get("settings").get("aspect_ratio").asText());
        assertEquals(5, body.get("settings").get("duration").asInt());
        assertFalse(body.get("options").get("watermark_info").get("enabled").asBoolean());
        assertEquals("biz-1", body.get("options").get("external_task_id").asText());
        assertFalse(body.has("model"));
    }

    @Test
    public void testBuildRequestBody_i2v() {
        Object[] built = KlingVideoApi.buildRequestBody(new KlingVideoRequest().setModel(KlingVideoApi.MODEL_2_6)
                .setPrompt("动起来").setFirstFrame("https://a.com/1.png").setLastFrame("https://a.com/2.png").setResolution("1080p"));
        assertEquals(KlingVideoApi.ROUTE_IMAGE_TO_VIDEO, built[0]);
        JsonNode contents = ((ObjectNode) built[1]).get("contents");
        assertEquals("prompt", contents.get(0).get("type").asText());
        assertEquals("first_frame", contents.get(1).get("type").asText());
        assertEquals("https://a.com/1.png", contents.get(1).get("url").asText());
        assertEquals("last_frame", contents.get(2).get("type").asText());
    }

    @Test
    public void testBuildRequestBody_invalid() {
        // 旧版 model_name 不能走新版接口
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setModel("kling-v2-6")))
                .isInstanceOf(IllegalArgumentException.class);
        // 新版不支持 negative_prompt / cfg_scale / mode
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setNegativePrompt("x")))
                .isInstanceOf(IllegalArgumentException.class);
        // 2.5-turbo 时长只支持 5 / 10
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setDuration(7))).isInstanceOf(IllegalArgumentException.class);
        // 2.5-turbo 不支持 21:9
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setAspectRatio("21:9")))
                .isInstanceOf(IllegalArgumentException.class);
        // 原生音频只有 2.6 且 1080p
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setAudio(true))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setModel(KlingVideoApi.MODEL_2_6).setAudio(true)))
                .isInstanceOf(IllegalArgumentException.class);
        // 图生不支持 aspect_ratio；尾帧需要首帧
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setFirstFrame("https://a.com/1.png")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setLastFrame("https://a.com/1.png")))
                .isInstanceOf(IllegalArgumentException.class);
        // 4.0 时长 3 ~ 30，支持 21:9
        KlingVideoApi.buildRequestBody(t2v().setModel(KlingVideoApi.MODEL_4_0).setAspectRatio("21:9").setDuration(30));
        assertThatThrownBy(() -> KlingVideoApi.buildRequestBody(t2v().setModel(KlingVideoApi.MODEL_4_0).setDuration(31)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 旧版参数 ==========

    @Test
    public void testBuildLegacyRequestBody() {
        Object[] built = KlingVideoApi.buildLegacyRequestBody(new KlingVideoRequest().setModel("kling-v2-1").setPrompt("一只猫")
                .setNegativePrompt("模糊").setCfgScale(0.5).setMode("std").setAspectRatio("9:16").setDuration(10));
        assertEquals(KlingVideoApi.ROUTE_LEGACY_TEXT2VIDEO, built[0]);
        ObjectNode body = (ObjectNode) built[1];
        assertEquals("kling-v2-1", body.get("model_name").asText());
        assertEquals("10", body.get("duration").asText());
        assertTrue(body.get("duration").isTextual());
        assertEquals("std", body.get("mode").asText());

        Object[] i2v = KlingVideoApi.buildLegacyRequestBody(new KlingVideoRequest().setModel("kling-v2-1")
                .setFirstFrame("https://a.com/1.png").setLastFrame("https://a.com/2.png"));
        assertEquals(KlingVideoApi.ROUTE_LEGACY_IMAGE2VIDEO, i2v[0]);
        assertEquals("https://a.com/1.png", ((ObjectNode) i2v[1]).get("image").asText());
        assertEquals("https://a.com/2.png", ((ObjectNode) i2v[1]).get("image_tail").asText());

        assertThatThrownBy(() -> KlingVideoApi.buildLegacyRequestBody(t2v())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KlingVideoApi.buildLegacyRequestBody(new KlingVideoRequest().setModel("kling-v2-1")
                .setPrompt("x").setCfgScale(2.0))).isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 提交 ==========

    @Test
    public void testCreateTask() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":0,\"message\":\"success\",\"request_id\":\"r-1\",\"data\":{\"id\":\"task-1\","
                    + "\"status\":\"submitted\"}}");
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofApiKey(API_KEY));

            AiVideoTaskRef ref = api.createTask(t2v());

            assertEquals("task-1", ref.getTaskId());
            assertEquals(KlingVideoApi.ROUTE_TEXT_TO_VIDEO, ref.getRoute());
            assertEquals("r-1", ref.getRequestId());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertEquals("/text-to-video/kling-2.5-turbo", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertEquals("一只猫", AiVideoUtils.parseJson(request.body()).get("prompt").asText());
        }
    }

    @Test
    public void testCreateTask_accessKeyRequiresLegacy() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofAccessKey("ak", "sk"));
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOf(IllegalArgumentException.class);
            assertEquals(0, server.requests().size());
        }
    }

    @Test
    public void testCreateLegacyTask_jwt() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":0,\"request_id\":\"r\",\"data\":{\"task_id\":\"legacy-1\",\"task_status\":\"submitted\"}}");
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofAccessKey("ak-test", "sk-test"));

            AiVideoTaskRef ref = api.createLegacyTask(new KlingVideoRequest().setModel("kling-v2-1").setPrompt("一只猫"));

            assertEquals("legacy-1", ref.getTaskId());
            assertEquals(KlingVideoApi.ROUTE_LEGACY_TEXT2VIDEO, ref.getRoute());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("/v1/videos/text2video", request.path());
            String jwt = request.header("Authorization").substring("Bearer ".length());
            assertTrue(JWTUtil.verify(jwt, "sk-test".getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void testCreateTask_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(401, "{\"code\":1002,\"message\":\"Authorization is invalid " + API_KEY + "\",\"request_id\":\"r\"}");
            server.enqueueJson(429, "{\"code\":1102,\"message\":\"Account balance not enough\"}");
            server.enqueueJson(400, "{\"code\":1301,\"message\":\"Trigger the content security policy\"}");
            server.enqueueJson(429, "{\"code\":1303,\"message\":\"Trigger resource pack concurrency\"}");
            server.enqueueJson(500, "{\"code\":5000,\"message\":\"Server internal error\"}");
            server.enqueueJson(200, "{\"code\":0,\"data\":{}}");
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofApiKey(API_KEY));

            assertKind(api, AiVideoException.Kind.AUTHENTICATION, false);
            assertKind(api, AiVideoException.Kind.INSUFFICIENT_BALANCE, false);
            assertKind(api, AiVideoException.Kind.CONTENT_REJECTED, false);
            assertKind(api, AiVideoException.Kind.RATE_LIMITED, false);
            assertKind(api, AiVideoException.Kind.SERVER_ERROR, true);
            assertKind(api, AiVideoException.Kind.INVALID_RESPONSE, true);
            assertEquals(6, server.requests().size());
        }
    }

    private static void assertKind(KlingVideoApi api, AiVideoException.Kind kind, boolean unknown) {
        assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
            assertEquals(kind, ex.getKind());
            assertEquals(unknown, ex.isSubmitOutcomeUnknown());
            assertThat(ex.getMessage()).doesNotContain(API_KEY);
        });
    }

    @Test
    public void testCreateTask_timeoutNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofApiKey(API_KEY), Duration.ofMillis(500));
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                assertTrue(ex.isSubmitOutcomeUnknown());
            });
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    // ========== 查询 ==========

    @Test
    public void testGetTask_new() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":0,\"data\":[{\"id\":\"task-1\",\"status\":\"submitted\"}]}");
            server.enqueueJson(200, "{\"code\":0,\"data\":[{\"id\":\"task-1\",\"status\":\"processing\"}]}");
            server.enqueueJson(200, "{\"code\":0,\"data\":[{\"id\":\"task-1\",\"status\":\"succeeded\",\"outputs\":["
                    + "{\"type\":\"image\",\"url\":\"https://a.com/cover.png\"},"
                    + "{\"type\":\"video\",\"url\":\"https://v.klingai.com/v.mp4\",\"watermark_url\":\"https://v.klingai.com/w.mp4\","
                    + "\"duration\":\"5.1\"}]}]}");
            server.enqueueJson(200, "{\"code\":0,\"data\":[{\"id\":\"task-1\",\"status\":\"succeeded\",\"outputs\":[]}]}");
            server.enqueueJson(200, "{\"code\":0,\"data\":[{\"id\":\"task-1\",\"status\":\"failed\",\"message\":\"content risk\"}]}");
            server.enqueueJson(200, "{\"code\":0,\"data\":[]}");
            server.enqueueJson(200, "{\"code\":1203,\"message\":\"task not found\"}");
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofApiKey(API_KEY));
            AiVideoTaskRef ref = new AiVideoTaskRef(KlingVideoApi.PLATFORM, KlingVideoApi.MODEL_2_6, "task-1",
                    KlingVideoApi.ROUTE_IMAGE_TO_VIDEO, null);

            assertEquals(AiVideoTaskStatus.QUEUED, api.getTask(ref).getStatus());
            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask(ref).getStatus());
            AiVideoTaskResult success = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.SUCCEEDED, success.getStatus());
            assertEquals(1, success.getVideos().size());
            assertEquals("https://v.klingai.com/v.mp4", success.getFirstVideoUrl());
            assertEquals("https://v.klingai.com/w.mp4", success.getVideos().get(0).getWatermarkUrl());
            assertEquals(5.1, success.getVideos().get(0).getDurationSeconds());
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, api.getTask(ref).getErrorCode());
            assertEquals(AiVideoTaskStatus.FAILED, api.getTask(ref).getStatus());
            // 列表中没有该任务：未知
            assertEquals(AiVideoTaskStatus.UNKNOWN, api.getTask(ref).getStatus());
            assertThatThrownBy(() -> api.getTask(ref)).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.NOT_FOUND, ex.getKind()));

            assertEquals("GET", server.lastRequest().method());
            assertEquals("/tasks", server.lastRequest().path());
            assertEquals("task_ids=task-1", server.lastRequest().query());
            assertThat(server.requests()).noneMatch(r -> "POST".equals(r.method()));
        }
    }

    @Test
    public void testGetTask_legacy() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":0,\"data\":{\"task_id\":\"l-1\",\"task_status\":\"processing\"}}");
            server.enqueueJson(200, "{\"code\":0,\"data\":{\"task_id\":\"l-1\",\"task_status\":\"succeed\",\"task_result\":"
                    + "{\"videos\":[{\"id\":\"v\",\"url\":\"https://v.klingai.com/l.mp4\",\"duration\":\"5\"}]}}}");
            server.enqueueJson(200, "{\"code\":0,\"data\":{\"task_id\":\"l-1\",\"task_status\":\"failed\",\"task_status_msg\":\"risk\"}}");
            KlingVideoApi api = new KlingVideoApi(server.url(), KlingCredentials.ofAccessKey("ak", "sk"));
            AiVideoTaskRef ref = new AiVideoTaskRef(KlingVideoApi.PLATFORM, "kling-v2-1", "l-1",
                    KlingVideoApi.ROUTE_LEGACY_IMAGE2VIDEO, null);

            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask(ref).getStatus());
            assertEquals("https://v.klingai.com/l.mp4", api.getTask(ref).getFirstVideoUrl());
            AiVideoTaskResult failed = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.FAILED, failed.getStatus());
            assertEquals("risk", failed.getErrorMessage());
            assertEquals("/v1/videos/image2video/l-1", server.lastRequest().path());

            // 未知路由不发请求
            ref.setRoute("unknown");
            assertThatThrownBy(() -> api.getTask(ref)).isInstanceOf(IllegalArgumentException.class);
            assertEquals(3, server.requests().size());
        }
    }

}
