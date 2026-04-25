package cn.iocoder.yudao.module.deepay.service.gateway;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 模型网关调用请求。
 */
@Data
@Builder
public class GatewayRequest {

    /** 租户 ID */
    private Long tenantId;

    /** 客户 / 用户 ID */
    private Long customerId;

    /** 会话 ID */
    private String sessionId;

    /** 板块 */
    private String module;

    /**
     * 模型名称（可选，留空使用配置的主模型）。
     * 例：deepseek-chat / moonshot-v1-8k / gpt-4o-mini
     */
    private String model;

    /** API URL（可选，留空使用配置的主 URL） */
    private String apiUrl;

    /** API Key（可选，留空使用配置的主 Key） */
    private String apiKey;

    /**
     * 消息列表（OpenAI 格式）。
     * 每条消息：{role: "system"/"user"/"assistant", content: "..."}
     */
    private List<Map<String, Object>> messages;

    /** 温度（0~2，默认 0.7） */
    @Builder.Default
    private double temperature = 0.7;

    /** 最大输出 token（0=不限制） */
    @Builder.Default
    private int maxTokens = 0;

}
