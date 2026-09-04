package cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 工作项评论 DO
 *
 * @author 芋道源码
 */
@TableName("pms_work_item_comment")
@KeySequence("pms_work_item_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkItemCommentDO extends BaseDO {

    /**
     * 主评论使用的根编号
     */
    public static final Long MAIN_ID_ROOT = 0L;

    /**
     * 评论编号
     */
    @TableId
    private Long id;
    /**
     * 工作项编号
     *
     * 关联 {@link PmsWorkItemDO#getId()}
     */
    private Long workItemId;
    /**
     * 评论人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 主评论编号，{@link #MAIN_ID_ROOT} 表示主评论
     *
     * 关联 {@link #getId()}
     */
    private Long mainId;
    /**
     * 回复对象用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long replyUserId;
    /**
     * 评论内容
     */
    private String content;

}
