package cn.iocoder.yudao.module.ai1.dal.dataobject.agent;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.List;

/**
 * AI1 Agent DO
 *
 * @author 芋道源码
 */
@TableName(value = "ai1_agent", autoResultMap = true)
@KeySequence("ai1_agent_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1AgentDO extends TenantBaseDO {

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
     * 介绍
     */
    private String introduction;
    /**
     * 模型所属 Provider 编号
     *
     * 关联 {@link Ai1ProviderDO#getId()}
     */
    private Long providerId;
    /**
     * 对话模型编号
     *
     * 关联 {@link Ai1ModelDO#getId()}
     */
    private Long modelId;
    /**
     * 系统指令
     */
    private String systemPrompt;
    /**
     * 知识库编号集合
     *
     * 关联 {@link Ai1KnowledgeBaseDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> knowledgeBaseIds;
    /**
     * MCP 编号集合
     *
     * 关联 {@link Ai1McpDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> mcpIds;
    /**
     * SKILL 编号集合
     *
     * 关联 {@link Ai1SkillDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> skillIds;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}；开启后才可在「Agent 对话」中使用
     */
    private Integer status;

}
