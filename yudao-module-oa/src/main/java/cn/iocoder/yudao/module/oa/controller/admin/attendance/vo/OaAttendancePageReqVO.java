package cn.iocoder.yudao.module.oa.controller.admin.attendance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceStatusEnum;
import cn.iocoder.yudao.module.oa.enums.attendance.OaAttendanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 考勤分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaAttendancePageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "考勤类型", example = "1")
    @InEnum(value = OaAttendanceTypeEnum.class, message = "考勤类型必须是 {value}")
    private Integer type;

    @Schema(description = "考勤状态", example = "1")
    @InEnum(value = OaAttendanceStatusEnum.class, message = "考勤状态必须是 {value}")
    private Integer status;

    @Schema(description = "考勤时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] attendanceTime;

}
