package cn.iocoder.yudao.module.yaya.service.practice;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeAttemptCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeQuestionRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicDetailRespVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicPageReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.practice.vo.YayaAppPracticeTopicRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaContentSeasonDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeQuestionDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeTopicDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaFavoriteDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaPracticeAttemptDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaUserTopicStateDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaContentSeasonMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeQuestionMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeTopicMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.practice.YayaFavoriteMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.practice.YayaPracticeAttemptMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.practice.YayaUserTopicStateMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_MEMBER_NOT_LOGIN;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_TOPIC_NOT_EXISTS;

@Service
@Validated
public class YayaPracticeServiceImpl implements YayaPracticeService {

    private static final String PUBLISH_STATUS_PUBLISHED = "published";

    @Resource
    private YayaContentSeasonMapper seasonMapper;
    @Resource
    private YayaPracticeTopicMapper topicMapper;
    @Resource
    private YayaPracticeQuestionMapper questionMapper;
    @Resource
    private YayaFavoriteMapper favoriteMapper;
    @Resource
    private YayaPracticeAttemptMapper attemptMapper;
    @Resource
    private YayaUserTopicStateMapper stateMapper;

    @Override
    public PageResult<YayaAppPracticeTopicRespVO> getTopicPage(YayaAppPracticeTopicPageReqVO reqVO, Long memberUserId) {
        Long seasonId = getSeasonId(reqVO.getSeason());
        if (seasonId == null) {
            return PageResult.empty();
        }

        PageResult<YayaPracticeTopicDO> page = topicMapper.selectAppPublishedPage(reqVO, seasonId);
        List<YayaAppPracticeTopicRespVO> list = convertList(page.getList(), this::buildTopicResp);
        applyMemberOverlays(list, memberUserId);
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public YayaAppPracticeTopicDetailRespVO getTopicDetail(Long topicId, Long memberUserId) {
        YayaPracticeTopicDO topic = validatePublishedTopic(topicId);
        YayaAppPracticeTopicDetailRespVO resp = new YayaAppPracticeTopicDetailRespVO();
        copyTopicFields(topic, resp);
        resp.setPromptEn(topic.getPromptEn());
        resp.setPromptZh(topic.getPromptZh());
        resp.setMetadata(topic.getMetadata());
        resp.setQuestions(convertList(questionMapper.selectListByTopicId(topicId), this::buildQuestionResp));
        applyMemberOverlays(List.of(resp), memberUserId);
        return resp;
    }

    @Override
    public Long createFavorite(Long memberUserId, Long topicId) {
        requireMember(memberUserId);
        validatePublishedTopic(topicId);
        YayaFavoriteDO existing = favoriteMapper.selectByMemberAndTopic(memberUserId, topicId);
        if (existing != null) {
            return existing.getId();
        }

        YayaFavoriteDO favorite = new YayaFavoriteDO();
        favorite.setMemberUserId(memberUserId);
        favorite.setTopicId(topicId);
        try {
            favoriteMapper.insert(favorite);
            return favorite.getId();
        } catch (DuplicateKeyException ignored) {
            return favoriteMapper.selectByMemberAndTopic(memberUserId, topicId).getId();
        }
    }

    @Override
    public Long createAttempt(Long memberUserId, YayaAppPracticeAttemptCreateReqVO reqVO) {
        requireMember(memberUserId);
        validatePublishedTopic(reqVO.getTopicId());

        YayaPracticeAttemptDO attempt = new YayaPracticeAttemptDO();
        attempt.setMemberUserId(memberUserId);
        attempt.setTopicId(reqVO.getTopicId());
        attempt.setStatus("submitted");
        attempt.setAnswerText(defaultString(reqVO.getAnswerText()));
        attempt.setDurationSeconds(reqVO.getDurationSeconds());
        attempt.setMetadata(defaultMap(reqVO.getMetadata()));
        attemptMapper.insert(attempt);

        upsertTopicState(memberUserId, reqVO.getTopicId(), attempt.getId(), attempt.getMetadata());
        return attempt.getId();
    }

    private Long getSeasonId(String seasonKey) {
        if (seasonKey == null) {
            return null;
        }
        YayaContentSeasonDO season = seasonMapper.selectBySeasonKey(seasonKey.toUpperCase());
        return season == null ? null : season.getId();
    }

    private YayaPracticeTopicDO validatePublishedTopic(Long topicId) {
        YayaPracticeTopicDO topic = topicMapper.selectById(topicId);
        if (topic == null || !PUBLISH_STATUS_PUBLISHED.equals(topic.getPublishStatus())) {
            throw exception(YAYA_TOPIC_NOT_EXISTS);
        }
        return topic;
    }

    private void upsertTopicState(Long memberUserId, Long topicId, Long attemptId, Map<String, Object> metadata) {
        LocalDateTime now = LocalDateTime.now();
        YayaUserTopicStateDO existing = stateMapper.selectByMemberAndTopic(memberUserId, topicId);
        if (existing == null) {
            YayaUserTopicStateDO state = new YayaUserTopicStateDO();
            state.setMemberUserId(memberUserId);
            state.setTopicId(topicId);
            state.setPracticed(true);
            state.setAttemptCount(1);
            state.setLastAttemptId(attemptId);
            state.setLastPracticedAt(now);
            state.setMetadata(metadata);
            try {
                stateMapper.insert(state);
            } catch (DuplicateKeyException ignored) {
                stateMapper.incrementByMemberAndTopic(memberUserId, topicId, attemptId, now, metadata);
            }
            return;
        }

        stateMapper.incrementByMemberAndTopic(memberUserId, topicId, attemptId, now, metadata);
    }

    private void applyMemberOverlays(List<? extends YayaAppPracticeTopicRespVO> topics, Long memberUserId) {
        topics.forEach(topic -> {
            topic.setFavorite(false);
            topic.setPracticed(false);
        });
        if (memberUserId == null || CollUtil.isEmpty(topics)) {
            return;
        }

        List<Long> topicIds = topics.stream().map(YayaAppPracticeTopicRespVO::getId).toList();
        Set<Long> favoriteTopicIds = favoriteMapper.selectListByMemberAndTopicIds(memberUserId, topicIds).stream()
                .map(YayaFavoriteDO::getTopicId)
                .collect(Collectors.toSet());
        Map<Long, YayaUserTopicStateDO> stateByTopicId = stateMapper.selectListByMemberAndTopicIds(memberUserId, topicIds)
                .stream()
                .collect(Collectors.toMap(YayaUserTopicStateDO::getTopicId, Function.identity(), (left, right) -> left));
        topics.forEach(topic -> {
            topic.setFavorite(favoriteTopicIds.contains(topic.getId()));
            YayaUserTopicStateDO state = stateByTopicId.get(topic.getId());
            topic.setPracticed(state != null && Boolean.TRUE.equals(state.getPracticed()));
        });
    }

    private YayaAppPracticeTopicRespVO buildTopicResp(YayaPracticeTopicDO topic) {
        YayaAppPracticeTopicRespVO resp = new YayaAppPracticeTopicRespVO();
        copyTopicFields(topic, resp);
        return resp;
    }

    private void copyTopicFields(YayaPracticeTopicDO topic, YayaAppPracticeTopicRespVO resp) {
        resp.setId(topic.getId());
        resp.setSeasonId(topic.getSeasonId());
        resp.setPart(topic.getPart());
        resp.setStableKey(topic.getStableKey());
        resp.setTopicNo(topic.getTopicNo());
        resp.setTitleEn(topic.getTitleEn());
        resp.setTitleZh(topic.getTitleZh());
        resp.setTopicType(topic.getTopicType());
        resp.setCategory(topic.getCategory());
        resp.setDisplayOrder(topic.getDisplayOrder());
    }

    private YayaAppPracticeQuestionRespVO buildQuestionResp(YayaPracticeQuestionDO question) {
        return new YayaAppPracticeQuestionRespVO()
                .setId(question.getId())
                .setTopicId(question.getTopicId())
                .setQuestionRole(question.getQuestionRole())
                .setPromptEn(question.getPromptEn())
                .setPromptZh(question.getPromptZh())
                .setCueBullets(question.getCueBullets())
                .setDisplayOrder(question.getDisplayOrder())
                .setPrepareSeconds(question.getPrepareSeconds())
                .setAnswerSeconds(question.getAnswerSeconds())
                .setMetadata(question.getMetadata());
    }

    private void requireMember(Long memberUserId) {
        if (memberUserId == null) {
            throw exception(YAYA_MEMBER_NOT_LOGIN);
        }
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static Map<String, Object> defaultMap(Map<String, Object> value) {
        return value == null ? Collections.emptyMap() : value;
    }

}
