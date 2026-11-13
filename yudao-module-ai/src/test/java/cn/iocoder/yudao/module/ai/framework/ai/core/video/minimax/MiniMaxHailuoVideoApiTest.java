package cn.iocoder.yudao.module.ai.framework.ai.core.video.minimax;

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
 * {@link MiniMaxHailuoVideoApi}（v1 旧版协议）的契约测试
 *
 * @author 芋道源码
 */
public class MiniMaxHailuoVideoApiTest {

    private static final String API_KEY = "test-minimax-key";

    private static MiniMaxHailuoVideoRequest t2v() {
        return new MiniMaxHailuoVideoRequest().setModel("MiniMax-Hailuo-02").setPrompt("一只猫").setDuration(6).setResolution("768P");
    }

    // ========== 模型差异 ==========

    @Test
    public void testBuildRequestBody_modes() {
        assertEquals(MiniMaxHailuoVideoApi.Mode.T2V, MiniMaxHailuoVideoApi.getMode(t2v()));
        ObjectNode body = MiniMaxHailuoVideoApi.buildRequestBody(t2v());
        assertEquals("MiniMax-Hailuo-02", body.get("model").asText());
        assertEquals(6, body.get("duration").asInt());
        assertEquals("768P", body.get("resolution").asText());

        // 02 首尾帧
        ObjectNode fl2v = MiniMaxHailuoVideoApi.buildRequestBody(t2v().setFirstFrameImage("https://a.com/1.png")
                .setLastFrameImage("https://a.com/2.png"));
        assertEquals("https://a.com/1.png", fl2v.get("first_frame_image").asText());
        assertEquals("https://a.com/2.png", fl2v.get("last_frame_image").asText());

        // 02 图生 512P
        MiniMaxHailuoVideoApi.buildRequestBody(t2v().setFirstFrameImage("https://a.com/1.png").setResolution("512P"));

        // S2V-01 主体参考
        ObjectNode s2v = MiniMaxHailuoVideoApi.buildRequestBody(new MiniMaxHailuoVideoRequest().setModel("S2V-01")
                .setPrompt("跳舞").setSubjectReferenceImage("https://a.com/face.png"));
        JsonNode subject = s2v.get("subject_reference").get(0);
        assertEquals("character", subject.get("type").asText());
        assertEquals("https://a.com/face.png", subject.get("image").get(0).asText());
        assertFalse(s2v.has("duration"));
    }

    @Test
    public void testBuildRequestBody_fastRejectsTextToVideo() {
        // MiniMax-Hailuo-2.3-Fast 只支持图生视频，不能当作文生视频
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("MiniMax-Hailuo-2.3-Fast")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("文生视频");
        ObjectNode body = MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("MiniMax-Hailuo-2.3-Fast")
                .setFirstFrameImage("https://a.com/1.png").setFastPretreatment(true));
        assertTrue(body.get("fast_pretreatment").asBoolean());
    }

    @Test
    public void testBuildRequestBody_invalidCombinations() {
        // 2.3 不支持 10 秒 1080P
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("MiniMax-Hailuo-2.3")
                .setDuration(10).setResolution("1080P"))).isInstanceOf(IllegalArgumentException.class);
        // 2.3 不支持首尾帧
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("MiniMax-Hailuo-2.3")
                .setFirstFrameImage("https://a.com/1.png").setLastFrameImage("https://a.com/2.png")))
                .isInstanceOf(IllegalArgumentException.class);
        // 02 文生不支持 512P
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setResolution("512P")))
                .isInstanceOf(IllegalArgumentException.class);
        // T2V-01 只支持 6 秒
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("T2V-01").setResolution(null)
                .setDuration(10))).isInstanceOf(IllegalArgumentException.class);
        // I2V-01 不支持文生
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("I2V-01").setResolution(null)))
                .isInstanceOf(IllegalArgumentException.class);
        // 旧模型不支持 fast_pretreatment
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("T2V-01").setResolution(null)
                .setFastPretreatment(true))).isInstanceOf(IllegalArgumentException.class);
        // S2V-01 不能指定时长
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(new MiniMaxHailuoVideoRequest().setModel("S2V-01")
                .setPrompt("x").setSubjectReferenceImage("https://a.com/face.png").setDuration(6)))
                .isInstanceOf(IllegalArgumentException.class);
        // 文生必须有提示词
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setPrompt(null)))
                .isInstanceOf(IllegalArgumentException.class);
        // 未知模型
        assertThatThrownBy(() -> MiniMaxHailuoVideoApi.buildRequestBody(t2v().setModel("MiniMax-H3")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 提交 ==========

    @Test
    public void testCreateTask() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"task_id\":\"106916112212032\",\"base_resp\":{\"status_code\":0,\"status_msg\":\"success\"}}");
            server.enqueueJson(200, "{\"task_id\":\"\",\"base_resp\":{\"status_code\":1008,\"status_msg\":\"insufficient balance\"}}");
            server.enqueueJson(200, "{\"base_resp\":{\"status_code\":2049,\"status_msg\":\"invalid api key\"}}");
            server.enqueueJson(200, "{\"base_resp\":{\"status_code\":1026,\"status_msg\":\"input new_sensitive\"}}");
            server.enqueueJson(502, "<html>bad gateway</html>");
            MiniMaxHailuoVideoApi api = new MiniMaxHailuoVideoApi(server.url(), API_KEY);

            AiVideoTaskRef ref = api.createTask(t2v());
            assertEquals("106916112212032", ref.getTaskId());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("/v1/video_generation", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertEquals("一只猫", AiVideoUtils.parseJson(request.body()).get("prompt").asText());

            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.INSUFFICIENT_BALANCE, ex.getKind());
                assertEquals("1008", ex.getCode());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.AUTHENTICATION, ex.getKind()));
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.CONTENT_REJECTED, ex.getKind()));
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.SERVER_ERROR, ex.getKind());
                assertTrue(ex.isSubmitOutcomeUnknown());
            });
            assertEquals(5, server.requests().size());
        }
    }

    @Test
    public void testCreateTask_timeoutNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            MiniMaxHailuoVideoApi api = new MiniMaxHailuoVideoApi(server.url(), API_KEY, Duration.ofMillis(500));
            assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertTrue(ex.isSubmitOutcomeUnknown()));
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    // ========== 查询 ==========

    @Test
    public void testGetTask_successRetrievesFile() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"task_id\":\"1\",\"status\":\"Queueing\",\"base_resp\":{\"status_code\":0}}");
            server.enqueueJson(200, "{\"task_id\":\"1\",\"status\":\"Processing\",\"base_resp\":{\"status_code\":0}}");
            server.enqueueJson(200, "{\"task_id\":\"1\",\"status\":\"Success\",\"file_id\":\"205258526306433\","
                    + "\"video_width\":1366,\"video_height\":768,\"base_resp\":{\"status_code\":0}}");
            server.enqueueJson(200, "{\"file\":{\"file_id\":205258526306433,\"download_url\":\"https://cdn.minimax.cn/v.mp4?Expires=1\"},"
                    + "\"base_resp\":{\"status_code\":0}}");
            MiniMaxHailuoVideoApi api = new MiniMaxHailuoVideoApi(server.url(), API_KEY);

            assertEquals(AiVideoTaskStatus.QUEUED, api.getTask("1").getStatus());
            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask("1").getStatus());
            AiVideoTaskResult result = api.getTask("1");
            assertEquals(AiVideoTaskStatus.SUCCEEDED, result.getStatus());
            assertEquals("https://cdn.minimax.cn/v.mp4?Expires=1", result.getFirstVideoUrl());
            assertEquals(1366, result.getVideos().get(0).getWidth());

            assertEquals("/v1/query/video_generation", server.requests().get(0).path());
            assertEquals("task_id=1", server.requests().get(0).query());
            assertEquals("/v1/files/retrieve", server.lastRequest().path());
            assertEquals("file_id=205258526306433", server.lastRequest().query());
            assertThat(server.requests()).allMatch(r -> "GET".equals(r.method()));
        }
    }

    @Test
    public void testGetTask_failedAndUnknown() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"task_id\":\"1\",\"status\":\"Fail\",\"base_resp\":{\"status_code\":1027,"
                    + "\"status_msg\":\"output new_sensitive\"}}");
            server.enqueueJson(200, "{\"task_id\":\"1\",\"status\":\"Success\",\"file_id\":\"\",\"base_resp\":{\"status_code\":0}}");
            server.enqueueJson(200, "{\"task_id\":\"1\",\"status\":\"NewStatus\",\"base_resp\":{\"status_code\":0}}");
            server.enqueueJson(200, "{\"task_id\":\"\",\"status\":\"\",\"base_resp\":{\"status_code\":2013,"
                    + "\"status_msg\":\"task does not exist\"}}");
            MiniMaxHailuoVideoApi api = new MiniMaxHailuoVideoApi(server.url(), API_KEY);

            AiVideoTaskResult failed = api.getTask("1");
            assertEquals(AiVideoTaskStatus.FAILED, failed.getStatus());
            assertEquals("1027", failed.getErrorCode());
            // Success 却没有 file_id：不能当作成功
            AiVideoTaskResult noFile = api.getTask("1");
            assertEquals(AiVideoTaskStatus.FAILED, noFile.getStatus());
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, noFile.getErrorCode());
            assertEquals(AiVideoTaskStatus.UNKNOWN, api.getTask("1").getStatus());
            assertThatThrownBy(() -> api.getTask("2")).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.NOT_FOUND, ex.getKind()));
        }
    }

}
