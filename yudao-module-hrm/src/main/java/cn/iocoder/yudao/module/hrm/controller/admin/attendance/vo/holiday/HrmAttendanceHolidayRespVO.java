package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 考勤节假日 Response VO")
@Data
public class HrmAttendanceHolidayRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "日期")
    private LocalDateTime date;

    @Schema(description = "考勤假期类型")
    private Integer type;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
