package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工请假申请创建 Request VO")
@Data
public class HrmAttendanceLeaveCreateReqVO {

    @Schema(description = "请假类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "事假")
    @NotBlank(message = "请假类型不能为空")
    @Size(max = 64, message = "请假类型不能超过 64 个字符")
    private String type;

    @Schema(description = "请假开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请假开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "请假结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请假结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "请假天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.00")
    @NotNull(message = "请假天数不能为空")
    @DecimalMin(value = "0.01", message = "请假天数必须大于 0")
    @Digits(integer = 8, fraction = 2, message = "请假天数最多保留两位小数")
    private BigDecimal day;

    @Schema(description = "请假事由", requiredMode = Schema.RequiredMode.REQUIRED, example = "个人事务")
    @NotBlank(message = "请假事由不能为空")
    @Size(max = 300, message = "请假事由不能超过 300 个字符")
    private String reason;

    @Schema(description = "备注", example = "请审批")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @Schema(hidden = true)
    @AssertTrue(message = "请假结束时间必须晚于请假开始时间")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

}
