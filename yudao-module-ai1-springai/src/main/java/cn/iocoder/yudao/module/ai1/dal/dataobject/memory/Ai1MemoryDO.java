package cn.iocoder.yudao.module.ai1.dal.dataobject.memory;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.session.Ai1SessionMessageDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 长期记忆 DO
 *
 * 每个用户在每个 Agent 下一条未删除的记忆，由归档 Job 根据已完成的消息增量生成；
 * 删除为逻辑删除，保留历史记录便于追溯，之后的归档会根据历史消息生成新的记录
 *
 * @author 芋道源码
 */
@TableName("ai1_memory")
@KeySequence("ai1_memory_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1MemoryDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * Agent 编号
     *
     * 关联 {@link Ai1AgentDO#getId()}
     */
    private Long agentId;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 记忆正文，Markdown 格式，按维度分节
     */
    private String content;
    /**
     * 已归档的最后一条消息编号，即增量归档的水位线
     *
     * 关联 {@link Ai1SessionMessageDO#getId()}
     */
    private Long lastMessageId;

}
