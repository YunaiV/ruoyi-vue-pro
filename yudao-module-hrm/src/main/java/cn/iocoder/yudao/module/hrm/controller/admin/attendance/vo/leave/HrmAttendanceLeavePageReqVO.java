package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave;

import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - HRM 请假记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmAttendanceLeavePageReqVO extends SortablePageParam {

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "部门编号数组", example = "[100, 101]")
    private List<Long> deptIds;

    @Schema(description = "员工姓名或工号", example = "张三")
    private String employeeKeyword;

    @Schema(description = "请假类型数组", example = "[事假, 病假]")
    private List<String> types;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "开始时间")
    private LocalDateTime[] startTime;

    @Schema(description = "审批状态", example = "2")
    private Integer approvalStatus;

    @Schema(hidden = true)
    private Collection<Long> employeeIds;

}
