package cn.iocoder.yudao.module.oa.controller.admin.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - OA 考勤周报 Request VO")
@Data
public class OaAttendanceWeekReportReqVO {

    @Schema(description = "周开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-07")
    @NotNull(message = "周开始日期不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

}
