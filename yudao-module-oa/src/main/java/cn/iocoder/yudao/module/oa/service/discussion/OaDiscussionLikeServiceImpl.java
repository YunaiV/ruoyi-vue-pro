package cn.iocoder.yudao.module.oa.service.discussion;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionLikeDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.discussion.OaDiscussionLikeMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.DISCUSSION_LIKE_TARGET_INVALID;

/**
 * OA 讨论点赞 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaDiscussionLikeServiceImpl implements OaDiscussionLikeService {

    @Resource
    private OaDiscussionLikeMapper discussionLikeMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionService discussionService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaDiscussionReplyService discussionReplyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDiscussionLike(Long discussionId, Long replyId, Long userId) {
        // 1.1 校验点赞对象存在
        validateLikeTargetExists(discussionId, replyId);
        // 1.2 已点赞时直接返回，避免重试重复新增
        if (discussionLikeMapper.selectByTargetAndUserId(discussionId, replyId, userId) != null) {
            return;
        }

        // 2. 新增点赞
        discussionLikeMapper.insert(new OaDiscussionLikeDO().setDiscussionId(discussionId)
                .setReplyId(replyId).setUserId(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussionLike(Long discussionId, Long replyId, Long userId) {
        // 1. 查询当前用户的点赞，没有点赞时无需处理
        OaDiscussionLikeDO like = discussionLikeMapper.selectByTargetAndUserId(discussionId, replyId, userId);
        if (like == null) {
            return;
        }

        // 2. 删除当前用户的点赞
        discussionLikeMapper.deleteById(like.getId());
    }

    @Override
    public List<OaDiscussionLikeDO> getDiscussionLikeList(Collection<Long> discussionIds) {
        return CollUtil.isEmpty(discussionIds) ? Collections.emptyList()
                : discussionLikeMapper.selectListByDiscussionIds(discussionIds);
    }

    @Override
    public List<OaDiscussionLikeDO> getReplyLikeList(Collection<Long> replyIds) {
        return CollUtil.isEmpty(replyIds) ? Collections.emptyList()
                : discussionLikeMapper.selectListByReplyIds(replyIds);
    }

    @Override
    public void deleteDiscussionLikesByDiscussionId(Long discussionId) {
        discussionLikeMapper.deleteByDiscussionId(discussionId);
    }

    @Override
    public void deleteDiscussionLikesByReplyIds(Collection<Long> replyIds) {
        if (CollUtil.isEmpty(replyIds)) {
            return;
        }
        discussionLikeMapper.deleteByReplyIds(replyIds);
    }

    /**
     * 校验讨论或主回复存在
     *
     * @param discussionId 讨论编号
     * @param replyId 回复编号
     */
    private void validateLikeTargetExists(Long discussionId, Long replyId) {
        if (discussionId != null) {
            discussionService.validateDiscussionExists(discussionId);
        } else {
            OaDiscussionReplyDO reply = discussionReplyService.validateDiscussionReplyExists(replyId);
            if (ObjectUtil.notEqual(reply.getParentId(), OaDiscussionReplyDO.PARENT_ID_ROOT)) {
                throw exception(DISCUSSION_LIKE_TARGET_INVALID);
            }
        }
    }

}
