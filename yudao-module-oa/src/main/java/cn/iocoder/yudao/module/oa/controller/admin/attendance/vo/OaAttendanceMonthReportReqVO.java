package cn.iocoder.yudao.module.oa.controller.admin.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - OA 考勤月报 Request VO")
@Data
public class OaAttendanceMonthReportReqVO {

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "年份不能为空")
    @Min(value = 2000, message = "年份不能小于 2000")
    private Integer year;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "9")
    @NotNull(message = "月份不能为空")
    @Min(value = 1, message = "月份不能小于 1")
    @Max(value = 12, message = "月份不能大于 12")
    private Integer month;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

}
