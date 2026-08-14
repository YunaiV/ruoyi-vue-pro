package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工月度考勤汇总分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmAttendanceMonthRecordPageReqVO extends PageParam {

    @Schema(description = "员工姓名或工号，模糊匹配", example = "张三")
    private String search;

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "部门编号数组", example = "100,101")
    private List<Long> deptIds;

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "年份不能为空")
    private Integer year;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "月份不能为空")
    @Min(value = 1, message = "月份必须在 1 到 12 之间")
    @Max(value = 12, message = "月份必须在 1 到 12 之间")
    private Integer month;

    @Schema(description = "是否全勤", example = "true")
    private Boolean fullAttendance;

}
