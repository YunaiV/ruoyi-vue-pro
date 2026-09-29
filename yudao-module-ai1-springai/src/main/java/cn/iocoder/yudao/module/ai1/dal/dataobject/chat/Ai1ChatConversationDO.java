package cn.iocoder.yudao.module.ai1.dal.dataobject.chat;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 对话 DO
 *
 * 管理后台登录用户与 Agent 的一次会话
 *
 * @author 芋道源码
 */
@TableName("ai1_chat_conversation")
@KeySequence("ai1_chat_conversation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1ChatConversationDO extends TenantBaseDO {

    /**
     * 默认标题：首条消息发送后，自动替换为提问内容
     */
    public static final String TITLE_DEFAULT = "新对话";
    /**
     * 自动生成的对话标题最大长度：首条提问内容超出时截断
     */
    public static final int TITLE_MAX_LENGTH = 50;

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
     * 标题
     */
    private String title;

}
