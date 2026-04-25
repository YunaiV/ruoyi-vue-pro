package cn.iocoder.yudao.module.deepay.dal.mongodb;

import cn.iocoder.yudao.module.deepay.service.tool.AiTool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * 工具调用审计日志（MongoDB）。
 *
 * <p>记录：who / tenant / module / session / tool / params / result / error。</p>
 * <p>集合名：{@code ai_tool_audit_log}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_tool_audit_log")
@CompoundIndexes({
    @CompoundIndex(name = "idx_tenant_session", def = "{'tenantId': 1, 'sessionId': 1}"),
    @CompoundIndex(name = "idx_tenant_tool",    def = "{'tenantId': 1, 'toolName': 1, 'createdAt': -1}")
})
public class AiToolAuditLogDocument {

    @Id
    private String id;

    /** 租户 ID */
    private Long tenantId;

    /** 客户 / 用户 ID */
    private Long customerId;

    /** 板块 */
    private String module;

    /** 会话 ID */
    private String sessionId;

    /** 操作人 */
    private String operator;

    /** 工具名称 */
    private String toolName;

    /** 工具风险等级 */
    private AiTool.RiskLevel riskLevel;

    /** 调用参数 */
    private Map<String, Object> params;

    /** 执行结果（成功时） */
    private Map<String, Object> result;

    /** 错误信息（失败时） */
    private String error;

    /**
     * 执行状态：PENDING_CONFIRM / SUCCESS / FAILED / CANCELLED
     */
    private String status;

    /** 调用耗时（毫秒） */
    private long latencyMs;

    /** 创建时间 */
    private Instant createdAt;

}
