package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
* 商品品牌 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductBrandBaseVO {

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "苹果")
    @NotNull(message = "品牌名称不能为空")
    private String name;

    @Schema(description = "品牌图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "品牌图片不能为空")
    private String picUrl;

    @Schema(description = "品牌排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "品牌排序不能为空")
    private Integer sort;

    @Schema(description = "品牌描述", example = "描述")
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
