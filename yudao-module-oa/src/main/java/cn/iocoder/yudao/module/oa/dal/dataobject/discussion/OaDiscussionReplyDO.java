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
 * OA 讨论回复 DO
 *
 * @author 芋道源码
 */
@TableName("oa_discussion_reply")
@KeySequence("oa_discussion_reply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaDiscussionReplyDO extends BaseDO {

    /**
     * 主回复的父回复编号
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 回复编号
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
     * 回复人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 父回复编号
     *
     * 关联 {@link #getId()}
     */
    private Long parentId;
    /**
     * 被回复人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long replyUserId;
    /**
     * 回复内容
     */
    private String content;

}
