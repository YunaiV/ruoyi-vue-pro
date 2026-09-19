package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyCreateReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply.OaDiscussionReplyPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionReplyDO;

import java.util.Collection;
import java.util.List;

/**
 * OA 讨论回复 Service 接口
 *
 * @author 芋道源码
 */
public interface OaDiscussionReplyService {

    /**
     * 创建讨论回复
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 回复编号
     */
    Long createDiscussionReply(OaDiscussionReplyCreateReqVO createReqVO, Long userId);

    /**
     * 删除讨论回复
     *
     * @param id 回复编号
     * @param userId 用户编号
     */
    void deleteDiscussionReply(Long id, Long userId);

    /**
     * 获得讨论回复列表
     *
     * @param discussionIds 讨论编号集合
     * @return 讨论回复列表
     */
    List<OaDiscussionReplyDO> getDiscussionReplyList(Collection<Long> discussionIds);

    /**
     * 获得讨论回复分页
     *
     * @param pageReqVO 分页查询
     * @return 讨论回复分页
     */
    PageResult<OaDiscussionReplyDO> getDiscussionReplyPage(OaDiscussionReplyPageReqVO pageReqVO);

    /**
     * 获得主回复下的全部子回复
     *
     * @param replies 当前页的主回复
     * @return 子回复列表，按创建时间排序
     */
    List<OaDiscussionReplyDO> getDiscussionReplyDescendantList(List<OaDiscussionReplyDO> replies);

    /**
     * 校验讨论回复存在
     *
     * @param id 回复编号
     * @return 讨论回复
     */
    OaDiscussionReplyDO validateDiscussionReplyExists(Long id);

    /**
     * 删除讨论下的回复及回复点赞，供讨论删除时级联调用
     *
     * @param discussionId 讨论编号
     */
    void deleteDiscussionRepliesByDiscussionId(Long discussionId);

}
