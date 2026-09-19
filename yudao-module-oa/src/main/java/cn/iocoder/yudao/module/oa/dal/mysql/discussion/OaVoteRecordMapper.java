package cn.iocoder.yudao.module.oa.dal.mysql.discussion;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 投票记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaVoteRecordMapper extends BaseMapperX<OaVoteRecordDO> {

    default List<OaVoteRecordDO> selectListByDiscussionIds(Collection<Long> discussionIds) {
        return selectList(new LambdaQueryWrapperX<OaVoteRecordDO>()
                .in(OaVoteRecordDO::getDiscussionId, discussionIds));
    }

    default List<OaVoteRecordDO> selectListByDiscussionIdAndUserId(Long discussionId, Long userId) {
        return selectList(OaVoteRecordDO::getDiscussionId, discussionId, OaVoteRecordDO::getUserId, userId);
    }

    default void deleteByDiscussionId(Long discussionId) {
        delete(OaVoteRecordDO::getDiscussionId, discussionId);
    }

}
