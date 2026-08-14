package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee;

import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工工资项修改 Request VO")
@Data
public class HrmSalaryMonthEmployeeRecordUpdateReqVO {

    @Schema(description = "员工月度工资记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工工资记录编号不能为空")
    private Long id;

    @Schema(description = "薪资项值列表")
    @Valid
    private List<HrmSalaryOptionValueVO> optionValues;

}
