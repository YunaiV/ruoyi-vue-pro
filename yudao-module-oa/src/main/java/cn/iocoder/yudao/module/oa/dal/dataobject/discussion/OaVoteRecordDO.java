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
 * OA 投票记录 DO
 *
 * @author 芋道源码
 */
@TableName("oa_vote_record")
@KeySequence("oa_vote_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaVoteRecordDO extends BaseDO {

    /**
     * 投票记录编号
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
     * 投票选项编号
     *
     * 关联 {@link OaVoteOptionDO#getId()}
     */
    private Long optionId;
    /**
     * 投票人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;

}
