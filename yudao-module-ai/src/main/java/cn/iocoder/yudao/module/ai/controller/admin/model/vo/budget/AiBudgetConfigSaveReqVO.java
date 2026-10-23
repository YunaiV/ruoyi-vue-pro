package cn.iocoder.yudao.module.ai.controller.admin.model.vo.budget;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 预算配置新增/修改 Request VO")
@Data
public class AiBudgetConfigSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "用户编号，0 表示租户级预算", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "周期类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MONTHLY")
    @NotNull(message = "周期类型不能为空")
    private String periodType;

    @Schema(description = "预算金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.0")
    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0", message = "预算金额不能小于 0")
    private Double budgetAmountYuan;

    @Schema(description = "告警阈值，JSON 数组", example = "[80,90,100]")
    private String alertThresholds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
