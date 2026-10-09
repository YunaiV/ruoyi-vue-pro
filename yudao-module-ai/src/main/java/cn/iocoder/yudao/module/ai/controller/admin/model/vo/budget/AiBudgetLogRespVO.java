package cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 预算事件日志 Response VO")
@Data
public class AiBudgetLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号，0 表示租户级", example = "0")
    private Long userId;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "THRESHOLD_ALERT")
    private String eventType;

    @Schema(description = "周期开始时间")
    private LocalDateTime periodStartTime;

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "预算金额（微元）", example = "100000000")
    private Long budgetAmount;

    @Schema(description = "已用金额（微元）", example = "80000000")
    private Long usedAmount;

    @Schema(description = "变化金额（微元）", example = "1500")
    private Long deltaAmount;

    @Schema(description = "触发业务类型", example = "CHAT_MESSAGE")
    private String requestBizType;

    @Schema(description = "触发业务主键", example = "100")
    private Long requestBizId;

    @Schema(description = "描述信息", example = "达到80%阈值触发告警")
    private String message;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
