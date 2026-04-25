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
 * 聊天消息明细（MongoDB）。
 *
 * <p>SSE 流式过程中不逐 token 落库；AI 完成后合并成一条最终 assistant message 再落库。</p>
 * <p>TTL：365 天（由 createdAt 字段的 TTL 索引控制）。</p>
 * <p>集合名：{@code ai_chat_message}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_chat_message")
@CompoundIndexes({
    @CompoundIndex(name = "idx_session",          def = "{'sessionId': 1, 'createdAt': 1}"),
    @CompoundIndex(name = "idx_tenant_customer",  def = "{'tenantId': 1, 'customerId': 1, 'createdAt': -1}")
})
public class AiChatMessageDocument {

    @Id
    private String id;

    /** 所属会话 ID */
    private String sessionId;

    /** 租户 ID */
    private Long tenantId;

    /** 用户 / 客户 ID */
    private Long customerId;

    /** 板块 */
    private String module;

    /** 消息角色：user / assistant */
    private String role;

    /** 消息正文 */
    private String content;

    /**
     * 创建时间 — TTL 索引挂在此字段，过期时间 = 365 天。
     */
    @Indexed(name = "ttl_created_at", expireAfterSeconds = 31_536_000)
    private Instant createdAt;

}
