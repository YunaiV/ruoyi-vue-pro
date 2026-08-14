package cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplatecategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - FMS 凭证模板分类 Response VO")
@Data
public class FmsVoucherTemplateCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long accountSetId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "日常收支")
    private String name;

}
