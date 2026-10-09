package cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 预算配置 Response VO")
@Data
public class AiBudgetConfigRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号，0 表示租户级预算", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long userId;

    @Schema(description = "周期类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MONTHLY")
    private String periodType;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNY")
    private String currency;

    @Schema(description = "预算金额（微元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100000000")
    private Long budgetAmount;

    @Schema(description = "预算金额（元）", example = "100.0")
    private Double budgetAmountYuan;

    @Schema(description = "告警阈值", example = "[80,90,100]")
    private String alertThresholds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
