package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceHolidayTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 考勤节假日保存 Request VO")
@Data
public class HrmAttendanceHolidaySaveReqVO {

    @Schema(description = "节假日编号", example = "1024")
    private Long id;

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    private LocalDateTime date;

    @Schema(description = "节假日类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "类型不能为空")
    @InEnum(value = HrmAttendanceHolidayTypeEnum.class, message = "节假日类型必须是 {value}")
    private Integer type;

}
