package cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 知识库 DO
 *
 * 向量存储在 Milvus 集合 {collectionPrefix}{id}（默认 knowledge_base_{id}）中，按知识库隔离
 *
 * @author 芋道源码
 */
@TableName("ai1_knowledge_base")
@KeySequence("ai1_knowledge_base_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1KnowledgeBaseDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 向量化 Provider 编号
     *
     * 关联 {@link Ai1ProviderDO#getId()}
     */
    private Long embeddingProviderId;
    /**
     * 向量化模型编号
     *
     * 关联 {@link Ai1ModelDO#getId()}
     */
    private Long embeddingModelId;
    /**
     * 分片大小，单位：字符
     */
    private Integer chunkSize;
    /**
     * 分片重叠，单位：字符
     */
    private Integer chunkOverlap;
    /**
     * 检索数量
     */
    private Integer topK;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
