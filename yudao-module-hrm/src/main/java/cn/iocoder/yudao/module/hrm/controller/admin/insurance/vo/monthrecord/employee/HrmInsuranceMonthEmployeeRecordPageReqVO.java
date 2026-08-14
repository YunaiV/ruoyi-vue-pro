package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.employee.HrmInsuranceEmployeeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 员工月度社保分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmInsuranceMonthEmployeeRecordPageReqVO extends PageParam {

    @Schema(description = "月度社保表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "社保表编号不能为空")
    private Long monthRecordId;

    @Schema(description = "员工编号", example = "1024")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "社保方案编号", example = "1024")
    private Long schemeId;

    @Schema(description = "参保地区编号", example = "440300")
    private Integer areaId;

    @Schema(description = "参保状态", example = "1")
    @InEnum(value = HrmInsuranceEmployeeStatusEnum.class, message = "参保状态必须是 {value}")
    private Integer status;

}
