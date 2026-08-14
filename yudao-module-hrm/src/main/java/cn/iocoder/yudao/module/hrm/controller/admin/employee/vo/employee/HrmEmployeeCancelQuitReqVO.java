package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工取消离职 Request VO")
@Data
public class HrmEmployeeCancelQuitReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "员工撤回离职申请")
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 500, message = "取消原因不能超过 500 个字符")
    private String reason;

}
