package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工请假申请取消 Request VO")
@Data
public class HrmAttendanceLeaveCancelReqVO {

    @Schema(description = "请假记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "请假记录编号不能为空")
    private Long id;

    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "行程取消")
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 500, message = "取消原因不能超过 500 个字符")
    private String reason;

}
