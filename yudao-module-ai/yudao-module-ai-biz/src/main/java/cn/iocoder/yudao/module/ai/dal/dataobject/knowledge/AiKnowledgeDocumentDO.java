package cn.iocoder.yudao.module.ai.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.enums.knowledge.AiKnowledgeDocumentStatusEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库-文档 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_knowledge_document")
@Data
public class AiKnowledgeDocumentDO extends BaseDO {

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
     * 文件名称
     */
    private String name;
    /**
     * 内容
     */
    private String content;
    /**
     * 文件 URL
     */
    private String url;
    /**
     * token 数量
     */
    private Integer tokens;
    /**
     * 字符数
     */
    private Integer wordCount;
    /**
     * 每个文本块的目标 token 数
     */
    private Integer defaultChunkSize;
    /**
     * 每个文本块的最小字符数
     */
    private Integer minChunkSizeChars;
    /**
     * 低于此值的块会被丢弃
     */
    private Integer minChunkLengthToEmbed;
    /**
     * 最大块数
     */
    private Integer maxNumChunks;
    /**
     * 分块是否保留分隔符
     */
    private Boolean keepSeparator;
    /**
     * 切片状态
     * <p>
     * 枚举 {@link AiKnowledgeDocumentStatusEnum}
     */
    private Integer sliceStatus;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
