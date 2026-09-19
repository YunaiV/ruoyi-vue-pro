package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 会议室占用时段 Request VO")
@Data
public class OaMeetingRoomBookingScheduleReqVO {

    @Schema(description = "会议室编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "会议室编号不能为空")
    private Long roomId;

    @Schema(description = "查询开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-14 10:00:00")
    @NotNull(message = "查询开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "查询结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-14 11:00:00")
    @NotNull(message = "查询结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
