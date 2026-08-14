package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 薪资项更新启用状态 Request VO")
@Data
public class HrmSalaryOptionUpdateEnabledReqVO {

    @Schema(description = "薪资项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "薪资项编号不能为空")
    private Long id;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

}
