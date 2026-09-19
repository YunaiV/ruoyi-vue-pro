package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote.OaVoteOptionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteOptionDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaVoteRecordDO;

import java.util.Collection;
import java.util.List;

/**
 * OA 讨论投票 Service 接口
 *
 * @author 芋道源码
 */
public interface OaDiscussionVoteService {

    /**
     * 参与讨论投票
     *
     * @param discussionId 讨论编号
     * @param optionIds 投票选项编号集合
     * @param userId 用户编号
     */
    void voteDiscussion(Long discussionId, Collection<Long> optionIds, Long userId);

    /**
     * 获得投票选项列表
     *
     * @param discussionIds 讨论编号集合
     * @return 投票选项列表
     */
    List<OaVoteOptionDO> getVoteOptionList(Collection<Long> discussionIds);

    /**
     * 获得投票记录列表
     *
     * @param discussionIds 讨论编号集合
     * @return 投票记录列表
     */
    List<OaVoteRecordDO> getVoteRecordList(Collection<Long> discussionIds);

    /**
     * 创建讨论投票选项
     *
     * @param discussionId 讨论编号
     * @param voteOptions 投票选项列表
     */
    void createVoteOptionList(Long discussionId, List<OaVoteOptionSaveReqVO> voteOptions);

    /**
     * 删除讨论投票记录和选项，供讨论删除时级联调用
     *
     * @param discussionId 讨论编号
     */
    void deleteDiscussionVotesByDiscussionId(Long discussionId);

}
