package cn.iocoder.yudao.module.yaya.service.content;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaContentSeasonDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaImportBatchDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeQuestionDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeTopicDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaContentSeasonMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaImportBatchMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeQuestionMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeTopicMapper;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(YayaImportServiceImpl.class)
class YayaImportServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaImportServiceImpl importService;
    @Resource
    private YayaContentSeasonMapper seasonMapper;
    @Resource
    private YayaPracticeTopicMapper topicMapper;
    @Resource
    private YayaPracticeQuestionMapper questionMapper;
    @Resource
    private YayaImportBatchMapper importBatchMapper;
    @Resource
    private DataSource dataSource;

    @TempDir
    private Path tempDir;

    @Test
    void previewImportShouldReadNewestSnapshotManifest() throws IOException {
        writeSnapshot("20260528-100000", 1, 1);
        writeSnapshot("20260528-110000", 2, 3);
        ReflectionTestUtils.setField(importService, "snapshotRoot", tempDir);

        YayaImportResultResp result = importService.previewImport("26Q1");

        assertEquals("26Q1", result.getSeasonKey());
        assertEquals(2, result.getTopics());
        assertEquals(3, result.getQuestions());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void previewImportShouldNormalizeSeasonKey() throws IOException {
        writeSnapshot("20260528-110000", 2, 3);
        ReflectionTestUtils.setField(importService, "snapshotRoot", tempDir);

        YayaImportResultResp result = importService.previewImport("26q1");

        assertEquals("26Q1", result.getSeasonKey());
        assertEquals(2, result.getTopics());
        assertEquals(3, result.getQuestions());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void runImportShouldUpsertTopicsReplaceQuestionsAndWriteBatch() throws IOException {
        writeSnapshot("20260528-110000", 1, 2);
        ReflectionTestUtils.setField(importService, "snapshotRoot", tempDir);

        YayaImportResultResp first = importService.runImport("26Q1");

        assertEquals(1, first.getTopics());
        assertEquals(2, first.getQuestions());
        YayaContentSeasonDO season = seasonMapper.selectBySeasonKey("26Q1");
        assertEquals("26Q1 IELTS Speaking", season.getName());
        YayaPracticeTopicDO topic = topicMapper.selectBySeasonPartStableKey(season.getId(), 1, "26q1-part1-work");
        assertEquals("legacy-topic-1", topic.getLegacyUuid());
        assertEquals("Work", topic.getTitleEn());
        assertEquals("draft", topic.getPublishStatus());
        List<YayaPracticeQuestionDO> questions = questionMapper.selectListByTopicId(topic.getId());
        assertEquals(2, questions.size());
        assertEquals("legacy-question-1", questions.get(0).getLegacyUuid());
        assertEquals("What do you do?", questions.get(0).getPromptEn());
        assertEquals(1, importBatchMapper.selectCount().intValue());

        writeSnapshotWithReplacementQuestion("20260528-120000");
        YayaImportResultResp second = importService.runImport("26Q1");

        assertEquals(1, second.getTopics());
        assertEquals(1, second.getQuestions());
        YayaPracticeTopicDO updated = topicMapper.selectBySeasonPartStableKey(season.getId(), 1, "26q1-part1-work");
        assertEquals(topic.getId(), updated.getId());
        assertEquals("Work Updated", updated.getTitleEn());
        List<YayaPracticeQuestionDO> replacedQuestions = questionMapper.selectListByTopicId(topic.getId());
        assertEquals(1, replacedQuestions.size());
        assertEquals("Replacement question?", replacedQuestions.get(0).getPromptEn());
        assertEquals(1, countAllQuestions());
        assertEquals(2, importBatchMapper.selectCount().intValue());
        YayaImportBatchDO batch = importBatchMapper.selectList().stream()
                .filter(importBatch -> "20260528-120000".equals(importBatch.getSummary().get("snapshot")))
                .findFirst()
                .orElseThrow();
        assertEquals("completed", batch.getStatus());
        assertEquals(1, batch.getSummary().get("topics"));
        assertEquals(1, batch.getSummary().get("questions"));
        assertEquals(1, batch.getSummary().get("updatedTopics"));
    }

    @Test
    void runImportShouldNotWriteDatabaseStateWhenRequiredSnapshotFileIsMissing() throws IOException {
        writeSnapshot("20260528-110000", 1, 2);
        Files.delete(tempDir.resolve("20260528-110000").resolve("practice_questions.json"));
        ReflectionTestUtils.setField(importService, "snapshotRoot", tempDir);

        YayaImportResultResp result = importService.runImport("26Q1");

        assertEquals("26Q1", result.getSeasonKey());
        assertEquals(0, result.getTopics());
        assertEquals(0, result.getQuestions());
        assertEquals(1, result.getErrors().size());
        assertEquals(0, seasonMapper.selectCount().intValue());
        assertEquals(0, topicMapper.selectCount().intValue());
        assertEquals(0, importBatchMapper.selectCount().intValue());
        assertEquals(0, countAllQuestions());
    }

    @Test
    void runImportShouldRejectQuestionsWithoutMatchingTopic() throws IOException {
        writeSnapshot("20260528-110000", 1, 1);
        Files.writeString(tempDir.resolve("20260528-110000").resolve("practice_questions.json"), """
                [
                  {
                    "legacy_uuid": "legacy-question-orphan",
                    "topic_legacy_uuid": "missing-topic",
                    "season_key": "26Q1",
                    "question_role": "question",
                    "prompt_en": "This should not import",
                    "cue_bullets": [],
                    "display_order": 1,
                    "metadata": {}
                  }
                ]
                """);
        ReflectionTestUtils.setField(importService, "snapshotRoot", tempDir);

        YayaImportResultResp result = importService.runImport("26Q1");

        assertEquals(0, result.getTopics());
        assertEquals(0, result.getQuestions());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("missing-topic"));
        assertEquals(0, seasonMapper.selectCount().intValue());
        assertEquals(0, topicMapper.selectCount().intValue());
        assertEquals(0, importBatchMapper.selectCount().intValue());
        assertEquals(0, countAllQuestions());
    }

    private void writeSnapshot(String name, int topics, int questions) throws IOException {
        Path snapshot = tempDir.resolve(name);
        Files.createDirectories(snapshot);
        Files.writeString(snapshot.resolve("manifest.json"), """
                {
                  "season_counts": {
                    "26Q1": {"topics": %d, "questions": %d}
                  },
                  "table_counts": {
                    "practice_topics": %d,
                    "practice_questions": %d
                  }
                }
                """.formatted(topics, questions, topics, questions));
        Files.writeString(snapshot.resolve("content_modules.json"), "[]");
        Files.writeString(snapshot.resolve("content_seasons.json"), """
                [
                  {
                    "legacy_uuid": "legacy-season-1",
                    "season_key": "26Q1",
                    "name": "26Q1 IELTS Speaking",
                    "active": true,
                    "defaulted": false
                  }
                ]
                """);
        Files.writeString(snapshot.resolve("practice_topics.json"), """
                [
                  {
                    "legacy_uuid": "legacy-topic-1",
                    "season_legacy_uuid": "legacy-season-1",
                    "season_key": "26Q1",
                    "part": 1,
                    "stable_key": "26q1-part1-work",
                    "topic_no": 1,
                    "title_en": "Work",
                    "title_zh": "",
                    "topic_type": "必考话题",
                    "category": "work",
                    "prompt_en": "",
                    "prompt_zh": "",
                    "display_order": 1,
                    "review_status": "draft",
                    "publish_status": "draft",
                    "metadata": {"source_file": "part1_topics_2026-04.csv"}
                  }
                ]
                """);
        Files.writeString(snapshot.resolve("practice_questions.json"), """
                [
                  {
                    "legacy_uuid": "legacy-question-1",
                    "topic_legacy_uuid": "legacy-topic-1",
                    "season_key": "26Q1",
                    "question_role": "question",
                    "prompt_en": "What do you do?",
                    "prompt_zh": "",
                    "cue_bullets": [],
                    "display_order": 1,
                    "metadata": {}
                  },
                  {
                    "legacy_uuid": "legacy-question-2",
                    "topic_legacy_uuid": "legacy-topic-1",
                    "season_key": "26Q1",
                    "question_role": "question",
                    "prompt_en": "Do you like your work?",
                    "prompt_zh": "",
                    "cue_bullets": [],
                    "display_order": 2,
                    "metadata": {}
                  }
                ]
                """);
        writeEmptyOptionalFiles(snapshot);
    }

    private void writeSnapshotWithReplacementQuestion(String name) throws IOException {
        writeSnapshot(name, 1, 1);
        Path snapshot = tempDir.resolve(name);
        Files.writeString(snapshot.resolve("practice_topics.json"), """
                [
                  {
                    "legacy_uuid": "legacy-topic-1",
                    "season_legacy_uuid": "legacy-season-1",
                    "season_key": "26Q1",
                    "part": 1,
                    "stable_key": "26q1-part1-work",
                    "topic_no": 1,
                    "title_en": "Work Updated",
                    "display_order": 1,
                    "metadata": {"source_file": "part1_topics_2026-04.csv"}
                  }
                ]
                """);
        Files.writeString(snapshot.resolve("practice_questions.json"), """
                [
                  {
                    "legacy_uuid": "legacy-question-3",
                    "topic_legacy_uuid": "legacy-topic-1",
                    "season_key": "26Q1",
                    "question_role": "question",
                    "prompt_en": "Replacement question?",
                    "cue_bullets": [],
                    "display_order": 1,
                    "metadata": {}
                  }
                ]
                """);
    }

    private static void writeEmptyOptionalFiles(Path snapshot) throws IOException {
        for (String file : List.of("topic_relations.json", "tags.json", "topic_tags.json")) {
            Files.writeString(snapshot.resolve(file), "[]");
        }
    }

    private int countAllQuestions() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM yaya_practice_question")) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
