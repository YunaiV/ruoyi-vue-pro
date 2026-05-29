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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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

        // Legacy filters must be applied before pagination; the IELTS season corpus is intentionally bounded.
        List<YayaAppPracticeTopicRespVO> list = convertList(topicMapper.selectAppPublishedList(reqVO, seasonId)
                        .stream()
                        .filter(topic -> topicTypeMatches(topic, reqVO.getTopicType()))
                        .filter(topic -> keywordMatches(topic, reqVO.getQ()))
                        .toList(),
                this::buildTopicResp);
        applyMemberOverlays(list, memberUserId);
        List<YayaAppPracticeTopicRespVO> filtered = list.stream()
                .filter(topic -> progressMatches(topic, reqVO.getProgressFilter()))
                .toList();
        return page(filtered, reqVO.getPageNo(), reqVO.getPageSize());
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
        resp.setCompletedQuestionIds(completedQuestionIds(memberUserId, topicId));
        return resp;
    }

    @Override
    public Long createFavorite(Long memberUserId, Long topicId) {
        return setFavorite(memberUserId, topicId, true);
    }

    @Override
    public Long setFavorite(Long memberUserId, Long topicId, Boolean active) {
        requireMember(memberUserId);
        validatePublishedTopic(topicId);
        YayaFavoriteDO existing = favoriteMapper.selectByMemberAndTopic(memberUserId, topicId);
        if (!Boolean.TRUE.equals(active)) {
            if (existing != null) {
                favoriteMapper.deleteByMemberAndTopic(memberUserId, topicId);
                return existing.getId();
            }
            return null;
        }
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
        validateQuestion(reqVO.getTopicId(), reqVO.getQuestionId());

        YayaPracticeAttemptDO attempt = new YayaPracticeAttemptDO();
        attempt.setMemberUserId(memberUserId);
        attempt.setTopicId(reqVO.getTopicId());
        attempt.setStatus("submitted");
        attempt.setAnswerText(defaultString(reqVO.getAnswerText()));
        attempt.setDurationSeconds(reqVO.getDurationSeconds());
        attempt.setMetadata(attemptMetadata(reqVO));
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

    private void validateQuestion(Long topicId, Long questionId) {
        if (questionId == null) {
            return;
        }
        YayaPracticeQuestionDO question = questionMapper.selectById(questionId);
        if (question == null || !topicId.equals(question.getTopicId())) {
            throw exception(YAYA_TOPIC_NOT_EXISTS);
        }
    }

    private void upsertTopicState(Long memberUserId, Long topicId, Long attemptId, Map<String, Object> metadata) {
        LocalDateTime now = LocalDateTime.now();
        YayaUserTopicStateDO existing = stateMapper.selectByMemberAndTopic(memberUserId, topicId);
        Map<String, Object> mergedMetadata = stateMetadata(existing == null ? null : existing.getMetadata(), metadata);
        if (existing == null) {
            YayaUserTopicStateDO state = new YayaUserTopicStateDO();
            state.setMemberUserId(memberUserId);
            state.setTopicId(topicId);
            state.setPracticed(true);
            state.setAttemptCount(1);
            state.setLastAttemptId(attemptId);
            state.setLastPracticedAt(now);
            state.setMetadata(mergedMetadata);
            try {
                stateMapper.insert(state);
            } catch (DuplicateKeyException ignored) {
                stateMapper.incrementByMemberAndTopic(memberUserId, topicId, attemptId, now, mergedMetadata);
            }
            return;
        }

        stateMapper.incrementByMemberAndTopic(memberUserId, topicId, attemptId, now, mergedMetadata);
    }

    private List<Long> completedQuestionIds(Long memberUserId, Long topicId) {
        if (memberUserId == null) {
            return List.of();
        }
        YayaUserTopicStateDO state = stateMapper.selectByMemberAndTopic(memberUserId, topicId);
        return state == null ? List.of() : completedQuestionIds(state.getMetadata());
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

    private static PageResult<YayaAppPracticeTopicRespVO> page(List<YayaAppPracticeTopicRespVO> list, Integer pageNo,
                                                               Integer pageSize) {
        int total = list.size();
        int size = pageSize == null ? 10 : Math.max(1, pageSize);
        int page = Math.max(1, pageNo == null ? 1 : pageNo);
        long requestedOffset = (long) (page - 1) * size;
        if (requestedOffset >= total) {
            return new PageResult<>(Collections.emptyList(), (long) total);
        }
        int fromIndex = (int) requestedOffset;
        int toIndex = Math.min(fromIndex + size, total);
        return new PageResult<>(list.subList(fromIndex, toIndex), (long) total);
    }

    private static boolean topicTypeMatches(YayaPracticeTopicDO topic, String topicType) {
        String value = normalize(topicType);
        if (value.isEmpty()) {
            return true;
        }
        String raw = normalize(topic.getTopicType());
        if ("new".equals(value) || value.contains("新题")) {
            return raw.contains("新题");
        }
        if ("retained".equals(value) || "old".equals(value) || value.contains("保留") || value.contains("旧题")) {
            return raw.contains("保留") || raw.contains("旧题");
        }
        return raw.contains(value);
    }

    private static boolean keywordMatches(YayaPracticeTopicDO topic, String query) {
        String value = normalize(query);
        if (value.isEmpty()) {
            return true;
        }
        return normalize(topic.getTitleEn()).contains(value)
                || normalize(topic.getTitleZh()).contains(value)
                || normalize(topic.getPromptEn()).contains(value)
                || normalize(topic.getPromptZh()).contains(value)
                || normalize(topic.getTopicType()).contains(value)
                || normalize(topic.getCategory()).contains(value);
    }

    private static boolean progressMatches(YayaAppPracticeTopicRespVO topic, String progressFilter) {
        String value = normalize(progressFilter);
        if (value.isEmpty()) {
            return true;
        }
        boolean favorite = Boolean.TRUE.equals(topic.getFavorite());
        boolean practiced = Boolean.TRUE.equals(topic.getPracticed());
        if ("favorite".equals(value) || "favorites".equals(value) || value.contains("收藏")) {
            return favorite;
        }
        if ("practiced".equals(value) || "done".equals(value) || "completed".equals(value) || value.contains("已练习")) {
            return practiced;
        }
        if ("unpracticed".equals(value) || "unstarted".equals(value) || value.contains("未练习")) {
            return !practiced;
        }
        return true;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, Object> attemptMetadata(YayaAppPracticeAttemptCreateReqVO reqVO) {
        Map<String, Object> metadata = new LinkedHashMap<>(defaultMap(reqVO.getMetadata()));
        if (reqVO.getQuestionId() != null) {
            metadata.put("question_id", reqVO.getQuestionId());
            metadata.put("completed_question_ids", List.of(reqVO.getQuestionId()));
        }
        return metadata;
    }

    private static Map<String, Object> stateMetadata(Map<String, Object> existingMetadata,
                                                     Map<String, Object> attemptMetadata) {
        Map<String, Object> metadata = new LinkedHashMap<>(defaultMap(existingMetadata));
        metadata.putAll(defaultMap(attemptMetadata));
        List<Long> completedQuestionIds = mergeCompletedQuestionIds(existingMetadata, attemptMetadata);
        if (!completedQuestionIds.isEmpty()) {
            metadata.put("completed_question_ids", completedQuestionIds);
        }
        return metadata;
    }

    private static List<Long> mergeCompletedQuestionIds(Map<String, Object> existingMetadata,
                                                        Map<String, Object> attemptMetadata) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>(completedQuestionIds(existingMetadata));
        ids.addAll(completedQuestionIds(attemptMetadata));
        return new ArrayList<>(ids);
    }

    private static List<Long> completedQuestionIds(Map<String, Object> metadata) {
        Object completed = defaultMap(metadata).get("completed_question_ids");
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (completed instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                Long id = longValue(value);
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        Long questionId = longValue(defaultMap(metadata).get("question_id"));
        if (questionId != null) {
            ids.add(questionId);
        }
        return new ArrayList<>(ids);
    }

    private static Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            return Long.valueOf(string);
        }
        return null;
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
