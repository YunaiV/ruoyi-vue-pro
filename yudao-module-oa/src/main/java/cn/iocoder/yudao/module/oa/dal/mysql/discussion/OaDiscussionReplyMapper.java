package cn.iocoder.yudao.module.oa.dal.mysql.discussion;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 讨论回复 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaDiscussionReplyMapper extends BaseMapperX<OaDiscussionReplyDO> {

    default PageResult<OaDiscussionReplyDO> selectPage(OaDiscussionReplyPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<OaDiscussionReplyDO>()
                .eq(OaDiscussionReplyDO::getDiscussionId, pageReqVO.getDiscussionId())
                .eq(OaDiscussionReplyDO::getParentId, OaDiscussionReplyDO.PARENT_ID_ROOT)
                .eqIfPresent(OaDiscussionReplyDO::getUserId, pageReqVO.getUserId())
                .orderByAsc(OaDiscussionReplyDO::getId));
    }

    default List<OaDiscussionReplyDO> selectListByDiscussionIds(Collection<Long> discussionIds) {
        return selectList(new LambdaQueryWrapperX<OaDiscussionReplyDO>()
                .in(OaDiscussionReplyDO::getDiscussionId, discussionIds)
                .orderByAsc(OaDiscussionReplyDO::getCreateTime)
                .orderByAsc(OaDiscussionReplyDO::getId));
    }

    default void deleteByDiscussionId(Long discussionId) {
        delete(OaDiscussionReplyDO::getDiscussionId, discussionId);
    }

}
