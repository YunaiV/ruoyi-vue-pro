package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.template;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 工资条模板项 VO")
@Data
public class HrmSalarySlipTemplateOptionVO {

    @Schema(description = "薪资工资条模板选项名称")
    @NotBlank(message = "工资条模板项名称不能为空")
    @Size(max = 64, message = "工资条模板项名称不能超过 64 个字符")
    private String name;

    @Schema(description = "薪资工资条模板选项类型")
    private Integer type;

    @Schema(description = "编码")
    private Integer code;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注不能超过 255 个字符")
    private String remark;

    @Schema(description = "父级编码")
    private Integer parentCode;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "排序")
    private Integer sort;

}
