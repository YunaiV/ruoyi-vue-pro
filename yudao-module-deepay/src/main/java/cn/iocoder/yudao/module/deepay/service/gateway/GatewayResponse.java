package cn.iocoder.yudao.module.deepay.service.gateway;

import lombok.Builder;
import lombok.Data;

/**
 * 模型网关调用响应。
 */
@Data
@Builder
public class GatewayResponse {

    /** 模型生成的回复正文 */
    private String content;

    /** 实际使用的模型名称 */
    private String model;

    /** 服务商 */
    private String provider;

    /** 输入 token 数 */
    private int tokensIn;

    /** 输出 token 数 */
    private int tokensOut;

    /** 调用耗时（毫秒） */
    private long latencyMs;

    /** 估算成本（USD） */
    private double estimatedCostUsd;

    /** 是否发生了 fallback */
    private boolean fallback;

}
