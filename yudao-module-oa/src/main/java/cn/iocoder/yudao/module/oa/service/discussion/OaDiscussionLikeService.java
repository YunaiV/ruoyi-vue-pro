package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionLikeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * OA 讨论点赞 Service 接口
 *
 * @author 芋道源码
 */
public interface OaDiscussionLikeService {

    /**
     * 点赞讨论或主回复，已有点赞时不重复新增
     *
     * @param discussionId 讨论编号
     * @param replyId 回复编号
     * @param userId 用户编号
     */
    void createDiscussionLike(Long discussionId, Long replyId, Long userId);

    /**
     * 取消讨论或主回复点赞，没有点赞时无需处理
     *
     * @param discussionId 讨论编号
     * @param replyId 回复编号
     * @param userId 用户编号
     */
    void deleteDiscussionLike(Long discussionId, Long replyId, Long userId);

    /**
     * 获得讨论点赞列表
     *
     * @param discussionIds 讨论编号集合
     * @return 讨论点赞列表
     */
    List<OaDiscussionLikeDO> getDiscussionLikeList(Collection<Long> discussionIds);

    /**
     * 获得回复点赞列表
     *
     * @param replyIds 回复编号集合
     * @return 回复点赞列表
     */
    List<OaDiscussionLikeDO> getReplyLikeList(Collection<Long> replyIds);

    /**
     * 删除讨论点赞，供讨论删除时级联调用
     *
     * @param discussionId 讨论编号
     */
    void deleteDiscussionLikesByDiscussionId(Long discussionId);

    /**
     * 删除回复点赞，供回复删除时级联调用
     *
     * @param replyIds 回复编号集合
     */
    void deleteDiscussionLikesByReplyIds(Collection<Long> replyIds);

    /**
     * 获得回复点赞 Map
     *
     * @param replyIds 回复编号集合
     * @return 回复编号与点赞列表的 Map
     */
    default Map<Long, List<OaDiscussionLikeDO>> getReplyLikeListMap(Collection<Long> replyIds) {
        return convertMultiMap(getReplyLikeList(replyIds), OaDiscussionLikeDO::getReplyId);
    }

}
