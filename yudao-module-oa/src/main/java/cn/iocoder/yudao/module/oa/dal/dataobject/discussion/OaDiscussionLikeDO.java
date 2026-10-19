package cn.iocoder.yudao.module.oa.dal.dataobject.discussion;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 讨论点赞 DO
 *
 * @author 芋道源码
 */
@TableName("oa_discussion_like")
@KeySequence("oa_discussion_like_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaDiscussionLikeDO extends BaseDO {

    /**
     * 点赞记录编号
     */
    @TableId
    private Long id;
    /**
     * 讨论编号
     *
     * 关联 {@link OaDiscussionDO#getId()}
     */
    private Long discussionId;
    /**
     * 回复编号
     *
     * 关联 {@link OaDiscussionReplyDO#getId()}
     */
    private Long replyId;
    /**
     * 点赞人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;

}
