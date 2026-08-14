package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 工资条待发员工 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmSalarySlipSendEmployeeReqVO extends PageParam {

    @Schema(description = "月度工资表编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "月度工资表编号不能为空")
    private Long monthRecordId;

    @Schema(description = "员工姓名、工号或手机号", example = "张三")
    private String search;

    @Schema(description = "部门编号", example = "100")
    private Long deptId;

    @Schema(description = "是否已发送", example = "false")
    private Boolean sent;

}
