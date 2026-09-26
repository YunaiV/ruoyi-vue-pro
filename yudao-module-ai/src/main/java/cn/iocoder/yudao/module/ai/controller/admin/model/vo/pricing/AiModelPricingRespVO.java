package cn.iocoder.yudao.module.ai.controller.admin.model.vo.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 模型计费配置 Response VO")
@Data
public class AiModelPricingRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long modelId;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNY")
    private String currency;

    @Schema(description = "输入单价：微元/100万tokens（缓存未命中）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000000")
    private Long priceInPer1m;

    @Schema(description = "缓存命中输入单价：微元/100万tokens", example = "500000")
    private Long priceCachedPer1m;

    @Schema(description = "输出单价：微元/100万tokens（标准输出）", requiredMode = Schema.RequiredMode.REQUIRED, example = "8000000")
    private Long priceOutPer1m;

    @Schema(description = "推理/思考输出单价：微元/100万tokens", example = "16000000")
    private Long priceReasoningPer1m;

    @Schema(description = "输入单价（元/100万tokens，缓存未命中）", example = "2.0")
    private Double priceInPer1mYuan;

    @Schema(description = "缓存命中输入单价（元/100万tokens）", example = "0.5")
    private Double priceCachedPer1mYuan;

    @Schema(description = "输出单价（元/100万tokens，标准输出）", example = "8.0")
    private Double priceOutPer1mYuan;

    @Schema(description = "推理/思考输出单价（元/100万tokens）", example = "16.0")
    private Double priceReasoningPer1mYuan;

    @Schema(description = "计费策略类型", example = "DEFAULT")
    private String strategyType;

    @Schema(description = "策略扩展配置（JSON）", example = "{}")
    private String strategyConfig;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
