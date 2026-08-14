package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 薪资项更新显示状态 Request VO")
@Data
public class HrmSalaryOptionUpdateVisibleReqVO {

    @Schema(description = "薪资项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "薪资项编号不能为空")
    private Long id;

    @Schema(description = "是否显示", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "显示状态不能为空")
    private Boolean visible;

}
