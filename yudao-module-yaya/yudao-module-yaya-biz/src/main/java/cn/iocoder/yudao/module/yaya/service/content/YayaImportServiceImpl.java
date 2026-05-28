package cn.iocoder.yudao.module.yaya.service.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaContentSeasonDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaImportBatchDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeQuestionDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeTopicDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaContentSeasonMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaImportBatchMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeQuestionMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeTopicMapper;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
@Validated
public class YayaImportServiceImpl implements YayaImportService {

    private static final TypeReference<List<JsonNode>> JSON_NODE_LIST = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<Object>> OBJECT_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Path snapshotRoot = Path.of("docs/yaya-migration/content-import-snapshots");

    @Resource
    private YayaContentSeasonMapper seasonMapper;
    @Resource
    private YayaPracticeTopicMapper topicMapper;
    @Resource
    private YayaPracticeQuestionMapper questionMapper;
    @Resource
    private YayaImportBatchMapper importBatchMapper;

    @Value("${yaya.import.snapshot-root:}")
    public void setSnapshotRoot(String snapshotRoot) {
        if (StringUtils.hasText(snapshotRoot)) {
            this.snapshotRoot = Path.of(snapshotRoot);
        }
    }

    @Override
    public YayaImportResultResp previewImport(String seasonKey) {
        String normalizedSeasonKey = normalizeSeasonKey(seasonKey);
        try {
            Snapshot snapshot = loadNewestSnapshot();
            Count count = readManifestCount(snapshot.manifest(), normalizedSeasonKey);
            return new YayaImportResultResp()
                    .setSeasonKey(normalizedSeasonKey)
                    .setTopics(count.topics())
                    .setQuestions(count.questions())
                    .setErrors(List.of());
        } catch (IOException | IllegalStateException ex) {
            return new YayaImportResultResp()
                    .setSeasonKey(normalizedSeasonKey)
                    .setTopics(0)
                    .setQuestions(0)
                    .setErrors(List.of(ex.getMessage()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YayaImportResultResp runImport(String seasonKey) {
        String normalizedSeasonKey = normalizeSeasonKey(seasonKey);
        try {
            Snapshot snapshot = loadNewestSnapshot();
            Count count = readManifestCount(snapshot.manifest(), normalizedSeasonKey);
            JsonNode seasonNode = readSeason(snapshot, normalizedSeasonKey);
            List<JsonNode> topics = filterBySeason(readRequiredArray(snapshot.path().resolve("practice_topics.json")),
                    normalizedSeasonKey);
            List<JsonNode> questions = filterBySeason(readRequiredArray(snapshot.path().resolve("practice_questions.json")),
                    normalizedSeasonKey);
            validateSnapshotCounts(normalizedSeasonKey, count, topics, questions);
            validateQuestionTopicReferences(topics, questions);
            Map<String, List<JsonNode>> questionsByTopic = groupQuestionsByTopic(questions);

            YayaContentSeasonDO season = createOrReuseSeason(normalizedSeasonKey, seasonNode);
            int insertedTopics = 0;
            int updatedTopics = 0;
            int writtenQuestions = 0;
            for (JsonNode topicNode : topics) {
                YayaPracticeTopicDO topic = upsertTopic(season.getId(), topicNode);
                if (topic.getId() == null) {
                    topicMapper.insert(topic);
                    insertedTopics++;
                } else {
                    topicMapper.updateById(topic);
                    updatedTopics++;
                }
                List<YayaPracticeQuestionDO> questionDOs = buildQuestions(topic.getId(),
                        questionsByTopic.get(text(topicNode, "legacy_uuid")));
                questionMapper.deleteByTopicId(topic.getId());
                if (CollUtil.isNotEmpty(questionDOs)) {
                    questionMapper.insertBatch(questionDOs);
                    writtenQuestions += questionDOs.size();
                }
            }

            YayaImportBatchDO batch = new YayaImportBatchDO();
            batch.setSeasonKey(normalizedSeasonKey);
            batch.setSourceLabel("legacy snapshot " + snapshot.path().getFileName());
            batch.setStatus("completed");
            batch.setCreatedBy("local-admin");
            batch.setSummary(Map.of(
                    "season", normalizedSeasonKey,
                    "topics", count.topics(),
                    "questions", count.questions(),
                    "insertedTopics", insertedTopics,
                    "updatedTopics", updatedTopics,
                    "writtenQuestions", writtenQuestions,
                    "snapshot", snapshot.path().getFileName().toString()
            ));
            importBatchMapper.insert(batch);
            return new YayaImportResultResp()
                    .setSeasonKey(normalizedSeasonKey)
                    .setTopics(count.topics())
                    .setQuestions(count.questions())
                    .setErrors(List.of());
        } catch (IOException | IllegalStateException ex) {
            return new YayaImportResultResp()
                    .setSeasonKey(normalizedSeasonKey)
                    .setTopics(0)
                    .setQuestions(0)
                    .setErrors(List.of(ex.getMessage()));
        }
    }

    private Snapshot loadNewestSnapshot() throws IOException {
        if (!Files.exists(snapshotRoot)) {
            throw new IllegalStateException("Snapshot root does not exist: " + snapshotRoot);
        }
        if (Files.isRegularFile(snapshotRoot.resolve("manifest.json"))) {
            return new Snapshot(snapshotRoot, readObject(snapshotRoot.resolve("manifest.json")));
        }
        try (Stream<Path> stream = Files.list(snapshotRoot)) {
            Path latest = stream
                    .filter(Files::isDirectory)
                    .max(Comparator.comparing(path -> path.getFileName().toString()))
                    .orElseThrow(() -> new IllegalStateException("No snapshot directory found under: " + snapshotRoot));
            return new Snapshot(latest, readObject(latest.resolve("manifest.json")));
        }
    }

    private JsonNode readObject(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Snapshot file does not exist: " + path);
        }
        return objectMapper.readTree(path.toFile());
    }

    private List<JsonNode> readRequiredArray(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Snapshot file does not exist: " + path);
        }
        return objectMapper.readValue(path.toFile(), JSON_NODE_LIST);
    }

    private Count readManifestCount(JsonNode manifest, String seasonKey) {
        JsonNode count = manifest.path("season_counts").path(seasonKey);
        if (count.isMissingNode()) {
            count = manifest.path("seasons").path(seasonKey);
        }
        if (count.isMissingNode()) {
            throw new IllegalStateException("Season not found in manifest: " + seasonKey);
        }
        return new Count(count.path("topics").asInt(0), count.path("questions").asInt(0));
    }

    private JsonNode readSeason(Snapshot snapshot, String seasonKey) throws IOException {
        return readRequiredArray(snapshot.path().resolve("content_seasons.json")).stream()
                .filter(node -> seasonKey.equalsIgnoreCase(text(node, "season_key")))
                .findFirst()
                .orElse(null);
    }

    private static void validateSnapshotCounts(String seasonKey, Count count, List<JsonNode> topics,
                                               List<JsonNode> questions) {
        if (topics.size() != count.topics() || questions.size() != count.questions()) {
            throw new IllegalStateException("Snapshot count mismatch for " + seasonKey + ": manifest topics="
                    + count.topics() + ", actual topics=" + topics.size() + ", manifest questions="
                    + count.questions() + ", actual questions=" + questions.size());
        }
    }

    private static void validateQuestionTopicReferences(List<JsonNode> topics, List<JsonNode> questions) {
        Set<String> topicLegacyUuids = new HashSet<>();
        for (JsonNode topic : topics) {
            String legacyUuid = text(topic, "legacy_uuid");
            if (!legacyUuid.isBlank()) {
                topicLegacyUuids.add(legacyUuid);
            }
        }
        for (JsonNode question : questions) {
            String topicLegacyUuid = text(question, "topic_legacy_uuid");
            if (topicLegacyUuid.isBlank() || !topicLegacyUuids.contains(topicLegacyUuid)) {
                throw new IllegalStateException("Question references missing topic: " + topicLegacyUuid
                        + ", question=" + text(question, "legacy_uuid"));
            }
        }
    }

    private YayaContentSeasonDO createOrReuseSeason(String seasonKey, JsonNode seasonNode) {
        YayaContentSeasonDO season = seasonMapper.selectBySeasonKey(seasonKey);
        if (season != null) {
            return season;
        }
        season = new YayaContentSeasonDO();
        season.setSeasonKey(seasonKey);
        season.setName(text(seasonNode, "name", seasonKey));
        season.setActive(booleanToInt(bool(seasonNode, "active", true)));
        season.setDefaulted(booleanToInt(bool(seasonNode, "defaulted", false)));
        seasonMapper.insert(season);
        return season;
    }

    private List<JsonNode> filterBySeason(List<JsonNode> nodes, String seasonKey) {
        return nodes.stream()
                .filter(node -> seasonKey.equalsIgnoreCase(text(node, "season_key")))
                .toList();
    }

    private Map<String, List<JsonNode>> groupQuestionsByTopic(List<JsonNode> questions) {
        Map<String, List<JsonNode>> grouped = new HashMap<>();
        for (JsonNode question : questions) {
            grouped.computeIfAbsent(text(question, "topic_legacy_uuid"), key -> new ArrayList<>()).add(question);
        }
        return grouped;
    }

    private YayaPracticeTopicDO upsertTopic(Long seasonId, JsonNode topicNode) {
        YayaPracticeTopicDO topic = topicMapper.selectBySeasonPartStableKey(seasonId,
                intValue(topicNode, "part", 0), text(topicNode, "stable_key"));
        if (topic == null) {
            topic = new YayaPracticeTopicDO();
        }
        topic.setLegacyUuid(text(topicNode, "legacy_uuid"));
        topic.setSeasonId(seasonId);
        topic.setPart(intValue(topicNode, "part", 0));
        topic.setStableKey(text(topicNode, "stable_key"));
        topic.setTopicNo(intValue(topicNode, "topic_no", intValue(topicNode, "number", 0)));
        topic.setTitleEn(text(topicNode, "title_en"));
        topic.setTitleZh(text(topicNode, "title_zh"));
        topic.setTopicType(text(topicNode, "topic_type"));
        topic.setCategory(text(topicNode, "category"));
        topic.setPromptEn(text(topicNode, "prompt_en"));
        topic.setPromptZh(text(topicNode, "prompt_zh"));
        topic.setDisplayOrder(intValue(topicNode, "display_order", 0));
        topic.setReviewStatus(text(topicNode, "review_status", "draft"));
        topic.setPublishStatus(text(topicNode, "publish_status", "draft"));
        topic.setMetadata(mapValue(topicNode, "metadata"));
        return topic;
    }

    private List<YayaPracticeQuestionDO> buildQuestions(Long topicId, List<JsonNode> questions) {
        if (CollUtil.isEmpty(questions)) {
            return List.of();
        }
        return convertList(questions, question -> {
            YayaPracticeQuestionDO questionDO = new YayaPracticeQuestionDO();
            questionDO.setLegacyUuid(text(question, "legacy_uuid"));
            questionDO.setTopicId(topicId);
            questionDO.setQuestionRole(text(question, "question_role", "question"));
            questionDO.setPromptEn(text(question, "prompt_en"));
            questionDO.setPromptZh(text(question, "prompt_zh"));
            questionDO.setCueBullets(listValue(question, "cue_bullets"));
            questionDO.setDisplayOrder(intValue(question, "display_order", 0));
            questionDO.setPrepareSeconds(nullableInt(question, "prepare_seconds"));
            questionDO.setAnswerSeconds(nullableInt(question, "answer_seconds"));
            questionDO.setMetadata(mapValue(question, "metadata"));
            return questionDO;
        });
    }

    private Map<String, Object> mapValue(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return Map.of();
        }
        return objectMapper.convertValue(value, MAP_TYPE);
    }

    private List<Object> listValue(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return List.of();
        }
        return objectMapper.convertValue(value, OBJECT_LIST_TYPE);
    }

    private static String text(JsonNode node, String field) {
        return text(node, field, "");
    }

    private static String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node == null ? null : node.get(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return defaultValue;
        }
        return value.asText(defaultValue);
    }

    private static Integer nullableInt(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    private static int intValue(JsonNode node, String field, int defaultValue) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isMissingNode() || value.isNull() ? defaultValue : value.asInt(defaultValue);
    }

    private static boolean bool(JsonNode node, String field, boolean defaultValue) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isMissingNode() || value.isNull() ? defaultValue : value.asBoolean(defaultValue);
    }

    private static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    private static String normalizeSeasonKey(String seasonKey) {
        return seasonKey == null ? "" : seasonKey.trim().toUpperCase(Locale.ROOT);
    }

    private record Snapshot(Path path, JsonNode manifest) {
    }

    private record Count(int topics, int questions) {
    }

}
