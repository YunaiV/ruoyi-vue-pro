package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工月度社保项目修改 Request VO")
@Data
public class HrmInsuranceMonthEmployeeRecordUpdateReqVO {

    @Schema(description = "员工月度社保记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工社保记录编号不能为空")
    private Long id;

    @Schema(description = "社保方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "社保方案不能为空")
    private Long schemeId;

    @Schema(description = "社保项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "社保项目不能为空")
    @Valid
    private List<HrmInsuranceMonthEmployeeProjectUpdateReqVO> projects;

}
