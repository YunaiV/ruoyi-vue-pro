package cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PMS 知识库文档分享 DO
 *
 * @author 芋道源码
 */
@TableName(value = "pms_knowledge_document_share", autoResultMap = true)
@KeySequence("pms_knowledge_document_share_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeDocumentShareDO extends BaseDO {

    /**
     * 分享编号
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
     * 内部分享成员用户编号列表
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> shareUserIds;
    /**
     * 外部查看令牌
     */
    private String token;
    /**
     * 分享状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 关闭人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long closeUserId;
    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

}
