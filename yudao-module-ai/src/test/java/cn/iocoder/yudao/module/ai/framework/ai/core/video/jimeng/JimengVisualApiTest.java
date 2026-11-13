package cn.iocoder.yudao.module.ai.framework.ai.core.video.jimeng;

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

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JimengVisualApi} 的契约测试（AK/SK 签名复用官方 SDK 的 SignerV4Impl）
 *
 * @author 芋道源码
 */
public class JimengVisualApiTest {

    private static final String AK = "AKLTtestAccessKeyId";
    private static final String SK = "TestSecretAccessKey==";
    private static final String API_KEY = "test-jimeng-api-key";
    private static final Instant NOW = Instant.parse("2025-11-13T08:00:00Z");

    // ========== 签名 ==========

    /**
     * 固定向量：与上一版手写签名器、官方 SDK（volc-sdk-java SignerV4Impl）对同一请求的计算结果一致，
     * 防止 SDK 升级或参数拼装变化导致签名改变
     */
    @Test
    public void testSign_officialSdkVector() {
        byte[] body = "{\"req_key\":\"jimeng_t2v_v30\",\"prompt\":\"a cat\"}".getBytes(StandardCharsets.UTF_8);

        Map<String, String> headers = JimengVisualApi.sign(AK, SK, "visual.volcengineapi.com", "/",
                "CVSync2AsyncSubmitTask", body, NOW);

        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("20251113T080000Z", headers.get("X-Date"));
        assertEquals("2f281458116057487058e763801dcbee397c7b649f1cf0ab06a3f7d7a8143edb", headers.get("X-Content-Sha256"));
        assertEquals("HMAC-SHA256 Credential=AKLTtestAccessKeyId/20251113/cn-north-1/cv/request, "
                        + "SignedHeaders=content-type;host;x-content-sha256;x-date, "
                        + "Signature=f457b5e2cbe6e471bc9aba21d33135e2aa8131d31e6f783758e7006f408098a8",
                headers.get("Authorization"));
        assertThat(headers.toString()).doesNotContain(SK);
    }

    // ========== 视频生成参数 ==========

    @Test
    public void testBuildVideoRequestBody() {
        ObjectNode t2v = JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30)
                .setPrompt("一只猫").setFrames(121).setAspectRatio("16:9").setSeed(-1L));
        assertEquals("jimeng_t2v_v30", t2v.get("req_key").asText());
        assertEquals(121, t2v.get("frames").asInt());
        assertEquals("16:9", t2v.get("aspect_ratio").asText());

        ObjectNode firstTail = JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_I2V_FIRST_TAIL_V30).setPrompt("变化")
                .setImageUrls(ListUtil.toList("https://a.com/1.png", "https://a.com/2.png")));
        assertEquals(2, firstTail.get("image_urls").size());

        ObjectNode pro = JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_TI2V_V30_PRO)
                .setBinaryDataBase64(ListUtil.toList("iVBORw0KGgo=")));
        assertEquals("iVBORw0KGgo=", pro.get("binary_data_base64").get(0).asText());
    }

    @Test
    public void testBuildVideoRequestBody_invalid() {
        // 文生不能带图；图生不能用 aspect_ratio；首尾帧需要 2 张
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30)
                .setPrompt("x").setImageUrls(ListUtil.toList("https://a.com/1.png")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_I2V_FIRST_V30).setPrompt("x").setAspectRatio("16:9")
                .setImageUrls(ListUtil.toList("https://a.com/1.png")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_I2V_FIRST_TAIL_V30).setPrompt("x")
                .setImageUrls(ListUtil.toList("https://a.com/1.png")))).isInstanceOf(IllegalArgumentException.class);
        // 3.0 Pro 图生不能用 aspect_ratio
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_TI2V_V30_PRO).setAspectRatio("16:9")
                .setImageUrls(ListUtil.toList("https://a.com/1.png")))).isInstanceOf(IllegalArgumentException.class);
        // 帧数只有 121 / 241；url 与 base64 二选一；base64 不带 data: 前缀
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30)
                .setPrompt("x").setFrames(100))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_TI2V_V30_PRO).setImageUrls(ListUtil.toList("https://a.com/1.png"))
                .setBinaryDataBase64(ListUtil.toList("AAAA")))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_TI2V_V30_PRO).setBinaryDataBase64(ListUtil.toList("data:image/png;base64,AAAA"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildVideoRequestBody(new JimengVideoRequest().setReqKey("jimeng_unknown")
                .setPrompt("x"))).isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 数字人、动作模仿参数 ==========

    @Test
    public void testBuildOmniHumanRequestBody() {
        ObjectNode v15 = JimengVisualApi.buildOmniHumanRequestBody(new JimengOmniHumanRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V15).setImageUrl("https://a.com/p.png").setAudioUrl("https://a.com/a.mp3")
                .setMaskUrls(ListUtil.toList("https://a.com/m.png")).setOutputResolution(720).setPeFastMode(true).setSeed(1L));
        assertEquals("https://a.com/p.png", v15.get("image_url").asText());
        assertEquals("https://a.com/m.png", v15.get("mask_url").get(0).asText());
        assertEquals(720, v15.get("output_resolution").asInt());
        assertTrue(v15.get("pe_fast_mode").asBoolean());

        ObjectNode v20 = JimengVisualApi.buildOmniHumanRequestBody(new JimengOmniHumanRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V20).setImageUrl("https://a.com/p.png").setAudioUrl("https://a.com/a.mp3")
                .setEnableWatermark(false));
        assertEquals("https://a.com/p.png", v20.get("image_urls").get(0).asText());
        assertFalse(v20.has("image_url"));
        assertFalse(v20.get("enable_watermark").asBoolean());

        assertThatThrownBy(() -> JimengVisualApi.buildOmniHumanRequestBody(new JimengOmniHumanRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V20).setImageUrl("https://a.com/p.png").setAudioUrl("https://a.com/a.mp3")
                .setOutputResolution(1080))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildOmniHumanRequestBody(new JimengOmniHumanRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V15).setImageUrl("https://a.com/p.png").setAudioUrl("https://a.com/a.mp3")
                .setOutputResolution(480))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildOmniHumanRequestBody(new JimengOmniHumanRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V15).setImageUrl("https://a.com/p.png")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBuildDreamActorRequestBody() {
        ObjectNode m1 = JimengVisualApi.buildDreamActorRequestBody(new JimengDreamActorRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_DREAM_ACTOR_M1).setImageUrl("https://a.com/p.png").setVideoUrl("https://a.com/m.mp4"));
        assertEquals("https://a.com/p.png", m1.get("image_url").asText());
        assertEquals("https://a.com/m.mp4", m1.get("video_url").asText());

        ObjectNode m20 = JimengVisualApi.buildDreamActorRequestBody(new JimengDreamActorRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_DREAM_ACTOR_M20).setImageUrl("https://a.com/p.png").setVideoUrl("https://a.com/m.mp4")
                .setCutResultFirstSecondSwitch(true));
        assertEquals("https://a.com/p.png", m20.get("image_urls").get(0).asText());
        assertTrue(m20.get("cut_result_first_second_switch").asBoolean());

        assertThatThrownBy(() -> JimengVisualApi.buildDreamActorRequestBody(new JimengDreamActorRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_DREAM_ACTOR_M1).setImageUrl("https://a.com/p.png").setVideoUrl("https://a.com/m.mp4")
                .setCutResultFirstSecondSwitch(true))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JimengVisualApi.buildDreamActorRequestBody(new JimengDreamActorRequest()
                .setReqKey(JimengVisualApi.REQ_KEY_DREAM_ACTOR_M20).setImageUrl("https://a.com/p.png")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ========== 提交：AK/SK 签名 ==========

    @Test
    public void testSubmitVideo_signed() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"task_id\":\"7392616336519610409\"},\"message\":\"Success\","
                    + "\"request_id\":\"r-1\",\"status\":10000}");
            JimengVisualApi api = JimengVisualApi.ofAccessKey(AK, SK, server.url(), null, Clock.fixed(NOW, ZoneOffset.UTC));

            AiVideoTaskRef ref = api.submitVideo(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30).setPrompt("a cat"));

            assertEquals("7392616336519610409", ref.getTaskId());
            assertEquals(JimengVisualApi.REQ_KEY_T2V_V30, ref.getModel());
            assertEquals(JimengVisualApi.ACTION_SYNC2ASYNC_GET_RESULT, ref.getRoute());
            assertEquals("r-1", ref.getRequestId());
            VideoMockServer.Request request = server.lastRequest();
            assertEquals("POST", request.method());
            assertEquals("/", request.path());
            assertEquals("Action=CVSync2AsyncSubmitTask&Version=2022-08-31", request.query());
            assertEquals(server.hostAndPort(), request.header("Host"));
            // 用收到的请求（Host 含端口、实际请求体）重新计算签名
            Map<String, String> expected = JimengVisualApi.sign(AK, SK, server.hostAndPort(), "/",
                    "CVSync2AsyncSubmitTask", request.body(), NOW);
            assertEquals(expected.get("Authorization"), request.header("Authorization"));
            assertEquals(expected.get("X-Date"), request.header("X-Date"));
            assertEquals(expected.get("X-Content-Sha256"), request.header("X-Content-Sha256"));
            assertEquals("{\"req_key\":\"jimeng_t2v_v30\",\"prompt\":\"a cat\"}", request.bodyAsString());
        }
    }

    @Test
    public void testSubmit_apiKeyPaths() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"task_id\":\"1\"},\"request_id\":\"r\"}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"task_id\":\"2\"},\"request_id\":\"r\"}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"task_id\":\"3\"},\"request_id\":\"r\"}");
            JimengVisualApi api = JimengVisualApi.ofApiKey(API_KEY, server.url(), null);

            AiVideoTaskRef omni15 = api.submitOmniHuman(new JimengOmniHumanRequest().setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V15)
                    .setImageUrl("https://a.com/p.png").setAudioUrl("https://a.com/a.mp3"));
            assertEquals(JimengVisualApi.ACTION_GET_RESULT, omni15.getRoute());
            assertEquals("/api/common/v3/submit_task", server.lastRequest().path());
            assertEquals("Bearer " + API_KEY, server.lastRequest().header("Authorization"));

            AiVideoTaskRef omni20 = api.submitOmniHuman(new JimengOmniHumanRequest().setReqKey(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V20)
                    .setImageUrl("https://a.com/p.png").setAudioUrl("https://a.com/a.mp3"));
            assertEquals(JimengVisualApi.ACTION_SYNC2ASYNC_GET_RESULT, omni20.getRoute());
            assertEquals("/api/common/v3/submit_task_v2", server.lastRequest().path());

            AiVideoTaskRef actor = api.submitDreamActor(new JimengDreamActorRequest().setReqKey(JimengVisualApi.REQ_KEY_DREAM_ACTOR_M20)
                    .setImageUrl("https://a.com/p.png").setVideoUrl("https://a.com/m.mp4"));
            assertEquals("3", actor.getTaskId());
            assertEquals("/api/common/v3/submit_task_v2", server.lastRequest().path());
            assertEquals("jimeng_dreamactor_m20_gen_video",
                    AiVideoUtils.parseJson(server.lastRequest().body()).get("req_key").asText());
        }
    }

    @Test
    public void testSubmit_errors() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(401, "{\"code\":50400,\"message\":\"apikey does not exist " + API_KEY + "\",\"request_id\":\"r\"}");
            server.enqueueJson(200, "{\"code\":50412,\"message\":\"Text Risk Not Pass\",\"request_id\":\"r\"}");
            server.enqueueJson(200, "{\"code\":50429,\"message\":\"Request Has Reached API Limit, Please Try Later\"}");
            server.enqueueJson(500, "{\"code\":50500,\"message\":\"Internal Error\"}");
            server.enqueueJson(401, "{\"ResponseMetadata\":{\"RequestId\":\"r-gw\",\"Action\":\"CVSync2AsyncSubmitTask\","
                    + "\"Error\":{\"Code\":\"SignatureDoesNotMatch\",\"Message\":\"The request signature we calculated does not match\"}}}");
            server.enqueueJson(403, "{\"ResponseMetadata\":{\"Error\":{\"Code\":\"AccessDenied\",\"Message\":\"not open\"}}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{}}");
            server.enqueueJson(200, "not json");
            JimengVisualApi api = JimengVisualApi.ofApiKey(API_KEY, server.url(), null);

            assertKind(api, AiVideoException.Kind.AUTHENTICATION, false);
            assertKind(api, AiVideoException.Kind.CONTENT_REJECTED, false);
            assertKind(api, AiVideoException.Kind.RATE_LIMITED, false);
            assertKind(api, AiVideoException.Kind.SERVER_ERROR, true);
            assertKind(api, AiVideoException.Kind.AUTHENTICATION, false);
            assertKind(api, AiVideoException.Kind.PERMISSION, false);
            assertKind(api, AiVideoException.Kind.INVALID_RESPONSE, true);
            assertKind(api, AiVideoException.Kind.INVALID_RESPONSE, true);
            assertEquals(8, server.requests().size());
        }
    }

    private static void assertKind(JimengVisualApi api, AiVideoException.Kind kind, boolean unknown) {
        assertThatThrownBy(() -> api.submitVideo(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30).setPrompt("x")))
                .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                    assertEquals(kind, ex.getKind());
                    assertEquals(unknown, ex.isSubmitOutcomeUnknown());
                    assertThat(ex.getMessage()).doesNotContain(API_KEY);
                });
    }

    @Test
    public void testSubmit_timeoutNotResubmitted() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueue(VideoMockServer.Response.json(200, "{}").delay(3000));
            JimengVisualApi api = JimengVisualApi.ofApiKey(API_KEY, server.url(), Duration.ofMillis(500));
            assertThatThrownBy(() -> api.submitVideo(new JimengVideoRequest().setReqKey(JimengVisualApi.REQ_KEY_T2V_V30).setPrompt("x")))
                    .isInstanceOfSatisfying(AiVideoException.class, ex -> {
                        assertEquals(AiVideoException.Kind.TIMEOUT, ex.getKind());
                        assertTrue(ex.isSubmitOutcomeUnknown());
                    });
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    // ========== 查询 ==========

    @Test
    public void testGetTask() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"in_queue\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"generating\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"request_id\":\"r\",\"data\":{\"status\":\"done\",\"aigc_meta_tagged\":true,"
                    + "\"video_url\":\"https://v.volcvideo.com/v.mp4?x-signature=abc\","
                    + "\"resp_data\":\"{\\\"video_meta\\\":{\\\"Duration\\\":5.04}}\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"done\",\"video_url\":\"\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"expired\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"not_found\"}}");
            server.enqueueJson(200, "{\"code\":50215,\"data\":null,\"message\":\"Input invalid for this service.\",\"status\":50215}");
            server.enqueueJson(200, "{\"code\":50516,\"data\":null,\"message\":\"Post Video Risk Not Pass\"}");
            server.enqueueJson(200, "{\"code\":50429,\"message\":\"Request Has Reached API Limit\"}");
            JimengVisualApi api = JimengVisualApi.ofApiKey(API_KEY, server.url(), null);
            AiVideoTaskRef ref = new AiVideoTaskRef(JimengVisualApi.PLATFORM, JimengVisualApi.REQ_KEY_T2V_V30, "7392616336519610409",
                    JimengVisualApi.ACTION_SYNC2ASYNC_GET_RESULT, null);

            assertEquals(AiVideoTaskStatus.QUEUED, api.getTask(ref).getStatus());
            assertEquals(AiVideoTaskStatus.RUNNING, api.getTask(ref).getStatus());
            AiVideoTaskResult success = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.SUCCEEDED, success.getStatus());
            assertEquals("https://v.volcvideo.com/v.mp4?x-signature=abc", success.getFirstVideoUrl());
            assertEquals(5.04, success.getVideos().get(0).getDurationSeconds());
            assertEquals(AiVideoTaskResult.ERROR_CODE_NO_OUTPUT, api.getTask(ref).getErrorCode());
            assertEquals(AiVideoTaskStatus.EXPIRED, api.getTask(ref).getStatus());
            AiVideoTaskResult notFound = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.UNKNOWN, notFound.getStatus());
            assertFalse(notFound.isFinished());
            // 素材不符合要求、审核未通过：任务失败
            AiVideoTaskResult invalid = api.getTask(ref);
            assertEquals(AiVideoTaskStatus.FAILED, invalid.getStatus());
            assertEquals("50215", invalid.getErrorCode());
            assertEquals("50516", api.getTask(ref).getErrorCode());
            // 限流：抛出异常，稍后重查，不能当作任务失败
            assertThatThrownBy(() -> api.getTask(ref)).isInstanceOfSatisfying(AiVideoException.class,
                    ex -> assertEquals(AiVideoException.Kind.RATE_LIMITED, ex.getKind()));

            VideoMockServer.Request request = server.lastRequest();
            assertEquals("/api/common/v3/get_result_v2", request.path());
            JsonNode body = AiVideoUtils.parseJson(request.body());
            assertEquals("jimeng_t2v_v30", body.get("req_key").asText());
            assertEquals("7392616336519610409", body.get("task_id").asText());
            assertThat(server.requests()).allMatch(r -> r.path().endsWith("get_result_v2"));

            // 非法路由、任务编号不发请求
            ref.setRoute(JimengVisualApi.ACTION_SYNC2ASYNC_SUBMIT);
            assertThatThrownBy(() -> api.getTask(ref)).isInstanceOf(IllegalArgumentException.class);
            assertEquals(9, server.requests().size());
        }
    }

    @Test
    public void testSubjectRecognitionAndDetection() throws Exception {
        try (VideoMockServer server = new VideoMockServer()) {
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"task_id\":\"s-1\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"generating\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"done\",\"resp_data\":\"{\\\"status\\\":1}\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"status\":\"done\",\"resp_data\":\"{\\\"status\\\":0}\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"resp_data\":\"{\\\"code\\\":0,\\\"status\\\":1,"
                    + "\\\"object_detection_result\\\":{\\\"mask\\\":{\\\"url\\\":[\\\"https://a.com/m1.png\\\",\\\"https://a.com/m2.png\\\"]}}}\"}}");
            server.enqueueJson(200, "{\"code\":10000,\"data\":{\"resp_data\":\"{\\\"code\\\":0,\\\"status\\\":0}\"}}");
            JimengVisualApi api = JimengVisualApi.ofApiKey(API_KEY, server.url(), null);

            AiVideoTaskRef ref = api.submitSubjectRecognition("https://a.com/p.png");
            assertEquals(JimengVisualApi.REQ_KEY_OMNI_HUMAN_V15_SUBJECT_RECOGNITION, ref.getModel());
            assertEquals("/api/common/v3/submit_task", server.lastRequest().path());
            JimengVisualApi.SubjectRecognitionResult running = api.getSubjectRecognition(ref);
            assertEquals(AiVideoTaskStatus.RUNNING, running.getStatus());
            assertEquals(null, running.getContainsSubject());
            assertEquals("/api/common/v3/get_result", server.lastRequest().path());
            assertTrue(api.getSubjectRecognition(ref).getContainsSubject());
            assertFalse(api.getSubjectRecognition(ref).getContainsSubject());

            List<String> masks = api.detectSubjects("https://a.com/p.png");
            assertEquals(ListUtil.toList("https://a.com/m1.png", "https://a.com/m2.png"), masks);
            assertEquals("/api/common/v3/process", server.lastRequest().path());
            assertEquals("jimeng_realman_avatar_object_detection",
                    AiVideoUtils.parseJson(server.lastRequest().body()).get("req_key").asText());
            assertTrue(api.detectSubjects("https://a.com/p.png").isEmpty());
        }
    }

}
