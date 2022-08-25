package cn.iocoder.yudao.module.product.controller.admin.category.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
* 商品分类 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class CategoryBaseVO {

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

    @ApiModelProperty(value = "开启状态", required = true, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

}
