package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 月度社保表创建 Request VO")
@Data
public class HrmInsuranceMonthRecordCreateReqVO {

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "年份不能为空")
    @Min(value = 2000, message = "年份不能小于 2000")
    @Max(value = 2100, message = "年份不能大于 2100")
    private Integer year;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "月份不能为空")
    @Min(value = 1, message = "月份必须在 1 到 12 之间")
    @Max(value = 12, message = "月份必须在 1 到 12 之间")
    private Integer month;

}
