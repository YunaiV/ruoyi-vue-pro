package cn.iocoder.yudao.module.ai.framework.ai.core.video.veo;

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
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VertexVeoVideoApi}（Vertex AI 正式协议）的契约测试
 *
 * @author 芋道源码
 */
public class VertexVeoVideoApiTest {

    private static final String TOKEN = "ya29.test-access-token";
    private static final String PROJECT = "my-project";
    private static final String OPERATION = "projects/my-project/locations/us-central1/publishers/google/models/"
            + "veo-3.1-fast-generate-001/operations/a1b2c3d4-5678";

    private static VertexVeoVideoRequest t2v() {
        return new VertexVeoVideoRequest().setModel(VertexVeoVideoApi.MODEL_3_1_FAST).setPrompt("a cat")
                .setAspectRatio("16:9").setDurationSeconds(4).setResolution("720p").setGenerateAudio(false).setSampleCount(1)
                .setStorageUri("gs://bucket/out/");
    }

    private static VertexVeoVideoApi api(VideoMockServer server, Duration timeout) {
        return new VertexVeoVideoApi(PROJECT, "us-central1", () -> TOKEN, server.url(), timeout, 0);
    }

    // ========== 参数 ==========

    @Test
    public void testBuildRequestBody() {
        ObjectNode body = VertexVeoVideoApi.buildRequestBody(t2v());
        JsonNode instance = body.get("instances").get(0);
        assertEquals("a cat", instance.get("prompt").asText());
        JsonNode parameters = body.get("parameters");
        assertEquals("16:9", parameters.get("aspectRatio").asText());
        assertEquals(4, parameters.get("durationSeconds").asInt());
        assertEquals("720p", parameters.get("resolution").asText());
        assertFalse(parameters.get("generateAudio").asBoolean());
        assertEquals("gs://bucket/out/", parameters.get("storageUri").asText());

        ObjectNode i2v = VertexVeoVideoApi.buildRequestBody(t2v()
                .setImage(new VertexVeoVideoRequest.Image().setBytesBase64Encoded("iVBORw0KGgo=").setMimeType("image/png"))
                .setLastFrame(new VertexVeoVideoRequest.Image().setGcsUri("gs://bucket/last.jpg").setMimeType("image/jpeg")));
        JsonNode image = i2v.get("instances").get(0).get("image");
        assertEquals("iVBORw0KGgo=", image.get("bytesBase64Encoded").asText());
        assertEquals("image/png", image.get("mimeType").asText());
        assertEquals("gs://bucket/last.jpg", i2v.get("instances").get(0).get("lastFrame").get("gcsUri").asText());

        ObjectNode r2v = VertexVeoVideoApi.buildRequestBody(t2v().setDurationSeconds(8).setReferenceImages(ListUtil.toList(
                new VertexVeoVideoRequest.Image().setGcsUri("gs://bucket/ref.png").setMimeType("image/png"))));
        JsonNode reference = r2v.get("instances").get(0).get("referenceImages").get(0);
        assertEquals("asset", reference.get("referenceType").asText());
    }

    @Test
    public void testBuildRequestBody_invalid() {
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setDurationSeconds(5)))
                .isInstanceOf(IllegalArgumentException.class);
        // 4k 只有 veo-3.1-generate-001
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setResolution("4k")))
                .isInstanceOf(IllegalArgumentException.class);
        VertexVeoVideoApi.buildRequestBody(t2v().setModel(VertexVeoVideoApi.MODEL_3_1).setResolution("4k"));
        // 参考图只支持 8 秒，Lite 不支持参考图
        VertexVeoVideoRequest.Image ref = new VertexVeoVideoRequest.Image().setGcsUri("gs://b/r.png").setMimeType("image/png");
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setReferenceImages(ListUtil.toList(ref))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setModel(VertexVeoVideoApi.MODEL_3_1_LITE)
                .setDurationSeconds(8).setReferenceImages(ListUtil.toList(ref)))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setDurationSeconds(8)
                .setReferenceImages(ListUtil.toList(ref, ref, ref, ref)))).isInstanceOf(IllegalArgumentException.class);
        // 图片 bytes / gcsUri 二选一，gcsUri 必须 gs://
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setImage(new VertexVeoVideoRequest.Image()
                .setGcsUri("https://a.com/1.png").setMimeType("image/png")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setImage(new VertexVeoVideoRequest.Image()
                .setMimeType("image/png")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setStorageUri("https://a.com/")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setAspectRatio("1:1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VertexVeoVideoApi.buildRequestBody(t2v().setModel("gemini-veo")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConstructor() {
        assertEquals("https://us-central1-aiplatform.googleapis.com", VertexVeoVideoApi.defaultBaseUrl("us-central1"));
        assertEquals("https://aiplatform.googleapis.com", VertexVeoVideoApi.defaultBaseUrl("global"));
        assertThatThrownBy(() -> new VertexVeoVideoApi("Bad/Project", null, () -> TOKEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VertexVeoVideoApi(PROJECT, null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 提交 ==========

    @Test
    public void testCreateTask() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"name\":\"" + OPERATION + "\"}");
            AiVideoTaskRef ref = api(server, null).createTask(t2v());

            assertEquals(OPERATION, ref.getTaskId());
            assertEquals(VertexVeoVideoApi.MODEL_3_1_FAST, ref.getModel());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertEquals("/v1/projects/my-project/locations/us-central1/publishers/google/models/veo-3.1-fast-generate-001"
                    + ":predictLongRunning", request.path());
            assertEquals("Bearer " + TOKEN, request.header("Authorization"));
            assertEquals("a cat", AiVideoUtils.parseJson(request.body()).get("instances").get(0).get("prompt").asText());
        }
    }

    @Test
    public void testCreateTask_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(401, "{\"error\":{\"code\":401,\"message\":\"Request had invalid authentication credentials "
                    + TOKEN + "\",\"status\":\"UNAUTHENTICATED\"}}");
            server.enqueueJson(403, "{\"error\":{\"code\":403,\"message\":\"Billing\",\"status\":\"PERMISSION_DENIED\","
                    + "\"details\":[{\"@type\":\"type.googleapis.com/google.rpc.ErrorInfo\",\"reason\":\"BILLING_DISABLED\"}]}}");
            server.enqueueJson(403, "{\"error\":{\"code\":403,\"message\":\"API disabled\",\"status\":\"PERMISSION_DENIED\","
                    + "\"details\":[{\"reason\":\"SERVICE_DISABLED\"}]}}");
            server.enqueueJson(429, "{\"error\":{\"code\":429,\"message\":\"quota\",\"status\":\"RESOURCE_EXHAUSTED\"}}");
            server.enqueueJson(503, "{\"error\":{\"code\":503,\"message\":\"unavailable\",\"status\":\"UNAVAILABLE\"}}");
            server.enqueueJson(200, "{}");
            VertexVeoVideoApi api = api(server, null);

            assertKind(api, AiVideoException.Kind.AUTHENTICATION, false);
            assertKind(api, AiVideoException.Kind.INSUFFICIENT_BALANCE, false);
            assertKind(api, AiVideoException.Kind.PERMISSION, false);
            assertKind(api, AiVideoException.Kind.RATE_LIMITED, false);
            assertKind(api, AiVideoException.Kind.SERVER_ERROR, true);
            assertKind(api, AiVideoException.Kind.INVALID_RESPONSE, true);
            assertEquals(6, server.requests().size());
        }
    }

    private static void assertKind(VertexVeoVideoApi api, AiVideoException.Kind kind, boolean unknown) {
        assertThatThrownBy(() -> api.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
            assertEquals(kind, ex.getKind());
            assertEquals(unknown, ex.isSubmitOutcomeUnknown());
            assertThat(ex.getMessage()).doesNotContain("test-access-token");
        });
    }

    @Test
    public void testCreateTask_timeoutAndEmptyToken() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            assertThatThrownBy(() -> api(server, Duration.ofMillis(500)).createTask(t2v()))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                    });
            Thread.sleep(300);
            assertEquals(1, server.requests().size());

            VertexVeoVideoApi noToken = new VertexVeoVideoApi(PROJECT, null, () -> "", server.url(), null, 0);
            assertThatThrownBy(() -> noToken.createTask(t2v())).isInstanceOfSatisfying(AiVideoException.class, ex -> {
                assertEquals(AiVideoException.Kind.AUTHENTICATION, ex.getKind());
                assertFalse(ex.isSubmitOutcomeUnknown());
            });
            assertEquals(1, server.requests().size());
        }
    }

    // ========== 查询 ==========

    @Test
    public void testGetTask() throws Exception {
        byte[] mp4 = new byte[]{0, 0, 0, 0x18, 'f', 't', 'y', 'p'};
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"name\":\"" + OPERATION + "\"}");
            server.enqueueJson(200, "{\"name\":\"" + OPERATION + "\",\"done\":true,\"response\":{\"@type\":"
                    + "\"type.googleapis.com/cloud.ai.large_models.vision.GenerateVideoResponse\",\"raiMediaFilteredCount\":0,"
                    + "\"videos\":[{\"gcsUri\":\"gs://bucket/out/sample_0.mp4\",\"mimeType\":\"video/mp4\"}]}}");
            server.enqueueJson(200, "{\"done\":true,\"response\":{\"videos\":[{\"bytesBase64Encoded\":\""
                    + Base64.getEncoder().encodeToString(mp4) + "\",\"mimeType\":\"video/mp4\"}]}}");
            server.enqueueJson(200, "{\"done\":true,\"response\":{\"raiMediaFilteredCount\":1,"
                    + "\"raiMediaFilteredReasons\":[\"Unable to generate videos because of safety\"]}}");
            server.enqueueJson(200, "{\"done\":true,\"response\":{}}");
            server.enqueueJson(200, "{\"done\":true,\"error\":{\"code\":3,\"message\":\"bad prompt\",\"status\":\"INVALID_ARGUMENT\"}}");
            server.enqueueJson(404, "{\"error\":{\"code\":404,\"message\":\"not found\",\"status\":\"NOT_FOUND\"}}");
            VertexVeoVideoApi api = api(server, null);
            AiVideoTaskRef ref = new AiVideoTaskRef(VertexVeoVideoApi.PLATFORM, VertexVeoVideoApi.MODEL_3_1_FAST, OPERATION, null, null);

            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask(ref).getStatus());
            AiVideoTaskResult gcs = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.SUCCEEDED, gcs.getStatus());
            assertEquals("gs://bucket/out/sample_0.mp4", gcs.getVideos().get(0).getGcsUri());
            AiVideoTaskResult inline = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.SUCCEEDED, inline.getStatus());
            assertArrayEquals(mp4, inline.getVideos().get(0).getData());
            AiVideoTaskResult filtered = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.FAILED, filtered.getStatus());
            assertEquals(AiVideoTaskResult.ERROR_CODE_FILTERED, filtered.getErrorCode());
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, api.getTask(ref).getErrorCode());
            AiVideoTaskResult failed = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.FAILED, failed.getStatus());
            assertEquals("INVALID_ARGUMENT", failed.getErrorCode());
            assertThatThrownBy(() -> api.getTask(ref)).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.NOT_FOUND, ex.getKind()));

            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertThat(request.path()).endsWith("/models/veo-3.1-fast-generate-001:fetchPredictOperation");
            assertEquals(OPERATION, AiVideoUtils.parseJson(request.body()).get("operationName").asText());
            // 查询只调用 fetchPredictOperation，不会重新提交
            assertThat(server.requests()).noneMatch(r -> r.path().endsWith(":predictLongRunning"));
        }
    }

    @Test
    public void testGetTask_operationMismatch() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            VertexVeoVideoApi api = api(server, null);
            // 模型不一致
            assertThatThrownBy(() -> api.getTask(new AiVideoTaskRef(VertexVeoVideoApi.PLATFORM, VertexVeoVideoApi.MODEL_3_1,
                    OPERATION, null, null))).isInstanceOf(IllegalArgumentException.class);
            // 非法的 operation name
            assertThatThrownBy(() -> api.getTask(new AiVideoTaskRef(VertexVeoVideoApi.PLATFORM, VertexVeoVideoApi.MODEL_3_1_FAST,
                    "../../evil", null, null))).isInstanceOf(IllegalArgumentException.class);
            assertEquals(0, server.requests().size());
        }
    }

}
