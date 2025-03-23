package cn.iocoder.yudao.module.ai.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库-文档分段 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_knowledge_segment")
@KeySequence("ai_knowledge_segment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiKnowledgeSegmentDO extends BaseDO {

    /**
     * 向量库的编号 - 空值
     */
    public static final String VECTOR_ID_EMPTY = "";

    /**
     * 编号
     */
    @TableId
    private Long id;
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
     * 切片内容长度
     */
    private Integer contentLength;

    /**
     * 向量库的编号
     */
    private String vectorId;
    /**
     * token 数量
     */
    private Integer tokens;

    /**
     * 召回次数
     */
    private Integer retrievalCount;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
