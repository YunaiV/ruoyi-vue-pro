package cn.iocoder.yudao.module.oa.dal.dataobject.discussion;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 投票选项 DO
 *
 * @author 芋道源码
 */
@TableName("oa_vote_option")
@KeySequence("oa_vote_option_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaVoteOptionDO extends BaseDO {

    /**
     * 投票选项编号
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
     * 选项标题
     */
    private String title;
    /**
     * 选项颜色
     */
    private String color;
    /**
     * 显示顺序
     */
    private Integer sort;

}
