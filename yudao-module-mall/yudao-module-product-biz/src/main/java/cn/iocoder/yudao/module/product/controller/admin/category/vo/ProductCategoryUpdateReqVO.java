package cn.iocoder.yudao.module.product.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商品分类更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCategoryUpdateReqVO extends ProductCategoryBaseVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "分类编号不能为空")
    private Long id;

    @Schema(description = "分类描述", example = "描述")
    private String description;

}
