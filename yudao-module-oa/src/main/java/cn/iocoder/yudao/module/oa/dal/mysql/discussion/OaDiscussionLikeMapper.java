package cn.iocoder.yudao.module.oa.dal.mysql.discussion;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionLikeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 讨论点赞 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaDiscussionLikeMapper extends BaseMapperX<OaDiscussionLikeDO> {

    default List<OaDiscussionLikeDO> selectListByDiscussionIds(Collection<Long> discussionIds) {
        return selectList(new LambdaQueryWrapperX<OaDiscussionLikeDO>()
                .in(OaDiscussionLikeDO::getDiscussionId, discussionIds));
    }

    default List<OaDiscussionLikeDO> selectListByReplyIds(Collection<Long> replyIds) {
        return selectList(new LambdaQueryWrapperX<OaDiscussionLikeDO>().in(OaDiscussionLikeDO::getReplyId, replyIds));
    }

    default OaDiscussionLikeDO selectByTargetAndUserId(Long discussionId, Long replyId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<OaDiscussionLikeDO>()
                .eqIfPresent(OaDiscussionLikeDO::getDiscussionId, discussionId)
                .eqIfPresent(OaDiscussionLikeDO::getReplyId, replyId)
                .eq(OaDiscussionLikeDO::getUserId, userId));
    }

    default void deleteByDiscussionId(Long discussionId) {
        delete(OaDiscussionLikeDO::getDiscussionId, discussionId);
    }

    default void deleteByReplyIds(Collection<Long> replyIds) {
        delete(new LambdaQueryWrapperX<OaDiscussionLikeDO>().in(OaDiscussionLikeDO::getReplyId, replyIds));
    }

}
