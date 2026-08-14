package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 月度工资表创建 Request VO")
@Data
public class HrmSalaryMonthRecordCreateReqVO {

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

    @Schema(description = "标题", example = "2026 年 7 月工资表")
    @Size(max = 128, message = "标题不能超过 128 个字符")
    private String title;

    @Schema(description = "开始日期")
    private LocalDateTime startTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;

}
