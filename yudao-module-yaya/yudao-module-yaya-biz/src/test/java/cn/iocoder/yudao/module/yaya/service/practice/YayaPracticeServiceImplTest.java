package cn.iocoder.yudao.module.yaya.service.practice;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaFavoriteDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaPracticeAttemptDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaUserTopicStateDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.practice.YayaFavoriteMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.practice.YayaPracticeAttemptMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.practice.YayaUserTopicStateMapper;
import cn.iocoder.yudao.module.yaya.service.content.YayaContentServiceImpl;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaContentSeasonCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionSaveReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicCreateReq;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_NOT_LOGIN;
import static org.junit.jupiter.api.Assertions.*;

@Import({YayaContentServiceImpl.class, YayaPracticeServiceImpl.class})
class YayaPracticeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaContentServiceImpl contentService;
    @Resource
    private YayaPracticeServiceImpl practiceService;
    @Resource
    private YayaFavoriteMapper favoriteMapper;
    @Resource
    private YayaPracticeAttemptMapper attemptMapper;
    @Resource
    private YayaUserTopicStateMapper stateMapper;

    @Test
    void getTopicPageShouldReturnOnlyPublishedTopicsForAnonymousUser() {
        Long seasonId = createSeason("26Q1");
        Long publishedTopicId = createTopic(seasonId, 1, "part1-work", "Work", "published");
        replaceQuestions(publishedTopicId);
        createTopic(seasonId, 1, "part1-home", "Home", "draft");
        createTopic(seasonId, 2, "part2-place", "Place", "published");

        YayaAppPracticeTopicPageReqVO reqVO = new YayaAppPracticeTopicPageReqVO().setSeason("26Q1").setPart(1);
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);

        PageResult<YayaAppPracticeTopicRespVO> page = practiceService.getTopicPage(reqVO, null);

        assertEquals(1L, page.getTotal());
        YayaAppPracticeTopicRespVO topic = page.getList().get(0);
        assertEquals(publishedTopicId, topic.getId());
        assertEquals("Work", topic.getTitleEn());
        assertFalse(topic.getFavorite());
        assertFalse(topic.getPracticed());
    }

    @Test
    void getTopicPageShouldOverlayFavoriteAndPracticedForMember() {
        Long seasonId = createSeason("26Q1");
        Long topicId = createTopic(seasonId, 1, "part1-work", "Work", "published");
        replaceQuestions(topicId);
        Long memberUserId = 10001L;
        Long favoriteId = practiceService.createFavorite(memberUserId, topicId);
        Long repeatedFavoriteId = practiceService.createFavorite(memberUserId, topicId);
        Long attemptId = practiceService.createAttempt(memberUserId, new YayaAppPracticeAttemptCreateReqVO()
                .setTopicId(topicId)
                .setAnswerText("I work as a designer.")
                .setDurationSeconds(42)
                .setMetadata(Map.of("client", "unit-test")));
        Long secondAttemptId = practiceService.createAttempt(memberUserId, new YayaAppPracticeAttemptCreateReqVO()
                .setTopicId(topicId)
                .setAnswerText("I also work with product teams."));

        YayaAppPracticeTopicPageReqVO reqVO = new YayaAppPracticeTopicPageReqVO().setSeason("26Q1").setPart(1);
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        PageResult<YayaAppPracticeTopicRespVO> page = practiceService.getTopicPage(reqVO, memberUserId);

        assertEquals(favoriteId, repeatedFavoriteId);
        assertEquals(1L, page.getTotal());
        assertTrue(page.getList().get(0).getFavorite());
        assertTrue(page.getList().get(0).getPracticed());
        YayaPracticeAttemptDO attempt = attemptMapper.selectById(attemptId);
        assertEquals("submitted", attempt.getStatus());
        assertEquals("I work as a designer.", attempt.getAnswerText());
        YayaUserTopicStateDO state = stateMapper.selectByMemberAndTopic(memberUserId, topicId);
        assertEquals(secondAttemptId, state.getLastAttemptId());
        assertEquals(2, state.getAttemptCount());
        assertTrue(state.getPracticed());
        assertEquals(1, favoriteMapper.selectCount().intValue());
    }

    @Test
    void getTopicDetailShouldIncludeQuestionsAndMemberOverlays() {
        Long seasonId = createSeason("26Q1");
        Long topicId = createTopic(seasonId, 1, "part1-work", "Work", "published");
        replaceQuestions(topicId);
        Long memberUserId = 10001L;
        practiceService.createFavorite(memberUserId, topicId);

        YayaAppPracticeTopicDetailRespVO detail = practiceService.getTopicDetail(topicId, memberUserId);

        assertEquals(topicId, detail.getId());
        assertEquals("part1-work", detail.getStableKey());
        assertTrue(detail.getFavorite());
        assertFalse(detail.getPracticed());
        assertEquals(2, detail.getQuestions().size());
        assertEquals("What do you do?", detail.getQuestions().get(0).getPromptEn());
    }

    @Test
    void createAttemptShouldRequireMemberUser() {
        Long seasonId = createSeason("26Q1");
        Long topicId = createTopic(seasonId, 1, "part1-work", "Work", "published");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> practiceService.createAttempt(null, new YayaAppPracticeAttemptCreateReqVO().setTopicId(topicId)));

        assertEquals(YAYA_MEMBER_NOT_LOGIN.getCode(), exception.getCode());
    }

    private Long createSeason(String seasonKey) {
        return contentService.createSeason(new YayaContentSeasonCreateReq()
                .setSeasonKey(seasonKey)
                .setName(seasonKey + " IELTS Speaking")
                .setActive(true)
                .setDefaulted(false));
    }

    private Long createTopic(Long seasonId, Integer part, String stableKey, String titleEn, String publishStatus) {
        return contentService.createTopic(new YayaTopicCreateReq()
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
                .setPublishStatus(publishStatus));
    }

    private void replaceQuestions(Long topicId) {
        contentService.replaceQuestions(topicId, List.of(
                new YayaQuestionSaveReq()
                        .setQuestionRole("question")
                        .setPromptEn("What do you do?")
                        .setPromptZh("")
                        .setCueBullets(List.of())
                        .setDisplayOrder(1),
                new YayaQuestionSaveReq()
                        .setQuestionRole("question")
                        .setPromptEn("Do you like your work?")
                        .setPromptZh("")
                        .setCueBullets(List.of())
                        .setDisplayOrder(2)
        ));
    }

}
