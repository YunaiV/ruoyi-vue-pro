package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 品牌精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBrandSimpleRespVO {

    @Schema(description = "品牌编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "苹果")
    private String name;

}
