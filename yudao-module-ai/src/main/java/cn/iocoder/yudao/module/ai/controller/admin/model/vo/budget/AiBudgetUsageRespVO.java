package cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 预算使用情况 Response VO")
@Data
public class AiBudgetUsageRespVO {

    @Schema(description = "用户编号，0 表示租户级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long userId;

    @Schema(description = "周期开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime periodStartTime;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNY")
    private String currency;

    @Schema(description = "预算金额（微元）", example = "100000000")
    private Long budgetAmount;

    @Schema(description = "预算金额（元）", example = "100.0")
    private Double budgetAmountYuan;

    @Schema(description = "已用金额（微元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50000000")
    private Long usedAmount;

    @Schema(description = "已用金额（元）", example = "50.0")
    private Double usedAmountYuan;

    @Schema(description = "剩余金额（微元）", example = "50000000")
    private Long remainAmount;

    @Schema(description = "剩余金额（元）", example = "50.0")
    private Double remainAmountYuan;

    @Schema(description = "使用比例（百分比）", example = "50.0")
    private Double usagePercent;

}
