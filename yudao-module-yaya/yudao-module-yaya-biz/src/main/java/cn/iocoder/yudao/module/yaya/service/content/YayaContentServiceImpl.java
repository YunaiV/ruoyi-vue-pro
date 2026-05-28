package cn.iocoder.yudao.module.yaya.service.content;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaContentSeasonDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeQuestionDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeTopicDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaContentSeasonMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeQuestionMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.content.YayaPracticeTopicMapper;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaContentSeasonCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionSaveReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicCreateReq;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicDetailResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicListItemResp;
import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicPageReqVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.*;

@Service
@Validated
public class YayaContentServiceImpl implements YayaContentService {

    @Resource
    private YayaContentSeasonMapper seasonMapper;
    @Resource
    private YayaPracticeTopicMapper topicMapper;
    @Resource
    private YayaPracticeQuestionMapper questionMapper;

    @Override
    public Long createSeason(YayaContentSeasonCreateReq req) {
        validateSeasonKeyUnique(req.getSeasonKey());

        YayaContentSeasonDO season = new YayaContentSeasonDO();
        season.setSeasonKey(req.getSeasonKey());
        season.setName(req.getName());
        season.setActive(booleanToInt(req.getActive(), 1));
        season.setDefaulted(booleanToInt(req.getDefaulted(), 0));
        seasonMapper.insert(season);
        return season.getId();
    }

    @Override
    public Long createTopic(YayaTopicCreateReq req) {
        validateSeasonExists(req.getSeasonId());
        validateTopicStableKeyUnique(null, req.getSeasonId(), req.getPart(), req.getStableKey());

        YayaPracticeTopicDO topic = new YayaPracticeTopicDO();
        copyTopicFields(req, topic);
        topicMapper.insert(topic);
        return topic.getId();
    }

    @Override
    public void updateTopic(Long id, YayaTopicCreateReq req) {
        YayaPracticeTopicDO existing = validateTopicExists(id);
        Long seasonId = valueOr(req.getSeasonId(), existing.getSeasonId());
        Integer part = valueOr(req.getPart(), existing.getPart());
        String stableKey = valueOr(req.getStableKey(), existing.getStableKey());
        validateSeasonExists(seasonId);
        validateTopicStableKeyUnique(id, seasonId, part, stableKey);

        YayaPracticeTopicDO topic = new YayaPracticeTopicDO();
        topic.setId(id);
        mergeTopicFields(req, existing, topic);
        topicMapper.updateById(topic);
    }

    private void copyTopicFields(YayaTopicCreateReq req, YayaPracticeTopicDO topic) {
        topic.setLegacyUuid(req.getLegacyUuid());
        topic.setSeasonId(req.getSeasonId());
        topic.setSourceSnapshotId(req.getSourceSnapshotId());
        topic.setPart(req.getPart());
        topic.setStableKey(req.getStableKey());
        topic.setTopicNo(req.getTopicNo());
        topic.setTitleEn(defaultString(req.getTitleEn()));
        topic.setTitleZh(defaultString(req.getTitleZh()));
        topic.setTopicType(defaultString(req.getTopicType()));
        topic.setCategory(defaultString(req.getCategory()));
        topic.setPromptEn(defaultString(req.getPromptEn()));
        topic.setPromptZh(defaultString(req.getPromptZh()));
        topic.setDisplayOrder(defaultInt(req.getDisplayOrder()));
        topic.setReviewStatus(defaultString(req.getReviewStatus(), "draft"));
        topic.setPublishStatus(defaultString(req.getPublishStatus(), "draft"));
        topic.setMetadata(defaultMap(req.getMetadata()));
    }

    private void mergeTopicFields(YayaTopicCreateReq req, YayaPracticeTopicDO existing, YayaPracticeTopicDO topic) {
        topic.setLegacyUuid(valueOr(req.getLegacyUuid(), existing.getLegacyUuid()));
        topic.setSeasonId(valueOr(req.getSeasonId(), existing.getSeasonId()));
        topic.setSourceSnapshotId(valueOr(req.getSourceSnapshotId(), existing.getSourceSnapshotId()));
        topic.setPart(valueOr(req.getPart(), existing.getPart()));
        topic.setStableKey(valueOr(req.getStableKey(), existing.getStableKey()));
        topic.setTopicNo(valueOr(req.getTopicNo(), existing.getTopicNo()));
        topic.setTitleEn(valueOr(req.getTitleEn(), existing.getTitleEn()));
        topic.setTitleZh(valueOr(req.getTitleZh(), existing.getTitleZh()));
        topic.setTopicType(valueOr(req.getTopicType(), existing.getTopicType()));
        topic.setCategory(valueOr(req.getCategory(), existing.getCategory()));
        topic.setPromptEn(valueOr(req.getPromptEn(), existing.getPromptEn()));
        topic.setPromptZh(valueOr(req.getPromptZh(), existing.getPromptZh()));
        topic.setDisplayOrder(valueOr(req.getDisplayOrder(), existing.getDisplayOrder()));
        topic.setReviewStatus(valueOr(req.getReviewStatus(), existing.getReviewStatus()));
        topic.setPublishStatus(valueOr(req.getPublishStatus(), existing.getPublishStatus()));
        topic.setMetadata(valueOr(req.getMetadata(), existing.getMetadata()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceQuestions(Long topicId, List<YayaQuestionSaveReq> questions) {
        validateTopicExists(topicId);
        questionMapper.deleteByTopicId(topicId);
        if (CollUtil.isEmpty(questions)) {
            return;
        }
        questionMapper.insertBatch(convertList(questions, question -> {
            YayaPracticeQuestionDO questionDO = new YayaPracticeQuestionDO();
            questionDO.setLegacyUuid(question.getLegacyUuid());
            questionDO.setTopicId(topicId);
            questionDO.setQuestionRole(defaultString(question.getQuestionRole(), "question"));
            questionDO.setPromptEn(defaultString(question.getPromptEn()));
            questionDO.setPromptZh(defaultString(question.getPromptZh()));
            questionDO.setCueBullets(question.getCueBullets() == null ? Collections.emptyList() : question.getCueBullets());
            questionDO.setDisplayOrder(defaultInt(question.getDisplayOrder()));
            questionDO.setPrepareSeconds(question.getPrepareSeconds());
            questionDO.setAnswerSeconds(question.getAnswerSeconds());
            questionDO.setMetadata(defaultMap(question.getMetadata()));
            return questionDO;
        }));
    }

    @Override
    public PageResult<YayaTopicListItemResp> getTopicPage(YayaTopicPageReqVO req) {
        Long seasonId = null;
        if (req.getSeasonKey() != null) {
            YayaContentSeasonDO season = seasonMapper.selectBySeasonKey(req.getSeasonKey());
            if (season == null) {
                return PageResult.empty();
            }
            seasonId = season.getId();
        }
        PageResult<YayaPracticeTopicDO> page = topicMapper.selectPage(req, seasonId);
        return new PageResult<>(convertList(page.getList(), this::buildListItem), page.getTotal());
    }

    @Override
    public YayaTopicDetailResp getTopicDetail(Long id) {
        YayaPracticeTopicDO topic = validateTopicExists(id);
        YayaTopicDetailResp resp = new YayaTopicDetailResp();
        copyListFields(topic, resp);
        resp.setPromptEn(topic.getPromptEn());
        resp.setPromptZh(topic.getPromptZh());
        resp.setMetadata(topic.getMetadata());
        resp.setQuestions(convertList(questionMapper.selectListByTopicId(id), this::buildQuestionResp));
        return resp;
    }

    @Override
    public void updateTopicPublishStatus(Long id, String publishStatus) {
        validateTopicExists(id);
        YayaPracticeTopicDO update = new YayaPracticeTopicDO();
        update.setId(id);
        update.setPublishStatus(publishStatus);
        topicMapper.updateById(update);
    }

    private YayaContentSeasonDO validateSeasonExists(Long seasonId) {
        YayaContentSeasonDO season = seasonMapper.selectById(seasonId);
        if (season == null) {
            throw exception(YAYA_SEASON_NOT_EXISTS);
        }
        return season;
    }

    private YayaPracticeTopicDO validateTopicExists(Long topicId) {
        YayaPracticeTopicDO topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw exception(YAYA_TOPIC_NOT_EXISTS);
        }
        return topic;
    }

    private void validateSeasonKeyUnique(String seasonKey) {
        if (seasonMapper.selectBySeasonKey(seasonKey) != null) {
            throw exception(YAYA_SEASON_KEY_DUPLICATE);
        }
    }

    private void validateTopicStableKeyUnique(Long id, Long seasonId, Integer part, String stableKey) {
        YayaPracticeTopicDO topic = topicMapper.selectBySeasonPartStableKey(seasonId, part, stableKey);
        if (topic == null) {
            return;
        }
        if (id == null || !topic.getId().equals(id)) {
            throw exception(YAYA_TOPIC_STABLE_KEY_DUPLICATE);
        }
    }

    private YayaTopicListItemResp buildListItem(YayaPracticeTopicDO topic) {
        YayaTopicListItemResp resp = new YayaTopicListItemResp();
        copyListFields(topic, resp);
        return resp;
    }

    private void copyListFields(YayaPracticeTopicDO topic, YayaTopicListItemResp resp) {
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
        resp.setReviewStatus(topic.getReviewStatus());
        resp.setPublishStatus(topic.getPublishStatus());
        resp.setUpdateTime(topic.getUpdateTime());
    }

    private YayaQuestionResp buildQuestionResp(YayaPracticeQuestionDO question) {
        return new YayaQuestionResp()
                .setId(question.getId())
                .setLegacyUuid(question.getLegacyUuid())
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

    private static int booleanToInt(Boolean value, int defaultValue) {
        return value == null ? defaultValue : Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private static int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static String defaultString(String value) {
        return defaultString(value, "");
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static Map<String, Object> defaultMap(Map<String, Object> value) {
        return value == null ? Collections.emptyMap() : value;
    }

    private static <T> T valueOr(T value, T fallback) {
        return value == null ? fallback : value;
    }

}
