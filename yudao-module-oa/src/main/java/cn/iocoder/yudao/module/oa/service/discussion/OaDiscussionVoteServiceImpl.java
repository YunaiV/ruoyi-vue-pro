package cn.iocoder.yudao.module.oa.service.discussion;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote.OaVoteOptionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteOptionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteRecordDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaVoteOptionMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaVoteRecordMapper;
import cn.iocoder.yudao.module.oa.enums.discussion.OaDiscussionTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_VOTE_DUPLICATE;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_VOTE_EXPIRED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_VOTE_INVALID;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_VOTE_OPTION_NOT_EXISTS;

/**
 * OA 讨论投票 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaDiscussionVoteServiceImpl implements OaDiscussionVoteService {

    @Resource
    private OaVoteOptionMapper voteOptionMapper;
    @Resource
    private OaVoteRecordMapper voteRecordMapper;
    @Resource
    @Lazy
    private OaDiscussionService discussionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voteDiscussion(Long discussionId, Collection<Long> optionIds, Long userId) {
        // 1.1 校验讨论存在
        OaDiscussionDO discussion = discussionService.validateDiscussionExists(discussionId);
        // 1.2 校验讨论类型为投票
        if (ObjectUtil.notEqual(OaDiscussionTypeEnum.VOTE.getType(), discussion.getType())) {
            throw exception(DISCUSSION_VOTE_INVALID);
        }
        // 1.3 校验投票时间
        LocalDateTime now = LocalDateTime.now();
        if (LocalDateTimeUtils.isNotBetween(discussion.getVoteStartTime(), discussion.getVoteEndTime(), now)) {
            throw exception(DISCUSSION_VOTE_EXPIRED);
        }
        // 1.4 校验单选投票只能选择一个选项
        Set<Long> distinctOptionIds = CollUtil.newHashSet(optionIds);
        if (Boolean.FALSE.equals(discussion.getVoteMultiple()) && distinctOptionIds.size() != 1) {
            throw exception(DISCUSSION_VOTE_INVALID);
        }
        // 1.5 校验投票选项属于当前讨论
        Set<Long> existingOptionIds = convertSet(voteOptionMapper.selectListByDiscussionIds(
                Collections.singleton(discussionId)), OaVoteOptionDO::getId);
        if (!existingOptionIds.containsAll(distinctOptionIds)) {
            throw exception(DISCUSSION_VOTE_OPTION_NOT_EXISTS);
        }
        // 1.6 单选只能提交一次；多选可以分次追加，但同一选项不能重复提交
        List<OaVoteRecordDO> existingVoteRecords =
                voteRecordMapper.selectListByDiscussionIdAndUserId(discussionId, userId);
        if (Boolean.FALSE.equals(discussion.getVoteMultiple()) && CollUtil.isNotEmpty(existingVoteRecords)) {
            throw exception(DISCUSSION_VOTE_DUPLICATE);
        }
        Set<Long> votedOptionIds = convertSet(existingVoteRecords, OaVoteRecordDO::getOptionId);
        if (!Collections.disjoint(distinctOptionIds, votedOptionIds)) {
            throw exception(DISCUSSION_VOTE_DUPLICATE);
        }

        // 2. 新增投票记录
        voteRecordMapper.insertBatch(convertList(distinctOptionIds, optionId -> new OaVoteRecordDO()
                .setDiscussionId(discussionId).setOptionId(optionId).setUserId(userId)));
    }

    @Override
    public List<OaVoteOptionDO> getVoteOptionList(Collection<Long> discussionIds) {
        return CollUtil.isEmpty(discussionIds) ? Collections.emptyList()
                : voteOptionMapper.selectListByDiscussionIds(discussionIds);
    }

    @Override
    public List<OaVoteRecordDO> getVoteRecordList(Collection<Long> discussionIds) {
        return CollUtil.isEmpty(discussionIds) ? Collections.emptyList()
                : voteRecordMapper.selectListByDiscussionIds(discussionIds);
    }

    @Override
    public void createVoteOptionList(Long discussionId, List<OaVoteOptionSaveReqVO> voteOptions) {
        if (CollUtil.isEmpty(voteOptions)) {
            return;
        }
        voteOptionMapper.insertBatch(convertList(voteOptions, voteOption ->
                BeanUtils.toBean(voteOption, OaVoteOptionDO.class).setDiscussionId(discussionId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussionVotesByDiscussionId(Long discussionId) {
        // 1. 删除投票记录
        voteRecordMapper.deleteByDiscussionId(discussionId);

        // 2. 删除投票选项
        voteOptionMapper.deleteByDiscussionId(discussionId);
    }

}
