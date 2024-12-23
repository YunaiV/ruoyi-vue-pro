package cn.iocoder.yudao.module.ai.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.enums.knowledge.AiKnowledgeDocumentStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库-文档 DO
 *
 * @author xiaoxin
 */
@TableName(value = "ai_knowledge_document")
@KeySequence("ai_knowledge_document_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 文档 token 数量
     */
    private Integer tokens;
    /**
     * 文档字符数
     */
    private Integer wordCount;


    // ========== 自定义分段所用参数 ==========
    // TODO @新：3）defaultChunkSize、defaultChunkSize、minChunkSizeChars、maxNumChunks 这几个字段的命名，可能要微信一起讨论下。尽量命名保持风格统一哈。
    /**
     * 每个文本块的目标 token 数
     */
    private Integer defaultSegmentTokens;
    /**
     * 每个文本块的最小字符数
     */
    private Integer minSegmentWordCount;
    /**
     * 低于此值的块会被丢弃
     */
    private Integer minChunkLengthToEmbed;
    /**
     * 最大块数
     */
    private Integer maxNumSegments;
    /**
     * 分块是否保留分隔符
     */
    private Boolean keepSeparator;
    // ===================================

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
