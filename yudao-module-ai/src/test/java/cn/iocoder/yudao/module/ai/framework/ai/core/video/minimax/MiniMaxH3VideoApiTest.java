package cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax;

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
 * {@link MiniMaxH3VideoApi} 的契约测试
 *
 * @author 芋道源码
 */
public class MiniMaxH3VideoApiTest {

    private static final String API_KEY = "test-minimax-key";

    private static MiniMaxH3VideoRequest t2v() {
        return new MiniMaxH3VideoRequest().setModel(MiniMaxH3VideoApi.MODEL_H3_LITE).setPrompt("一只猫")
                .setResolution("480P").setDuration(4).setRatio("16:9");
    }

    // ========== 参数 ==========

    @Test
    public void testBuildRequestBody_t2v() {
        ObjectNode body = MiniMaxH3VideoApi.buildRequestBody(t2v());
        assertEquals("MiniMax-H3-Lite", body.get("model").asText());
        assertEquals("text", body.get("content").get(0).get("type").asText());
        assertEquals("一只猫", body.get("content").get(0).get("text").asText());
        assertEquals("480P", body.get("resolution").asText());
        assertEquals(4, body.get("duration").asInt());
        assertEquals("16:9", body.get("ratio").asText());
        assertFalse(body.has("extra"));
    }

    @Test
    public void testBuildRequestBody_firstLastFrame() {
        MiniMaxH3VideoRequest request = t2v().setRatio("adaptive")
                .setFirstFrameImage("https://a.com/1.png").setLastFrameImage("mm_file://123");
        ObjectNode body = MiniMaxH3VideoApi.buildRequestBody(request);
        JsonNode first = body.get("content").get(1);
        assertEquals("image_url", first.get("type").asText());
        assertEquals("https://a.com/1.png", first.get("image_url").get("url").asText());
        assertEquals("first_frame", first.get("role").asText());
        assertEquals("last_frame", body.get("content").get(2).get("role").asText());
    }

    @Test
    public void testBuildRequestBody_invalid() {
        // 文生必须指定比例，且不能 adaptive
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setRatio(null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setRatio("adaptive")))
                .isInstanceOf(IllegalArgumentException.class);
        // MiniMax-H3 只支持 768P / 2K
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setModel(MiniMaxH3VideoApi.MODEL_H3)))
                .isInstanceOf(IllegalArgumentException.class);
        // Max 最短 5 秒
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setModel(MiniMaxH3VideoApi.MODEL_H3_MAX)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setDuration(16)))
                .isInstanceOf(IllegalArgumentException.class);
        // 提示词扩写只有 Max 支持
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setPromptExpansionMode("quality")))
                .isInstanceOf(IllegalArgumentException.class);
        // 首尾帧与参考素材不能混用
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setFirstFrameImage("https://a.com/1.png")
                .setReferenceImages(ListUtil.toList("https://a.com/2.png")))).isInstanceOf(IllegalArgumentException.class);
        // Lite 参考生不支持 adaptive
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setRatio("adaptive")
                .setReferenceImages(ListUtil.toList("https://a.com/2.png")))).isInstanceOf(IllegalArgumentException.class);
        // 模型不存在：MiniMax-M3 是语言模型
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setModel("MiniMax-M3")))
                .isInstanceOf(IllegalArgumentException.class);
        // 不支持的协议
        assertThatThrownBy(() -> MiniMaxH3VideoApi.buildRequestBody(t2v().setFirstFrameImage("file:///etc/passwd")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBuildRequestBody_maxPromptExpansion() {
        ObjectNode body = MiniMaxH3VideoApi.buildRequestBody(t2v().setModel(MiniMaxH3VideoApi.MODEL_H3_MAX)
                .setDuration(5).setPromptExpansionMode("disabled"));
        assertEquals("disabled", body.get("extra").get("prompt_expansion_mode").asText());
    }

    // ========== 提交 ==========

    @Test
    public void testCreateTask_success() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"task_id\":\"t-123\",\"request_id\":\"r-1\"}");
            MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(server.url() + "/", API_KEY);

            AiVideoTaskRef ref = api.createTask(t2v());

            assertEquals("t-123", ref.getTaskId());
            assertEquals("r-1", ref.getRequestId());
            assertEquals(MiniMaxH3VideoApi.PLATFORM, ref.getPlatform());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertEquals("/v2/video_generation", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            JsonNode body = AiVideoUtils.parseJson(request.body());
            assertEquals("MiniMax-H3-Lite", body.get("model").asText());
        }
    }

    @Test
    public void testCreateTask_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(402, "{\"type\":\"error\",\"error\":{\"type\":\"insufficient_balance_error\","
                    + "\"message\":\"insufficient balance (1008) " + API_KEY + "\",\"http_code\":402},\"request_id\":\"r-2\"}");
            server.enqueueJson(401, "{\"type\":\"error\",\"error\":{\"type\":\"authorized_error\",\"message\":\"invalid api key (2049)\"}}");
            server.enqueueJson(503, "{\"type\":\"error\",\"error\":{\"type\":\"overloaded_error\",\"message\":\"busy\"}}");
            server.enqueueJson(200, "{\"foo\":1}");
            MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(server.url(), API_KEY);

            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.INSUFFICIENT_BALANCE, ex.getKind());
                assertEquals("1008", ex.getCode());
                assertEquals("r-2", ex.getRequestId());
                assertFalse(ex.isSubmitOutcomeUnknown());
                assertThat(ex.getMessage()).doesNotContain(API_KEY);
            });
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.AUTHENTICATION, ex.getKind()));
            // 5xx：结果未知，不重试
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.SERVER_ERROR, ex.getKind());
                assertTrue(ex.isSubmitOutcomeUnknown());
            });
            // 2xx 无 task_id：结果未知
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.INVALID_RESPONSE, ex.getKind());
                assertTrue(ex.isSubmitOutcomeUnknown());
            });
            assertEquals(4, server.requests().size());
        }
    }

    @Test
    public void testCreateTask_timeoutNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{\"task_id\":\"late\"}").delay(3000));
            MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(server.url(), API_KEY, Duration.ofMillis(500));

            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                assertTrue(ex.isSubmitOutcomeUnknown());
            });
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testCreateTask_invalidRequestNotSent() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(server.url(), API_KEY);
            assertThatThrownBy(() -> api.createTask(t2v().setRatio(null))).isInstanceOf(IllegalArgumentException.class);
            assertEquals(0, server.requests().size());
        }
    }

    // ========== 查询 ==========

    @Test
    public void testGetTask_statuses() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"task\":{\"id\":\"t-1\",\"status\":\"queued\"}}");
            server.enqueueJson(200, "{\"id\":\"t-1\",\"status\":\"running\"}");
            server.enqueueJson(200, "{\"task\":{\"id\":\"t-1\",\"status\":\"succeeded\",\"model\":\"MiniMax-H3-Lite\","
                    + "\"duration\":4,\"content\":{\"url\":\"https://cdn.minimax.cn/v.mp4?sign=x\"},"
                    + "\"usage\":{\"output_seconds\":4}}}");
            server.enqueueJson(200, "{\"task\":{\"id\":\"t-1\",\"status\":\"succeeded\",\"content\":{}}}");
            server.enqueueJson(200, "{\"task\":{\"id\":\"t-1\",\"status\":\"failed\",\"error\":{\"code\":\"1026\","
                    + "\"message\":\"sensitive\"}}}");
            server.enqueueJson(200, "{\"task\":{\"id\":\"t-1\",\"status\":\"cancelled\"}}");
            server.enqueueJson(200, "{\"task\":{\"id\":\"t-1\",\"status\":\"something_new\"}}");
            MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(server.url(), API_KEY);

            assertEquals(AiVideoTaskStatus.QUEUED, api.getTask("t-1").getStatus());
            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask("t-1").getStatus());
            AiVideoTaskResult success = api.getTask("t-1");
            assertEquals(AiVideoTaskStatus.SUCCEEDED, success.getStatus());
            assertEquals("https://cdn.minimax.cn/v.mp4?sign=x", success.getFirstVideoUrl());
            assertEquals(4.0, success.getVideos().get(0).getDurationSeconds());
            AiVideoTaskResult noOutput = api.getTask("t-1");
            assertEquals(AiVideoTaskStatus.FAILED, noOutput.getStatus());
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, noOutput.getErrorCode());
            AiVideoTaskResult failed = api.getTask("t-1");
            assertEquals(AiVideoTaskStatus.FAILED, failed.getStatus());
            assertEquals("1026", failed.getErrorCode());
            assertEquals(AiVideoTaskStatus.CANCELLED, api.getTask("t-1").getStatus());
            AiVideoTaskResult unknown = api.getTask("t-1");
            assertEquals(AiVideoTaskStatus.UNKNOWN, unknown.getStatus());
            assertFalse(unknown.isFinished());

            VideoMockServer.Request request = server.lastRequest();
            assertEquals("GET", request.method());
            assertEquals("/v2/query/video_generation/t-1", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            // 查询不会提交任务
            assertThat(server.requests()).noneMatch(r -> "POST".equals(r.method()));
        }
    }

    @Test
    public void testGetTask_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(400, "{\"type\":\"error\",\"error\":{\"type\":\"invalid_request_error\","
                    + "\"message\":\"task does not exist (2013)\"}}");
            server.enqueueJson(500, "not json");
            MiniMaxH3VideoApi api = new MiniMaxH3VideoApi(server.url(), API_KEY);

            assertThatThrownBy(() -> api.getTask("t-404")).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.NOT_FOUND, ex.getKind());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
            assertThatThrownBy(() -> api.getTask("t-500")).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.SERVER_ERROR, ex.getKind()));
            // 非法任务编号不发请求
            assertThatThrownBy(() -> api.getTask("../x")).isInstanceOf(IllegalArgumentException.class);
            assertEquals(2, server.requests().size());
        }
    }

    @Test
    public void testConstructor_requiresKey() {
        assertThatThrownBy(() -> new MiniMaxH3VideoApi(null, " ")).isInstanceOf(IllegalArgumentException.class);
        assertTrue(MiniMaxH3VideoApi.getResolutions("MiniMax-M3").isEmpty());
    }

}
