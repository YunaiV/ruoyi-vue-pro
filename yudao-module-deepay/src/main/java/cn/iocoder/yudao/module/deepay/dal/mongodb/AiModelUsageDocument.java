package cn.iocoder.yudao.module.deepay.dal.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * 模型调用用量记录（MongoDB）。
 *
 * <p>记录：tenantId / customerId / model / tokensIn / tokensOut / latencyMs / cost / error。</p>
 * <p>集合名：{@code ai_model_usage}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_model_usage")
@CompoundIndexes({
    @CompoundIndex(name = "idx_tenant_model_date",
                   def  = "{'tenantId': 1, 'model': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "idx_tenant_customer",
                   def  = "{'tenantId': 1, 'customerId': 1, 'createdAt': -1}")
})
public class AiModelUsageDocument {

    @Id
    private String id;

    /** 租户 ID */
    private Long tenantId;

    /** 客户 / 用户 ID */
    private Long customerId;

    /** 会话 ID */
    private String sessionId;

    /** 板块 */
    private String module;

    /** 模型名称（如 moonshot-v1-8k / gpt-4o-mini / glm-4） */
    private String model;

    /** 服务商（moonshot / openai / zhipu / spark） */
    private String provider;

    /** 输入 token 数 */
    private int tokensIn;

    /** 输出 token 数 */
    private int tokensOut;

    /** 总 token 数 */
    private int tokensTotal;

    /** 调用耗时（毫秒） */
    private long latencyMs;

    /**
     * 估算成本（USD，粗估）。
     * 公式：(tokensIn × inPrice + tokensOut × outPrice) / 1_000_000
     */
    private double estimatedCostUsd;

    /** 是否发生 fallback（true=首选模型失败，切换到备用模型） */
    @Builder.Default
    private boolean fallback = false;

    /** 错误信息（成功时为 null） */
    private String error;

    /** 创建时间 */
    private Instant createdAt;

}
