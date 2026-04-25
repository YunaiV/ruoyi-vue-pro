package cn.iocoder.yudao.module.ai.framework.ai.core.gateway;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * AI 调用 usage 记录
 *
 * <p>统一记录每次 AI 调用的：tenantId / customerId / module / sessionId /
 * provider / modelId / latencyMs / error / token 用量（估算时标记 estimated）</p>
 *
 * @author deepay
 */
@Data
@Builder(toBuilder = true)
@ToString
public class AiUsageRecord {

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 客户 ID（SaaS 订阅方）
     */
    private Long customerId;

    /**
     * 调用模块（如 chat、write、mindmap）
     */
    private String module;

    /**
     * 会话 ID（对话 sessionId）
     */
    private String sessionId;

    /**
     * 模型提供商（如 openai、runpod、moonshot）
     */
    private String provider;

    /**
     * 模型 ID（如 gpt-4o、qwen3-32b-awq）
     */
    private String modelId;

    /**
     * 调用耗时（毫秒）
     */
    private Long latencyMs;

    /**
     * 错误信息（null 表示成功）
     */
    private String error;

    /**
     * 输入 token 数
     */
    private Integer promptTokens;

    /**
     * 输出 token 数
     */
    private Integer completionTokens;

    /**
     * token 数是否为估算值（true 表示无法从响应中获取真实 token，用字符数估算）
     */
    private boolean estimated;

}
