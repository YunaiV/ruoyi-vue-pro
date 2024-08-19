package cn.iocoder.yudao.module.ai.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 向量库的编号
     */
    private String vectorId;
    // TODO @新：knowledgeId 加个，会方便点
    /**
     * 文档编号
     *
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
