package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 员工月度工资分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalaryEmployeeMonthRecordPageReqVO extends PageParam {

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工编号不能为空")
    private Long employeeId;

    @Schema(description = "月度工资表状态", example = "10")
    @InEnum(value = HrmSalaryMonthRecordStatusEnum.class, message = "月度工资表状态必须是 {value}")
    private Integer monthRecordStatus;

}
