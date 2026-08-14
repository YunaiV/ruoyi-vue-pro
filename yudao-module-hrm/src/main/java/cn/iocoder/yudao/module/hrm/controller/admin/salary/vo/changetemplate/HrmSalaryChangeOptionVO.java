package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 调薪项 VO")
@Data
public class HrmSalaryChangeOptionVO {

    @Schema(description = "薪资项名称", example = "基本工资")
    private String name;

    @Schema(description = "薪资项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "10101")
    @NotNull(message = "薪资项编码不能为空")
    private Integer code;

}
