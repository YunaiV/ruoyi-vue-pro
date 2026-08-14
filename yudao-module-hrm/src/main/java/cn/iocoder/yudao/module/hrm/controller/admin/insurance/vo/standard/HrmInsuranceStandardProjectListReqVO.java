package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - HRM 标准参保项目列表 Request VO")
@Data
public class HrmInsuranceStandardProjectListReqVO {

    @Schema(description = "参保地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "440300")
    @NotNull(message = "参保城市不能为空")
    private Integer areaId;

    @Schema(description = "参保方案编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SS100173")
    @NotBlank(message = "参保方案编码不能为空")
    private String typeCode;

}
