package cn.iocoder.yudao.module.fms.controller.admin.config.vo.vouchertemplatecategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - FMS 凭证模板分类保存 Request VO")
@Data
public class FmsVoucherTemplateCategorySaveReqVO {

    @Schema(description = "分类编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "日常收支")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 255, message = "分类名称长度不能超过 255 个字符")
    private String name;

}
