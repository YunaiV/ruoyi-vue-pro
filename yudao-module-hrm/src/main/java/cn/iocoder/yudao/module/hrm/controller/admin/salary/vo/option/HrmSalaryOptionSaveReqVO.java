package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 工资表薪资项保存 Request VO")
@Data
public class HrmSalaryOptionSaveReqVO {

    @Schema(description = "父薪资项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "薪资项分类不能为空")
    private Integer parentCode;

    @Schema(description = "薪资项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "基本工资")
    @NotBlank(message = "薪资项名称不能为空")
    @Size(max = 64, message = "薪资项名称长度不能超过 64 个字符")
    private String name;

    @Schema(description = "备注", example = "固定薪资项")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
