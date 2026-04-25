package cn.iocoder.yudao.module.ai.framework.ai.core.gateway;

import lombok.Builder;
import lombok.Data;

/**
 * AI 调用上下文（用于 usage 记录）
 *
 * @author deepay
 */
@Data
@Builder
public class AiUsageContext {

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

}
