package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 海关产品分类表新增/修改 Request VO")
@Data
public class TmsCustomProductSaveReqVO {
    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "产品id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品id不能为空")
    private Long productId;

    @Schema(description = "海关分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "海关分类id不能为空")
    private Long customCategoryId;

}