package cn.iocoder.yudao.module.ai.framework.ai.core.video.doubao;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoException;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskRef;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoTaskStatus;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.AiVideoUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.video.VideoMockServer;
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
 * {@link DouBaoSeedanceVideoApi} 的契约测试
 *
 * @author 芋道源码
 */
public class DouBaoSeedanceVideoApiTest {

    private static final String API_KEY = "test-ark-key";
    private static final String MODEL_PRO_FAST = "doubao-seedance-1-0-pro-fast-251015";
    private static final String MODEL_2_0 = "doubao-seedance-2-0-260128";

    private static DouBaoSeedanceVideoRequest t2v() {
        return new DouBaoSeedanceVideoRequest().setModel(MODEL_PRO_FAST).setPrompt("一只猫")
                .setResolution("480p").setRatio("16:9").setDuration(2).setWatermark(false);
    }

    // ========== 参数 ==========

    @Test
    public void testBuildRequestBody_t2vAndFrames() {
        ObjectNode body = DouBaoSeedanceVideoApi.buildRequestBody(t2v().setSeed(7L).setCameraFixed(true));
        assertEquals(MODEL_PRO_FAST, body.get("model").asText());
        assertEquals("text", body.get("content").get(0).get("type").asText());
        assertEquals("480p", body.get("resolution").asText());
        assertEquals(2, body.get("duration").asInt());
        assertEquals(7, body.get("seed").asInt());
        assertTrue(body.get("camera_fixed").asBoolean());
        assertFalse(body.get("watermark").asBoolean());

        ObjectNode fl = DouBaoSeedanceVideoApi.buildRequestBody(t2v().setFirstFrame("https://a.com/1.png")
                .setLastFrame("asset://asset-1"));
        JsonNode first = fl.get("content").get(1);
        assertEquals("image_url", first.get("type").asText());
        assertEquals("https://a.com/1.png", first.get("image_url").get("url").asText());
        assertEquals("first_frame", first.get("role").asText());
        assertEquals("last_frame", fl.get("content").get(2).get("role").asText());
    }

    @Test
    public void testBuildRequestBody_v20Reference() {
        ObjectNode body = DouBaoSeedanceVideoApi.buildRequestBody(new DouBaoSeedanceVideoRequest().setModel(MODEL_2_0)
                .setPrompt("参考视频1的运镜").setReferenceImages(ListUtil.toList("https://a.com/1.png"))
                .setReferenceVideos(ListUtil.toList("https://a.com/1.mp4")).setReferenceAudios(ListUtil.toList("https://a.com/1.mp3"))
                .setGenerateAudio(true).setDuration(5));
        assertEquals("reference_image", body.get("content").get(1).get("role").asText());
        assertEquals("video_url", body.get("content").get(2).get("type").asText());
        assertEquals("reference_audio", body.get("content").get(3).get("role").asText());
        assertTrue(body.get("generate_audio").asBoolean());
    }

    @Test
    public void testBuildRequestBody_invalid() {
        // 尾帧需要首帧
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setLastFrame("https://a.com/2.png")))
                .isInstanceOf(IllegalArgumentException.class);
        // 首帧与参考图不能混用
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setFirstFrame("https://a.com/1.png")
                .setReferenceImages(ListUtil.toList("https://a.com/2.png")))).isInstanceOf(IllegalArgumentException.class);
        // 1.0 不支持参考视频、生成音频
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v()
                .setReferenceVideos(ListUtil.toList("https://a.com/1.mp4")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setGenerateAudio(true)))
                .isInstanceOf(IllegalArgumentException.class);
        // 2.0 不能单独参考音频；2.0 不支持 seed、frames；2.0 时长 4 ~ 15
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(new DouBaoSeedanceVideoRequest().setModel(MODEL_2_0)
                .setPrompt("x").setReferenceAudios(ListUtil.toList("https://a.com/1.mp3")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(new DouBaoSeedanceVideoRequest().setModel(MODEL_2_0)
                .setPrompt("x").setSeed(1L))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(new DouBaoSeedanceVideoRequest().setModel(MODEL_2_0)
                .setPrompt("x").setDuration(2))).isInstanceOf(IllegalArgumentException.class);
        // 4k 只有 2.0
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setResolution("4k")))
                .isInstanceOf(IllegalArgumentException.class);
        // frames 与 duration 二选一；frames 需 25 + 4n
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setFrames(29)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setDuration(null).setFrames(30)))
                .isInstanceOf(IllegalArgumentException.class);
        assertEquals(29, DouBaoSeedanceVideoApi.buildRequestBody(t2v().setDuration(null).setFrames(29)).get("frames").asInt());
        // lite-t2v 只支持文生
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setModel("doubao-seedance-1-0-lite-t2v-250428")
                .setFirstFrame("https://a.com/1.png"))).isInstanceOf(IllegalArgumentException.class);
        // 文生需要提示词
        assertThatThrownBy(() -> DouBaoSeedanceVideoApi.buildRequestBody(t2v().setPrompt(null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 提交 ==========

    @Test
    public void testCreateTask() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{\"id\":\"cgt-20251113-abc\"}").header("X-Request-Id", "req-1"));
            DouBaoSeedanceVideoApi api = new DouBaoSeedanceVideoApi(server.url() + "/", API_KEY);

            AiVideoTaskRef ref = api.createTask(t2v());

            assertEquals("cgt-20251113-abc", ref.getTaskId());
            assertEquals("req-1", ref.getRequestId());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertEquals("/api/v3/contents/generations/tasks", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertEquals(MODEL_PRO_FAST, AiVideoUtils.parseJson(request.body()).get("model").asText());
        }
    }

    @Test
    public void testCreateTask_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(401, "{\"error\":{\"code\":\"AuthenticationError\",\"message\":\"The API key " + API_KEY + " is invalid\"}}");
            server.enqueueJson(403, "{\"error\":{\"code\":\"AccountOverdueError\",\"message\":\"overdue\"}}");
            server.enqueueJson(404, "{\"error\":{\"code\":\"ModelNotOpen\",\"message\":\"not activated\"}}");
            server.enqueueJson(400, "{\"error\":{\"code\":\"InputTextSensitiveContentDetected\",\"message\":\"sensitive\"}}");
            server.enqueueJson(429, "{\"error\":{\"code\":\"RateLimitExceeded.EndpointRPMExceeded\",\"message\":\"limit\"}}");
            server.enqueueJson(500, "{\"error\":{\"code\":\"InternalServiceError\",\"message\":\"internal\"}}");
            server.enqueueJson(200, "{}");
            DouBaoSeedanceVideoApi api = new DouBaoSeedanceVideoApi(server.url(), API_KEY);

            assertKind(api, AiVideoException.Kind.AUTHENTICATION, false);
            assertKind(api, AiVideoException.Kind.INSUFFICIENT_BALANCE, false);
            assertKind(api, AiVideoException.Kind.PERMISSION, false);
            assertKind(api, AiVideoException.Kind.CONTENT_REJECTED, false);
            assertKind(api, AiVideoException.Kind.RATE_LIMITED, false);
            assertKind(api, AiVideoException.Kind.SERVER_ERROR, true);
            assertKind(api, AiVideoException.Kind.INVALID_RESPONSE, true);
            assertEquals(7, server.requests().size());
        }
    }

    private static void assertKind(DouBaoSeedanceVideoApi api, AiVideoException.Kind kind, boolean unknown) {
        assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
            assertEquals(kind, ex.getKind());
            assertEquals(unknown, ex.isSubmitOutcomeUnknown());
            assertThat(ex.getMessage()).doesNotContain(API_KEY);
        });
    }

    @Test
    public void testCreateTask_timeoutNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{\"id\":\"late\"}").delay(3000));
            DouBaoSeedanceVideoApi api = new DouBaoSeedanceVideoApi(server.url(), API_KEY, Duration.ofMillis(500));
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
    public void testGetTask_statuses() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"queued\"}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"running\"}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"model\":\"" + MODEL_PRO_FAST + "\",\"status\":\"succeeded\","
                    + "\"content\":{\"video_url\":\"https://ark-content-generation.tos-cn-beijing.volces.com/v.mp4?X-Tos-Signature=x\","
                    + "\"last_frame_url\":\"https://a.com/last.png\"},\"usage\":{\"completion_tokens\":24000},"
                    + "\"seed\":7,\"resolution\":\"480p\",\"ratio\":\"16:9\",\"duration\":2,\"framespersecond\":24}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"succeeded\",\"content\":{}}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"failed\",\"error\":{\"code\":\"OutputVideoSensitiveContentDetected\","
                    + "\"message\":\"sensitive\"}}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"cancelled\"}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"expired\"}");
            server.enqueueJson(200, "{\"id\":\"cgt-1\",\"status\":\"paused\"}");
            DouBaoSeedanceVideoApi api = new DouBaoSeedanceVideoApi(server.url(), API_KEY);

            assertEquals(AiVideoTaskStatus.QUEUED, api.getTask("cgt-1").getStatus());
            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask("cgt-1").getStatus());
            AiVideoTaskResult success = api.getTask("cgt-1");
            assertEquals(AiVideoTaskStatus.SUCCEEDED, success.getStatus());
            assertThat(success.getFirstVideoUrl()).startsWith("https://ark-content-generation");
            assertEquals("https://a.com/last.png", success.getVideos().get(0).getLastFrameUrl());
            assertEquals("24000", success.getMetadata().get("usage.completionTokens"));
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, api.getTask("cgt-1").getErrorCode());
            AiVideoTaskResult failed = api.getTask("cgt-1");
            assertEquals(AiVideoTaskStatus.FAILED, failed.getStatus());
            assertEquals("OutputVideoSensitiveContentDetected", failed.getErrorCode());
            assertEquals(AiVideoTaskStatus.CANCELLED, api.getTask("cgt-1").getStatus());
            assertEquals(AiVideoTaskStatus.EXPIRED, api.getTask("cgt-1").getStatus());
            AiVideoTaskResult unknown = api.getTask("cgt-1");
            assertEquals(AiVideoTaskStatus.UNKNOWN, unknown.getStatus());
            assertFalse(unknown.isFinished());

            assertEquals("GET", server.lastRequest().method());
            assertEquals("/api/v3/contents/generations/tasks/cgt-1", server.lastRequest().path());
            assertThat(server.requests()).noneMatch(r -> "POST".equals(r.method()));
        }
    }

    @Test
    public void testGetTask_notFound() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(404, "{\"error\":{\"code\":\"ResourceNotFound\",\"message\":\"The specified task is not found.\"}}");
            DouBaoSeedanceVideoApi api = new DouBaoSeedanceVideoApi(server.url(), API_KEY);
            assertThatThrownBy(() -> api.getTask("cgt-x")).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.NOT_FOUND, ex.getKind());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
        }
    }

}
