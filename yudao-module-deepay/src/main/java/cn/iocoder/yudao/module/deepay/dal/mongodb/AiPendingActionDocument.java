package cn.iocoder.yudao.module.deepay.dal.mongodb;

import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
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
import java.util.Map;

/**
 * 待确认工具动作（Human-in-the-loop）。
 *
 * <p>高风险工具执行前先创建此记录，等待人工确认后再真正执行。</p>
 * <p>TTL：30 分钟（超时视为取消）。</p>
 * <p>集合名：{@code ai_pending_action}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_pending_action")
@CompoundIndexes({
    @CompoundIndex(name = "idx_tenant_session", def = "{'tenantId': 1, 'sessionId': 1}")
})
public class AiPendingActionDocument {

    @Id
    private String id;

    /** 租户 ID */
    private Long tenantId;

    /** 会话 ID */
    private String sessionId;

    /** 工具名称 */
    private String toolName;

    /** 风险等级 */
    private AiTool.RiskLevel riskLevel;

    /** 操作描述（给用户看） */
    private String description;

    /** 调用参数 */
    private Map<String, Object> params;

    /** 状态：WAITING / CONFIRMED / CANCELLED */
    @Builder.Default
    private String status = "WAITING";

    /** 创建时间 */
    private Instant createdAt;

    /**
     * 过期时间 — TTL 索引挂在此字段，30 分钟超时后自动清除。
     */
    @Indexed(name = "ttl_expires_at", expireAfterSeconds = 0)
    private Instant expiresAt;

}
