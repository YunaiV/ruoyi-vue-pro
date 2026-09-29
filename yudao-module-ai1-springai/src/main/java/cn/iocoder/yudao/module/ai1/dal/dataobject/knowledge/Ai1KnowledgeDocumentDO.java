package cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.enums.knowledge.Ai1KnowledgeDocumentStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 知识文档 DO
 *
 * @author 芋道源码
 */
@TableName("ai1_knowledge_document")
@KeySequence("ai1_knowledge_document_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1KnowledgeDocumentDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 知识库编号
     *
     * 关联 {@link Ai1KnowledgeBaseDO#getId()}
     */
    private Long knowledgeBaseId;
    /**
     * 名称
     */
    private String name;
    /**
     * 内容
     */
    private String content;
    /**
     * 分片数量
     */
    private Integer chunkCount;
    /**
     * 向量化状态
     *
     * 枚举 {@link Ai1KnowledgeDocumentStatusEnum}
     */
    private Integer status;

}
