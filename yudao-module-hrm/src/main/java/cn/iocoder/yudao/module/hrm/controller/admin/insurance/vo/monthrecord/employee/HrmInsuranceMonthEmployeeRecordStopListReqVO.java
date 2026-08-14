package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工停止参保 Request VO")
@Data
public class HrmInsuranceMonthEmployeeRecordStopListReqVO {

    @Schema(description = "员工月度社保记录编号列表", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[1024, 1025]")
    @NotEmpty(message = "员工社保记录不能为空")
    private List<@NotNull(message = "员工社保记录编号不能为空") Long> ids;

}
