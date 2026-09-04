package cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 知识库文档评论 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_document_comment")
@KeySequence("pms_knowledge_document_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeDocumentCommentDO extends BaseDO {

    /**
     * 主评论编号
     */
    public static final Long MAIN_ID_ROOT = 0L;

    /**
     * 评论编号
     */
    @TableId
    private Long id;
    /**
     * 文档编号
     *
     * 关联 {@link PmsKnowledgeDocumentDO#getId()}
     */
    private Long documentId;
    /**
     * 评论人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 主评论编号，0 表示主评论
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
