package cn.iocoder.yudao.module.yaya.controller.app.compat;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo.YayaAppEvaluationRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeQuestionRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.recording.vo.YayaAppRecordingUploadReqVO;
import cn.iocoder.yudao.module.yaya.service.evaluation.YayaEvaluationService;
import cn.iocoder.yudao.module.yaya.service.practice.YayaPracticeService;
import cn.iocoder.yudao.module.yaya.service.recording.YayaRecordingService;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/api")
@Validated
public class YayaCompatController {

    private static final String DEFAULT_SEASON = "26Q1";
    private static final int DEFAULT_LEGACY_PAGE_SIZE = 200;

    @Resource
    private YayaPracticeService practiceService;
    @Resource
    private YayaRecordingService recordingService;
    @Resource
    private YayaEvaluationService evaluationService;

    @GetMapping("/health")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> health() {
        return orderedMap("ok", true, "service", "yaya-ruoyi");
    }

    @GetMapping("/topics")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> getLegacyTopics(@RequestParam("part") Integer part,
                                               @RequestParam(value = "season", required = false) String season,
                                               @RequestParam(value = "page", required = false) Integer page,
                                               @RequestParam(value = "page_size", required = false) Integer pageSize) {
        int effectivePageSize = pageSize(pageSize);
        PageResult<YayaAppPracticeTopicRespVO> result = practiceService.getTopicPage(
                topicPageReq(part, season, page, effectivePageSize, null, null, null),
                getLoginUserId());
        List<Map<String, Object>> topics = result.getList().stream()
                .map(YayaCompatController::legacyTopic)
                .toList();
        Map<String, Object> response = orderedMap("topics", topics, "part", part);
        response.put("items", topics);
        response.put("count", result.getTotal());
        putPageMeta(response, page, effectivePageSize, result.getTotal());
        return response;
    }

    @GetMapping("/topics/{topicId}")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> getLegacyTopic(@PathVariable("topicId") Long topicId) {
        return legacyTopicDetail(practiceService.getTopicDetail(topicId, getLoginUserId()));
    }

    @PostMapping(value = "/recordings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> createRecording(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "part", required = false) Integer part,
                                               @RequestParam(value = "topic_id", required = false) Long topicId,
                                               @RequestParam(value = "question_id", required = false) Long questionId,
                                               @RequestParam(value = "attempt_id", required = false) Long attemptId,
                                               @RequestParam(value = "duration_seconds", required = false)
                                               Double durationSeconds) throws Exception {
        YayaAppRecordingUploadReqVO reqVO = new YayaAppRecordingUploadReqVO()
                .setFile(file)
                .setTopicId(topicId)
                .setQuestionId(questionId)
                .setAttemptId(attemptId)
                .setDurationSeconds(durationSeconds);
        YayaAppRecordingRespVO recording = recordingService.createRecording(getLoginUserId(), reqVO);
        Map<String, Object> response = orderedMap(
                "id", id(recording.getId()),
                "userId", id(getLoginUserId()),
                "part", part,
                "topicId", id(recording.getTopicId()),
                "filename", file.getOriginalFilename(),
                "contentType", recording.getMimeType(),
                "sizeBytes", recording.getSizeBytes(),
                "storagePath", recording.getStoragePath());
        response.put("status", recording.getStatus());
        return response;
    }

    @PostMapping("/evaluations")
    public Map<String, Object> createEvaluation(@RequestBody LegacyEvaluationCreateReqVO reqVO) {
        YayaAppEvaluationCreateReqVO appReqVO = new YayaAppEvaluationCreateReqVO()
                .setRecordingId(reqVO.getRecordingId())
                .setTopicId(reqVO.getTopicId())
                .setOptions(defaultMap(reqVO.getOptions()));
        return legacyEvaluation(evaluationService.createEvaluation(getLoginUserId(), appReqVO), reqVO.getPart());
    }

    @GetMapping("/evaluations/{evaluationId}")
    public Map<String, Object> getEvaluation(@PathVariable("evaluationId") Long evaluationId) {
        return legacyEvaluation(evaluationService.getEvaluation(getLoginUserId(), evaluationId), null);
    }

    @PostMapping("/evaluations/{evaluationId}/polish-pack")
    public Map<String, Object> createPolishPack(@PathVariable("evaluationId") Long evaluationId) {
        return legacyPolishPack(evaluationService.createPolishPack(getLoginUserId(), evaluationId));
    }

    @GetMapping("/practice/topics")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> getPracticeTopics(@RequestParam("part") Integer part,
                                                 @RequestParam(value = "season", required = false) String season,
                                                 @RequestParam(value = "page", required = false) Integer page,
                                                 @RequestParam(value = "page_size", required = false) Integer pageSize,
                                                 @RequestParam(value = "topic_type", required = false)
                                                 String topicType,
                                                 @RequestParam(value = "progress_filter", required = false)
                                                 String progressFilter,
                                                 @RequestParam(value = "q", required = false) String q) {
        String effectiveSeason = defaultString(season, DEFAULT_SEASON);
        int effectivePageSize = pageSize(pageSize);
        PageResult<YayaAppPracticeTopicRespVO> result = practiceService.getTopicPage(
                topicPageReq(part, effectiveSeason, page, effectivePageSize, topicType, progressFilter, q),
                getLoginUserId());
        Map<String, Object> response = orderedMap(
                "items", result.getList().stream().map(YayaCompatController::practiceTopic).toList(),
                "count", result.getTotal(),
                "season", effectiveSeason,
                "part", part);
        putPageMeta(response, page, effectivePageSize, result.getTotal());
        return response;
    }

    @GetMapping("/practice/topics/{topicId}")
    @PermitAll
    @TenantIgnore
    public Map<String, Object> getPracticeTopic(@PathVariable("topicId") Long topicId) {
        return practiceTopicDetail(practiceService.getTopicDetail(topicId, getLoginUserId()));
    }

    @PostMapping("/practice/favorites")
    public Map<String, Object> createFavorite(@RequestBody LegacyFavoriteReqVO reqVO) {
        Long topicId = reqVO.getTopicId() == null ? parseLong(reqVO.getItemId()) : reqVO.getTopicId();
        Long favoriteId = practiceService.setFavorite(getLoginUserId(), topicId, reqVO.getActive());
        return orderedMap("id", id(favoriteId), "favoriteId", favoriteId, "active", reqVO.getActive());
    }

    @PostMapping("/practice/attempts")
    public Map<String, Object> createAttempt(@RequestBody LegacyAttemptReqVO reqVO) {
        YayaAppPracticeAttemptCreateReqVO appReqVO = new YayaAppPracticeAttemptCreateReqVO()
                .setTopicId(reqVO.getTopicId())
                .setQuestionId(reqVO.getQuestionId())
                .setAnswerText(defaultString(reqVO.getAnswerText(), reqVO.getDraftText()))
                .setDurationSeconds(reqVO.getDurationSeconds())
                .setMetadata(defaultMap(reqVO.getMetadata()));
        Long attemptId = practiceService.createAttempt(getLoginUserId(), appReqVO);
        return orderedMap("id", id(attemptId), "attemptId", attemptId,
                "status", defaultString(reqVO.getStatus(), "submitted"));
    }

    private static YayaAppPracticeTopicPageReqVO topicPageReq(Integer part, String season, Integer page,
                                                              Integer pageSize, String topicType,
                                                              String progressFilter, String q) {
        YayaAppPracticeTopicPageReqVO reqVO = new YayaAppPracticeTopicPageReqVO()
                .setSeason(defaultString(season, DEFAULT_SEASON))
                .setPart(part)
                .setTopicType(topicType)
                .setProgressFilter(progressFilter)
                .setQ(q);
        reqVO.setPageNo(defaultIfNull(page, 1));
        reqVO.setPageSize(pageSize(pageSize));
        return reqVO;
    }

    private static Map<String, Object> legacyTopic(YayaAppPracticeTopicRespVO topic) {
        Map<String, Object> item = orderedMap(
                "id", id(topic.getId()),
                "part", topic.getPart(),
                "topicType", topic.getTopicType(),
                "updateDate", "",
                "title", topic.getTitleEn(),
                "category", defaultString(topic.getCategory(), ""),
                "prompt", "",
                "questions", List.of(),
                "sourceFile", "");
        item.put("number", topic.getTopicNo());
        item.put("titleZh", topic.getTitleZh());
        item.put("favorite", boolInt(topic.getFavorite()));
        item.put("practiced", boolInt(topic.getPracticed()));
        return item;
    }

    private static Map<String, Object> legacyTopicDetail(YayaAppPracticeTopicDetailRespVO topic) {
        Map<String, Object> item = legacyTopic(topic);
        item.put("prompt", defaultString(topic.getPromptEn(), ""));
        item.put("promptZh", defaultString(topic.getPromptZh(), ""));
        item.put("questions", topic.getQuestions().stream()
                .map(YayaAppPracticeQuestionRespVO::getPromptEn)
                .toList());
        item.put("question_items", topic.getQuestions().stream()
                .map(YayaCompatController::practiceQuestion)
                .toList());
        item.put("metadata", defaultMap(topic.getMetadata()));
        return item;
    }

    private static Map<String, Object> practiceTopic(YayaAppPracticeTopicRespVO topic) {
        Map<String, Object> item = orderedMap(
                "id", id(topic.getId()),
                "part", topic.getPart(),
                "number", topic.getTopicNo(),
                "title_en", defaultString(topic.getTitleEn(), ""),
                "title_zh", defaultString(topic.getTitleZh(), ""),
                "topic_type", defaultString(topic.getTopicType(), ""),
                "category", defaultString(topic.getCategory(), ""),
                "prompt_en", "",
                "prompt_zh", "",
                "is_favorite", Boolean.TRUE.equals(topic.getFavorite()),
                "favorite", boolInt(topic.getFavorite()),
                "practiced", boolInt(topic.getPracticed()));
        item.put("user_state", orderedMap("status", Boolean.TRUE.equals(topic.getPracticed()) ? "practiced" : "unstarted",
                "practiced", boolInt(topic.getPracticed()), "last_practiced_at", null, "metadata", Map.of()));
        return item;
    }

    private static Map<String, Object> practiceTopicDetail(YayaAppPracticeTopicDetailRespVO topic) {
        Map<String, Object> item = practiceTopic(topic);
        item.put("stable_key", defaultString(topic.getStableKey(), ""));
        item.put("prompt_en", defaultString(topic.getPromptEn(), ""));
        item.put("prompt_zh", defaultString(topic.getPromptZh(), ""));
        item.put("display_order", topic.getDisplayOrder());
        item.put("review_status", "");
        item.put("metadata", defaultMap(topic.getMetadata()));
        item.put("completed_question_ids", topic.getCompletedQuestionIds().stream()
                .map(YayaCompatController::id)
                .toList());
        item.put("questions", topic.getQuestions().stream()
                .map(YayaCompatController::practiceQuestion)
                .toList());
        return item;
    }

    private static Map<String, Object> practiceQuestion(YayaAppPracticeQuestionRespVO question) {
        return orderedMap(
                "id", id(question.getId()),
                "topic_id", id(question.getTopicId()),
                "question_role", question.getQuestionRole(),
                "prompt_en", defaultString(question.getPromptEn(), ""),
                "prompt_zh", defaultString(question.getPromptZh(), ""),
                "cue_bullets", question.getCueBullets() == null ? List.of() : question.getCueBullets(),
                "display_order", question.getDisplayOrder(),
                "prepare_seconds", question.getPrepareSeconds(),
                "answer_seconds", question.getAnswerSeconds(),
                "metadata", defaultMap(question.getMetadata()));
    }

    private static Map<String, Object> legacyEvaluation(YayaAppEvaluationRespVO evaluation, Integer part) {
        Map<String, Object> report = defaultMap(evaluation.getReport());
        Map<String, Object> response = orderedMap(
                "id", id(evaluation.getId()),
                "status", legacyStatus(evaluation.getStatus()),
                "provider", evaluation.getProvider(),
                "model", evaluation.getModel(),
                "scores", defaultMap(evaluation.getScores()),
                "report", report,
                "topicId", id(evaluation.getTopicId()),
                "recordingId", id(evaluation.getRecordingId()));
        response.put("aiTaskId", id(evaluation.getAiTaskId()));
        response.put("scoreOverall", evaluation.getScoreOverall());
        response.put("polishPackStatus", legacyStatus(evaluation.getPolishPackStatus()));
        response.put("text_route_scores", defaultMap(evaluation.getTextRouteScores()));
        response.put("audio_route_scores", defaultMap(evaluation.getAudioRouteScores()));
        response.put("pron_route_scores", defaultMap(evaluation.getPronRouteScores()));
        response.put("fluency_metrics", defaultMap(evaluation.getFluencyMetrics()));
        response.put("band_lower", evaluation.getBandLower());
        response.put("band_upper", evaluation.getBandUpper());
        response.put("attemptId", idFromReport(report, "attemptId", "attempt_id"));
        response.put("questionId", idFromReport(report, "questionId", "question_id"));
        response.put("createdAt", iso(evaluation.getCreateTime()));
        response.put("updatedAt", iso(evaluation.getUpdateTime()));
        if (part != null) {
            response.put("part", part);
        }
        return response;
    }

    private static Map<String, Object> legacyPolishPack(YayaAppEvaluationRespVO evaluation) {
        Map<String, Object> report = defaultMap(evaluation.getReport());
        Map<String, Object> pack = mapValue(report.get("polish_pack"));
        if (pack.isEmpty() && "polish_pack_v4".equals(report.get("schema_version"))) {
            pack = report;
        }
        Map<String, Object> response = orderedMap(
                "schema_version", defaultString(asString(pack.get("schema_version")), "polish_pack_v4"),
                "original_zh", defaultString(asString(pack.get("original_zh"))),
                "ai_fixed_en", defaultString(asString(pack.get("ai_fixed_en"))),
                "ai_fixed_zh", defaultString(asString(pack.get("ai_fixed_zh"))),
                "topic_summary_zh", defaultString(asString(pack.get("topic_summary_zh"))),
                "answer_quality", defaultMap(mapValue(pack.get("answer_quality"))),
                "polish", defaultPolish(mapValue(pack.get("polish"))),
                "related_topics", listValue(pack.get("related_topics")));
        response.put("status", legacyStatus(evaluation.getPolishPackStatus()));
        return response;
    }

    private static String legacyStatus(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "PENDING" -> "queued";
            case "RUNNING" -> "running";
            case "SUCCEEDED", "COMPLETED", "DONE" -> "done";
            case "FAILED", "CANCELLED" -> "failed";
            default -> status.toLowerCase();
        };
    }

    private static int boolInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private static Long parseLong(String value) {
        return value == null || value.isBlank() ? null : Long.valueOf(value);
    }

    private static String idFromReport(Map<String, Object> report, String camelKey, String snakeKey) {
        Object value = report.get(camelKey);
        if (value == null) {
            value = report.get(snakeKey);
        }
        return value == null ? null : value.toString();
    }

    private static String id(Long value) {
        return value == null ? null : value.toString();
    }

    private static int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static int pageSize(Integer value) {
        return Math.max(1, defaultIfNull(value, DEFAULT_LEGACY_PAGE_SIZE));
    }

    private static void putPageMeta(Map<String, Object> response, Integer page, int pageSize, Long total) {
        int size = Math.max(1, pageSize);
        int totalCount = total == null ? 0 : total.intValue();
        int totalPages = Math.max(1, (totalCount + size - 1) / size);
        int pageNo = Math.max(1, defaultIfNull(page, 1));
        response.put("page", pageNo);
        response.put("page_size", size);
        response.put("total_pages", totalPages);
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static String defaultString(String value) {
        return defaultString(value, "");
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String iso(LocalDateTime value) {
        return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static Map<String, Object> defaultMap(Map<String, Object> value) {
        return value == null ? Collections.emptyMap() : value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> listValue(Object value) {
        return value instanceof List<?> ? (List<Object>) value : List.of();
    }

    private static Map<String, Object> defaultPolish(Map<String, Object> value) {
        Map<String, Object> polish = new LinkedHashMap<>(value);
        polish.putIfAbsent("优化", Map.of());
        polish.putIfAbsent("扩写", Map.of());
        polish.putIfAbsent("优化+扩写", Map.of());
        return polish;
    }

    private static Map<String, Object> orderedMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    @Data
    @Accessors(chain = true)
    public static class LegacyEvaluationCreateReqVO {

        @JsonProperty("recording_id")
        private Long recordingId;
        @JsonProperty("topic_id")
        private Long topicId;
        private Integer part;
        private Map<String, Object> options;
    }

    @Data
    @Accessors(chain = true)
    public static class LegacyFavoriteReqVO {

        @JsonProperty("topic_id")
        private Long topicId;
        @JsonProperty("item_id")
        private String itemId;
        private Boolean active = true;
    }

    @Data
    @Accessors(chain = true)
    public static class LegacyAttemptReqVO {

        @JsonProperty("topic_id")
        private Long topicId;
        @JsonProperty("question_id")
        private Long questionId;
        @JsonProperty("answer_text")
        private String answerText;
        @JsonProperty("draft_text")
        private String draftText;
        @JsonProperty("duration_seconds")
        private Integer durationSeconds;
        private String status;
        private Map<String, Object> metadata;
    }

}
