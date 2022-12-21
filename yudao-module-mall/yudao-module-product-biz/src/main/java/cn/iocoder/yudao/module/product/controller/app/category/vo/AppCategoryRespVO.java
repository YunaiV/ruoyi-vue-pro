package cn.iocoder.yudao.module.product.controller.app.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "用户 APP - 商品分类 Response VO")
public class AppCategoryRespVO {

    @Schema(description = "分类编号", required = true, example = "2")
    private Long id;

    @Schema(description = "父分类编号", required = true, example = "1")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;

    @Schema(description = "分类名称", required = true, example = "办公文具")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类图片", required = true)
    @NotBlank(message = "分类图片不能为空")
    private String picUrl;

}
