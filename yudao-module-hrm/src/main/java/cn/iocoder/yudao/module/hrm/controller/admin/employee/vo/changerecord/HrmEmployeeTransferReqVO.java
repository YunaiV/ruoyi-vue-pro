package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeReasonEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工调岗 Request VO")
@Data
public class HrmEmployeeTransferReqVO {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "异动原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "异动原因不能为空")
    @InEnum(value = HrmEmployeeChangeReasonEnum.class, message = "异动原因必须是 {value}")
    private Integer reason;

    @Schema(description = "新部门编号", example = "200")
    private Long newDeptId;

    @Schema(description = "新岗位名称", example = "产品经理")
    @Size(max = 255, message = "新岗位名称不能超过 255 个字符")
    private String newPostName;

    @Schema(description = "新职级", example = "P6")
    @Size(max = 255, message = "新职级不能超过 255 个字符")
    private String newPostLevel;

    @Schema(description = "新工作地点", example = "上海")
    @Size(max = 255, message = "新工作地点不能超过 255 个字符")
    private String newWorkAddress;

    @Schema(description = "新直属上级员工编号", example = "3")
    private Long newLeaderEmployeeId;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生效日期不能为空")
    private LocalDateTime effectTime;

    @Schema(description = "备注", example = "业务调整")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

    @AssertTrue(message = "调岗原因不合法")
    @JsonIgnore
    public boolean isReasonValid() {
        return reason == null || HrmEmployeeChangeReasonEnum.TRANSFER_AND_PROMOTION_REASONS.contains(reason);
    }

}
