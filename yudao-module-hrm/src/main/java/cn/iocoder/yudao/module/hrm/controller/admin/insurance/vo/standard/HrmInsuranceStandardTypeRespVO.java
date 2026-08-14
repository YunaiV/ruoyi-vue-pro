package cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - HRM 标准参保类型 Response VO")
@Data
public class HrmInsuranceStandardTypeRespVO {

    @Schema(description = "参保方案编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SS100173")
    private String code;

    @Schema(description = "参保方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "深户一档")
    private String name;

}
