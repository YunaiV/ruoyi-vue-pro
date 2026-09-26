package cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 模型调用日志 Response VO")
@Data
public class AiModelCallLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "平台标识", example = "OPENAI")
    private String platform;

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "模型标识", example = "gpt-4o")
    private String model;

    @Schema(description = "业务类型", example = "CHAT_MESSAGE")
    private String bizType;

    @Schema(description = "业务主键", example = "100")
    private Long bizId;

    @Schema(description = "会话编号", example = "200")
    private Long conversationId;

    @Schema(description = "请求时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime requestTime;

    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "耗时（毫秒）", example = "1200")
    private Integer durationMs;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "SUCCESS")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "厂商请求编号")
    private String requestId;

    // ========== Token 统计 ==========

    @Schema(description = "输入 Token 数", example = "500")
    private Integer promptTokens;

    @Schema(description = "输出 Token 数", example = "200")
    private Integer completionTokens;

    @Schema(description = "总 Token 数", example = "700")
    private Integer totalTokens;

    @Schema(description = "缓存命中 Token 数", example = "100")
    private Integer cachedTokens;

    @Schema(description = "推理/思考 Token 数", example = "50")
    private Integer reasoningTokens;

    @Schema(description = "Token 来源", example = "PROVIDER")
    private String tokenSource;

    // ========== 费用 ==========

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "本次调用费用（微元）", example = "1500")
    private Long costAmount;

    @Schema(description = "本次调用费用（元）", example = "0.0015")
    private Double costAmountYuan;

    @Schema(description = "输入单价快照：微元/100万tokens", example = "2000000")
    private Long priceInPer1m;

    @Schema(description = "缓存命中输入单价快照：微元/100万tokens", example = "500000")
    private Long priceCachedPer1m;

    @Schema(description = "输出单价快照：微元/100万tokens", example = "8000000")
    private Long priceOutPer1m;

    @Schema(description = "推理/思考输出单价快照：微元/100万tokens", example = "16000000")
    private Long priceReasoningPer1m;

    @Schema(description = "是否被预算拦截", example = "false")
    private Boolean blocked;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
