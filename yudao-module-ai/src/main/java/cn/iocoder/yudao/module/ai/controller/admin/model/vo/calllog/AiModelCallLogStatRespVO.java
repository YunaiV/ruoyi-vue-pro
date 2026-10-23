package cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - AI 模型调用日志统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelCallLogStatRespVO {

    @Schema(description = "调用总次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Long totalCount;

    @Schema(description = "成功次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "950")
    private Long successCount;

    @Schema(description = "失败次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Long failCount;

    @Schema(description = "总输入 Token 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "500000")
    private Long totalPromptTokens;

    @Schema(description = "总输出 Token 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "200000")
    private Long totalCompletionTokens;

    @Schema(description = "总 Token 数", requiredMode = Schema.RequiredMode.REQUIRED, example = "700000")
    private Long totalTokens;

    @Schema(description = "总费用（微元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15000000")
    private Long totalCostAmount;

    @Schema(description = "总费用（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15.0")
    private Double totalCostAmountYuan;

    @Schema(description = "平均耗时（毫秒）", example = "1200")
    private Long avgDurationMs;

}
