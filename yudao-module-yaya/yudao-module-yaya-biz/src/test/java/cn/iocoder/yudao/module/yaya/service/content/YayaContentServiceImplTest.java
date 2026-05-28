package cn.iocoder.yudao.module.yaya.service.content;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaContentSeasonDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeQuestionDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeTopicDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaContentSeasonMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeQuestionMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeTopicMapper;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaContentSeasonCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionSaveReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicDetailResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicListItemResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicPageReqVO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_TOPIC_STABLE_KEY_DUPLICATE;
import static org.junit.jupiter.api.Assertions.*;

@Import(YayaContentServiceImpl.class)
class YayaContentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaContentServiceImpl contentService;
    @Resource
    private YayaContentSeasonMapper seasonMapper;
    @Resource
    private YayaPracticeTopicMapper topicMapper;
    @Resource
    private YayaPracticeQuestionMapper questionMapper;

    @Test
    void createSeasonShouldPersist26Q1() {
        YayaContentSeasonCreateReq req = new YayaContentSeasonCreateReq()
                .setSeasonKey("26Q1")
                .setName("2026 Q1 IELTS Speaking")
                .setActive(true)
                .setDefaulted(true);

        Long seasonId = contentService.createSeason(req);

        YayaContentSeasonDO season = seasonMapper.selectById(seasonId);
        assertNotNull(season);
        assertEquals("26Q1", season.getSeasonKey());
        assertEquals("2026 Q1 IELTS Speaking", season.getName());
        assertEquals(1, season.getActive());
        assertEquals(1, season.getDefaulted());
    }

    @Test
    void createTopicAndReplaceQuestionsShouldPersistTwoQuestions() {
        Long seasonId = createSeason("26Q1");
        YayaTopicCreateReq topicReq = topicReq(seasonId, 1, "part1-work", "Work");

        Long topicId = contentService.createTopic(topicReq);
        contentService.replaceQuestions(topicId, List.of(
                questionReq("What do you do?", 1),
                questionReq("Why did you choose it?", 2)
        ));

        YayaPracticeTopicDO topic = topicMapper.selectById(topicId);
        assertEquals("part1-work", topic.getStableKey());
        List<YayaPracticeQuestionDO> questions = questionMapper.selectListByTopicId(topicId);
        assertEquals(2, questions.size());
        assertEquals("What do you do?", questions.get(0).getPromptEn());
        assertEquals("Why did you choose it?", questions.get(1).getPromptEn());
    }

    @Test
    void getTopicPageShouldListPublishedTopicsBySeasonAndPart() {
        Long season26Q1 = createSeason("26Q1");
        Long season26Q2 = createSeason("26Q2");
        contentService.createTopic(topicReq(season26Q1, 1, "published-work", "Work").setPublishStatus("published"));
        contentService.createTopic(topicReq(season26Q1, 2, "published-place", "Place").setPublishStatus("published"));
        contentService.createTopic(topicReq(season26Q2, 1, "published-study", "Study").setPublishStatus("published"));
        contentService.createTopic(topicReq(season26Q1, 1, "draft-home", "Home").setPublishStatus("draft"));

        YayaTopicPageReqVO req = new YayaTopicPageReqVO()
                .setSeasonKey("26Q1")
                .setPart(1)
                .setPublishStatus("published");
        req.setPageNo(1);
        req.setPageSize(10);

        PageResult<YayaTopicListItemResp> page = contentService.getTopicPage(req);

        assertEquals(1L, page.getTotal());
        assertEquals("published-work", page.getList().get(0).getStableKey());
        assertEquals("Work", page.getList().get(0).getTitleEn());
    }

    @Test
    void createTopicShouldRejectDuplicateSeasonPartStableKey() {
        Long seasonId = createSeason("26Q1");
        contentService.createTopic(topicReq(seasonId, 1, "part1-work", "Work"));

        assertServiceException(
                () -> contentService.createTopic(topicReq(seasonId, 1, "part1-work", "Work duplicate")),
                YAYA_TOPIC_STABLE_KEY_DUPLICATE);
    }

    @Test
    void getTopicDetailShouldIncludeQuestions() {
        Long seasonId = createSeason("26Q1");
        Long topicId = contentService.createTopic(topicReq(seasonId, 1, "part1-work", "Work"));
        contentService.replaceQuestions(topicId, List.of(
                questionReq("What do you do?", 1),
                questionReq("Do you like your work?", 2)
        ));

        YayaTopicDetailResp detail = contentService.getTopicDetail(topicId);

        assertEquals(topicId, detail.getId());
        assertEquals("part1-work", detail.getStableKey());
        assertEquals(2, detail.getQuestions().size());
    }

    private Long createSeason(String seasonKey) {
        return contentService.createSeason(new YayaContentSeasonCreateReq()
                .setSeasonKey(seasonKey)
                .setName(seasonKey + " IELTS Speaking")
                .setActive(true)
                .setDefaulted(false));
    }

    private static YayaTopicCreateReq topicReq(Long seasonId, Integer part, String stableKey, String titleEn) {
        return new YayaTopicCreateReq()
                .setSeasonId(seasonId)
                .setPart(part)
                .setStableKey(stableKey)
                .setTopicNo(1)
                .setTitleEn(titleEn)
                .setTitleZh("")
                .setTopicType("必考话题")
                .setCategory("work")
                .setPromptEn("")
                .setPromptZh("")
                .setDisplayOrder(1)
                .setReviewStatus("draft")
                .setPublishStatus("draft");
    }

    private static YayaQuestionSaveReq questionReq(String promptEn, Integer displayOrder) {
        return new YayaQuestionSaveReq()
                .setQuestionRole("question")
                .setPromptEn(promptEn)
                .setPromptZh("")
                .setCueBullets(List.of())
                .setDisplayOrder(displayOrder);
    }

}
