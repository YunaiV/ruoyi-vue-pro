package cn.iocoder.yudao.module.ai.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库-文档分段 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_knowledge_segment")
@Data
public class AiKnowledgeSegmentDO extends BaseDO {

    public static final String FIELD_KNOWLEDGE_ID = "knowledgeId";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 向量库的编号
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String vectorId;
    /**
     * 知识库编号
     * <p>
     * 关联 {@link AiKnowledgeDO#getId()}
     */
    private Long knowledgeId;
    /**
     * 文档编号
     * <p>
     * 关联 {@link AiKnowledgeDocumentDO#getId()}
     */
    private Long documentId;
    /**
     * 切片内容
     */
    private String content;
    /**
     * 字符数
     */
    private Integer wordCount;
    /**
     * token 数量
     */
    private Integer tokens;
    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
