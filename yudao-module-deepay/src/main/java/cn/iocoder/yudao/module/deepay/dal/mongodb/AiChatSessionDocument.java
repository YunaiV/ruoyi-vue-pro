package cn.iocoder.yudao.module.deepay.dal.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * 聊天会话元信息（MongoDB）。
 *
 * <p>TTL：365 天（由 lastActiveAt 字段的 TTL 索引控制）。</p>
 * <p>集合名：{@code ai_chat_session}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_chat_session")
@CompoundIndexes({
    @CompoundIndex(name = "idx_tenant_customer", def = "{'tenantId': 1, 'customerId': 1}"),
    @CompoundIndex(name = "idx_tenant_module",   def = "{'tenantId': 1, 'module': 1}")
})
public class AiChatSessionDocument {

    @Id
    private String id;

    /** 租户 ID */
    private Long tenantId;

    /** 用户 / 客户 ID */
    private Long customerId;

    /** 板块（selection / design / product / inventory / finance / trend / order） */
    private String module;

    /** 会话摘要（每 N 轮异步更新） */
    private String summary;

    /** 消息轮数（user + ai 各算 1 条） */
    private int messageCount;

    /** 记忆开关（false 时本会话不落 memory item） */
    @Builder.Default
    private boolean memoryEnabled = true;

    /** 创建时间 */
    private Instant createdAt;

    /**
     * 最后活跃时间 — TTL 索引挂在此字段，过期时间 = 365 天。
     * 每次对话后更新。
     */
    @Indexed(name = "ttl_last_active", expireAfterSeconds = 31_536_000) // 365 * 24 * 3600
    private Instant lastActiveAt;

}
