package cn.iocoder.yudao.module.product.controller.app.category.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "App - 商品分类 列表 Request VO", description = "参数和 CategoryBaseVO 是一致的")
public class AppCategoryListRespVO {

    @ApiModelProperty(value = "分类编号", required = true, example = "2")
    private Long id;

    @ApiModelProperty(value = "父分类编号", required = true, example = "1")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;

    @ApiModelProperty(value = "分类名称", required = true, example = "办公文具")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @ApiModelProperty(value = "分类图标")
    @NotBlank(message = "分类图标不能为空")
    private String icon;

    @ApiModelProperty(value = "分类图片", required = true)
    @NotBlank(message = "分类图片不能为空")
    private String bannerUrl;

    @ApiModelProperty(value = "分类排序", required = true, example = "1")
    private Integer sort;

    @ApiModelProperty(value = "分类描述", required = true, example = "描述")
    private String description;
}
