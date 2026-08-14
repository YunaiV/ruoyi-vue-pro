package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockSourceEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 打卡记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmAttendanceClockPageReqVO extends PageParam {

    @Schema(description = "员工姓名或工号，模糊匹配", example = "张三")
    private String search;

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "部门编号数组", example = "100,101")
    private List<Long> deptIds;

    @Schema(hidden = true)
    private Collection<Long> employeeIds;

    @Schema(description = "考勤打卡类型", example = "1")
    @InEnum(value = HrmAttendanceClockTypeEnum.class, message = "打卡类型必须是 {value}")
    private Integer type;

    @Schema(description = "考勤打卡状态", example = "1")
    @InEnum(value = HrmAttendanceClockStatusEnum.class, message = "打卡状态必须是 {value}")
    private Integer status;

    @Schema(description = "来源类型数组", example = "[1, 2]")
    @InEnum(value = HrmAttendanceClockSourceEnum.class, message = "打卡来源必须是 {value}")
    private List<Integer> sourceTypes;

    @Schema(description = "打卡地点，模糊匹配", example = "文三路")
    private String address;

    @Schema(description = "上班日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] attendanceTime;

    @Schema(description = "实际打卡时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] clockTime;

}
