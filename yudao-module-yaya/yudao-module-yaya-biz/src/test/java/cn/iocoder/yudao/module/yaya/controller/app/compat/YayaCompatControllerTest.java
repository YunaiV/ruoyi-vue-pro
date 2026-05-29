package cn.iocoder.yudao.module.yaya.controller.app.compat;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeQuestionRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.service.evaluation.YayaEvaluationService;
import cn.iocoder.yudao.module.yaya.service.practice.YayaPracticeService;
import cn.iocoder.yudao.module.yaya.service.recording.YayaRecordingService;
import jakarta.annotation.security.PermitAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YayaCompatControllerTest {

    @Test
    void shouldExposeLegacyRoutesUnderApi() throws Exception {
        RequestMapping mapping = YayaCompatController.class.getAnnotation(RequestMapping.class);
        assertArrayEquals(new String[]{"/api"}, mapping.value());

        assertGetMapping(method("health"), "/health");
        assertGetMapping(method("getLegacyTopics", Integer.class, String.class, Integer.class, Integer.class), "/topics");
        assertGetMapping(method("getLegacyTopic", Long.class), "/topics/{topicId}");
        assertPostMapping(method("createRecording", org.springframework.web.multipart.MultipartFile.class,
                Integer.class, Long.class, Long.class, Long.class, Double.class), "/recordings");
        assertPostMapping(method("createEvaluation", YayaCompatController.LegacyEvaluationCreateReqVO.class),
                "/evaluations");
        assertGetMapping(method("getEvaluation", Long.class), "/evaluations/{evaluationId}");
        assertPostMapping(method("createPolishPack", Long.class), "/evaluations/{evaluationId}/polish-pack");
        assertGetMapping(method("getPracticeTopics", Integer.class, String.class, Integer.class, Integer.class,
                String.class, String.class, String.class), "/practice/topics");
        assertGetMapping(method("getPracticeTopic", Long.class), "/practice/topics/{topicId}");
        assertPostMapping(method("createFavorite", YayaCompatController.LegacyFavoriteReqVO.class),
                "/practice/favorites");
        assertPostMapping(method("createAttempt", YayaCompatController.LegacyAttemptReqVO.class),
                "/practice/attempts");
    }

    @Test
    void controllerPackageShouldReceiveAppApiPrefix() {
        assertTrue(YayaCompatController.class.getPackageName().contains(".controller.app."));
    }

    @Test
    void readCompatibilityRoutesShouldAllowAnonymousAccess() throws Exception {
        assertNotNull(method("health").getAnnotation(PermitAll.class));
        assertNotNull(method("health").getAnnotation(TenantIgnore.class));
        assertNotNull(method("getLegacyTopics", Integer.class, String.class, Integer.class, Integer.class)
                .getAnnotation(PermitAll.class));
        assertNotNull(method("getLegacyTopic", Long.class).getAnnotation(TenantIgnore.class));
        assertNull(method("createEvaluation", YayaCompatController.LegacyEvaluationCreateReqVO.class)
                .getAnnotation(PermitAll.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLegacyTopicsShouldPreserveTopicsAndPartShape() {
        YayaPracticeService practiceService = mock(YayaPracticeService.class);
        YayaCompatController controller = controller(practiceService, null, null);
        YayaAppPracticeTopicRespVO topic = new YayaAppPracticeTopicRespVO()
                .setId(146L)
                .setPart(1)
                .setTopicNo(7)
                .setTitleEn("Accommodation")
                .setTitleZh("住宿")
                .setTopicType("必考话题")
                .setCategory("Life")
                .setFavorite(false)
                .setPracticed(true);
        when(practiceService.getTopicPage(any(YayaAppPracticeTopicPageReqVO.class), isNull()))
                .thenReturn(new PageResult<>(List.of(topic), 1L));

        Map<String, Object> response = controller.getLegacyTopics(1, null, null, null);

        assertEquals(1, response.get("part"));
        assertEquals(1L, response.get("count"));
        List<Map<String, Object>> topics = (List<Map<String, Object>>) response.get("topics");
        assertEquals("146", topics.get(0).get("id"));
        assertEquals("Accommodation", topics.get(0).get("title"));
        assertEquals("必考话题", topics.get(0).get("topicType"));
        assertEquals(1, topics.get(0).get("practiced"));
        ArgumentCaptor<YayaAppPracticeTopicPageReqVO> captor =
                ArgumentCaptor.forClass(YayaAppPracticeTopicPageReqVO.class);
        verify(practiceService).getTopicPage(captor.capture(), isNull());
        assertEquals("26Q1", captor.getValue().getSeason());
        assertEquals(1, captor.getValue().getPart());
        assertEquals(200, captor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPracticeTopicsShouldReturnLegacyPracticeShape() {
        YayaPracticeService practiceService = mock(YayaPracticeService.class);
        YayaCompatController controller = controller(practiceService, null, null);
        when(practiceService.getTopicPage(any(YayaAppPracticeTopicPageReqVO.class), isNull()))
                .thenReturn(new PageResult<>(List.of(new YayaAppPracticeTopicRespVO()
                        .setId(100L)
                        .setPart(2)
                        .setTopicNo(3)
                        .setTitleEn("A person")
                        .setTitleZh("一个人")
                        .setTopicType("新题")
                        .setFavorite(true)
                        .setPracticed(false)), 41L));

        Map<String, Object> response = controller.getPracticeTopics(2, "26Q1", 2, 20,
                "new", "favorite", "person");

        assertEquals("26Q1", response.get("season"));
        assertEquals(2, response.get("part"));
        assertEquals(2, response.get("page"));
        assertEquals(20, response.get("page_size"));
        assertEquals(3, response.get("total_pages"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        assertEquals("100", items.get(0).get("id"));
        assertEquals("A person", items.get(0).get("title_en"));
        assertEquals(1, items.get(0).get("favorite"));
        assertEquals(0, items.get(0).get("practiced"));
        assertTrue(items.get(0).containsKey("user_state"));
        ArgumentCaptor<YayaAppPracticeTopicPageReqVO> captor =
                ArgumentCaptor.forClass(YayaAppPracticeTopicPageReqVO.class);
        verify(practiceService).getTopicPage(captor.capture(), isNull());
        assertEquals("26Q1", captor.getValue().getSeason());
        assertEquals(2, captor.getValue().getPart());
        assertEquals(2, captor.getValue().getPageNo());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals("new", captor.getValue().getTopicType());
        assertEquals("favorite", captor.getValue().getProgressFilter());
        assertEquals("person", captor.getValue().getQ());
    }

    @Test
    void getPracticeTopicsShouldClampInvalidLegacyPageSizeAndPreserveRequestedPage() {
        YayaPracticeService practiceService = mock(YayaPracticeService.class);
        YayaCompatController controller = controller(practiceService, null, null);
        when(practiceService.getTopicPage(any(YayaAppPracticeTopicPageReqVO.class), isNull()))
                .thenReturn(new PageResult<>(List.of(), 2L));

        Map<String, Object> response = controller.getPracticeTopics(1, null, 999, 0, null, null, null);

        assertEquals(999, response.get("page"));
        assertEquals(1, response.get("page_size"));
        assertEquals(2, response.get("total_pages"));
        ArgumentCaptor<YayaAppPracticeTopicPageReqVO> captor =
                ArgumentCaptor.forClass(YayaAppPracticeTopicPageReqVO.class);
        verify(practiceService).getTopicPage(captor.capture(), isNull());
        assertEquals(999, captor.getValue().getPageNo());
        assertEquals(1, captor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPracticeTopicShouldReturnPublicPracticeDetailShape() {
        YayaPracticeService practiceService = mock(YayaPracticeService.class);
        YayaCompatController controller = controller(practiceService, null, null);
        YayaAppPracticeTopicDetailRespVO detail = new YayaAppPracticeTopicDetailRespVO()
                .setPromptEn("Describe a place.")
                .setPromptZh("描述一个地方。")
                .setMetadata(Map.of("source", "legacy"));
        detail.setId(100L);
        detail.setPart(2);
        detail.setStableKey("26Q1-P2-001");
        detail.setTitleEn("A place");
        detail.setTitleZh("一个地方");
        detail.setTopicType("新题");
        detail.setDisplayOrder(1);
        detail.setCompletedQuestionIds(List.of(101L));
        detail.setQuestions(List.of(new YayaAppPracticeQuestionRespVO()
                .setId(101L)
                .setTopicId(100L)
                .setPromptEn("Where is it?")
                .setPromptZh("它在哪里？")
                .setCueBullets(List.of("where", "why"))));
        when(practiceService.getTopicDetail(100L, null)).thenReturn(detail);

        Map<String, Object> response = controller.getPracticeTopic(100L);

        assertEquals("100", response.get("id"));
        assertEquals("26Q1-P2-001", response.get("stable_key"));
        assertEquals("Describe a place.", response.get("prompt_en"));
        assertEquals("描述一个地方。", response.get("prompt_zh"));
        assertEquals(1, response.get("display_order"));
        assertEquals(List.of("101"), response.get("completed_question_ids"));
        List<Map<String, Object>> questions = (List<Map<String, Object>>) response.get("questions");
        assertEquals("101", questions.get(0).get("id"));
        assertEquals(List.of("where", "why"), questions.get(0).get("cue_bullets"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLegacyTopicShouldReturnDetailWithQuestionPrompts() {
        YayaPracticeService practiceService = mock(YayaPracticeService.class);
        YayaCompatController controller = controller(practiceService, null, null);
        YayaAppPracticeTopicDetailRespVO detail = new YayaAppPracticeTopicDetailRespVO()
                .setPromptEn("Talk about your home.")
                .setPromptZh("谈谈你的家。");
        detail.setId(100L);
        detail.setPart(1);
        detail.setTitleEn("Accommodation");
        detail.setQuestions(List.of(new YayaAppPracticeQuestionRespVO().setPromptEn("Do you live in a house?")));
        when(practiceService.getTopicDetail(100L, null)).thenReturn(detail);

        Map<String, Object> response = controller.getLegacyTopic(100L);

        assertEquals("100", response.get("id"));
        assertEquals("Accommodation", response.get("title"));
        assertEquals("Talk about your home.", response.get("prompt"));
        assertEquals(List.of("Do you live in a house?"), response.get("questions"));
        List<Map<String, Object>> questionItems = (List<Map<String, Object>>) response.get("question_items");
        assertEquals("Do you live in a house?", questionItems.get(0).get("prompt_en"));
    }

    @Test
    void writeRoutesShouldDelegateToExistingServicesAndMapLegacyIds() throws Exception {
        YayaRecordingService recordingService = mock(YayaRecordingService.class);
        YayaEvaluationService evaluationService = mock(YayaEvaluationService.class);
        YayaPracticeService practiceService = mock(YayaPracticeService.class);
        YayaCompatController controller = controller(practiceService, recordingService, evaluationService);
        when(recordingService.createRecording(eq(null), any())).thenReturn(new YayaAppRecordingRespVO()
                .setId(200L)
                .setTopicId(146L)
                .setStoragePath("recordings/200.webm")
                .setMimeType("audio/webm")
                .setSizeBytes(5L)
                .setStatus("stored"));
        when(evaluationService.createEvaluation(eq(null), any())).thenReturn(new YayaAppEvaluationRespVO()
                .setId(300L)
                .setRecordingId(200L)
                .setTopicId(146L)
                .setAiTaskId(400L)
                .setStatus("PENDING")
                .setScores(Map.of())
                .setReport(Map.of()));
        when(evaluationService.getEvaluation(null, 300L)).thenReturn(new YayaAppEvaluationRespVO()
                .setId(300L)
                .setRecordingId(200L)
                .setTopicId(146L)
                .setStatus("SUCCEEDED")
                .setScores(Map.of("overall", 7.0))
                .setReport(Map.of("summary", "done", "attemptId", 9, "questionId", 1))
                .setTextRouteScores(Map.of("lr", 7))
                .setAudioRouteScores(Map.of("fluency", 6))
                .setPronRouteScores(Map.of("accuracy", 8))
                .setFluencyMetrics(Map.of("wpm", 120))
                .setBandLower(6.5)
                .setBandUpper(7.0)
                .setCreateTime(LocalDateTime.of(2026, 5, 29, 10, 0))
                .setUpdateTime(LocalDateTime.of(2026, 5, 29, 10, 1)));
        when(evaluationService.createPolishPack(null, 300L)).thenReturn(new YayaAppEvaluationRespVO()
                .setId(300L)
                .setStatus("PENDING")
                .setPolishPackStatus("PENDING")
                .setReport(Map.of("polish_pack", Map.of(
                        "schema_version", "polish_pack_v4",
                        "original_zh", "原文",
                        "ai_fixed_en", "Fixed answer",
                        "related_topics", List.of(Map.of("id", "topic-1"))))));
        when(practiceService.setFavorite(null, 146L, true)).thenReturn(10L);
        when(practiceService.setFavorite(null, 146L, false)).thenReturn(10L);
        when(practiceService.createAttempt(eq(null), any())).thenReturn(11L);

        Map<String, Object> recording = controller.createRecording(
                new MockMultipartFile("file", "answer.webm", "audio/webm", "audio".getBytes()),
                2, 146L, 1L, 9L, 12.5);
        YayaCompatController.LegacyEvaluationCreateReqVO evaluationReq =
                new YayaCompatController.LegacyEvaluationCreateReqVO()
                        .setRecordingId(200L)
                        .setTopicId(146L)
                        .setPart(2);
        Map<String, Object> evaluation = controller.createEvaluation(evaluationReq);
        Map<String, Object> evaluationDetail = controller.getEvaluation(300L);
        Map<String, Object> polishPack = controller.createPolishPack(300L);
        Map<String, Object> favorite = controller.createFavorite(new YayaCompatController.LegacyFavoriteReqVO()
                .setItemId("146")
                .setActive(true));
        Map<String, Object> removedFavorite = controller.createFavorite(new YayaCompatController.LegacyFavoriteReqVO()
                .setItemId("146")
                .setActive(false));
        Map<String, Object> attempt = controller.createAttempt(new YayaCompatController.LegacyAttemptReqVO()
                .setTopicId(146L)
                .setQuestionId(1L)
                .setDraftText("answer")
                .setDurationSeconds(90));

        assertEquals("200", recording.get("id"));
        assertEquals("audio/webm", recording.get("contentType"));
        assertEquals("300", evaluation.get("id"));
        assertEquals("queued", evaluation.get("status"));
        assertEquals("done", evaluationDetail.get("status"));
        assertEquals(Map.of("lr", 7), evaluationDetail.get("text_route_scores"));
        assertEquals(Map.of("fluency", 6), evaluationDetail.get("audio_route_scores"));
        assertEquals(Map.of("accuracy", 8), evaluationDetail.get("pron_route_scores"));
        assertEquals(Map.of("wpm", 120), evaluationDetail.get("fluency_metrics"));
        assertEquals(6.5, evaluationDetail.get("band_lower"));
        assertEquals(7.0, evaluationDetail.get("band_upper"));
        assertEquals("9", evaluationDetail.get("attemptId"));
        assertEquals("1", evaluationDetail.get("questionId"));
        assertEquals("2026-05-29T10:00:00", evaluationDetail.get("createdAt"));
        assertEquals("polish_pack_v4", polishPack.get("schema_version"));
        assertEquals("原文", polishPack.get("original_zh"));
        assertEquals("Fixed answer", polishPack.get("ai_fixed_en"));
        assertEquals("queued", polishPack.get("status"));
        assertTrue(((Map<String, Object>) polishPack.get("polish")).containsKey("优化"));
        assertEquals("10", favorite.get("id"));
        assertEquals("10", removedFavorite.get("id"));
        assertEquals(false, removedFavorite.get("active"));
        assertEquals("11", attempt.get("id"));
        assertEquals("submitted", attempt.get("status"));
        verify(practiceService).setFavorite(null, 146L, true);
        verify(practiceService).setFavorite(null, 146L, false);
        ArgumentCaptor<YayaAppPracticeAttemptCreateReqVO> attemptCaptor =
                ArgumentCaptor.forClass(YayaAppPracticeAttemptCreateReqVO.class);
        verify(practiceService).createAttempt(isNull(), attemptCaptor.capture());
        assertEquals(1L, attemptCaptor.getValue().getQuestionId());
    }

    private static YayaCompatController controller(YayaPracticeService practiceService,
                                                   YayaRecordingService recordingService,
                                                   YayaEvaluationService evaluationService) {
        YayaCompatController controller = new YayaCompatController();
        ReflectionTestUtils.setField(controller, "practiceService", practiceService);
        ReflectionTestUtils.setField(controller, "recordingService", recordingService);
        ReflectionTestUtils.setField(controller, "evaluationService", evaluationService);
        return controller;
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return YayaCompatController.class.getMethod(name, parameterTypes);
    }

    private static void assertGetMapping(Method method, String path) {
        GetMapping annotation = method.getAnnotation(GetMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

    private static void assertPostMapping(Method method, String path) {
        PostMapping annotation = method.getAnnotation(PostMapping.class);
        assertNotNull(annotation);
        assertArrayEquals(new String[]{path}, annotation.value());
    }

}
