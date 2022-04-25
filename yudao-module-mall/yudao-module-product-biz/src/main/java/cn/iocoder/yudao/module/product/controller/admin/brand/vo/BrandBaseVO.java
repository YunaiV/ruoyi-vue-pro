package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 品牌 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class BrandBaseVO {

    @ApiModelProperty(value = "分类编号", required = true, example = "1")
    @NotNull(message = "分类编号不能为空")
    private Long categoryId;

    @ApiModelProperty(value = "品牌名称", required = true, example = "芋道")
    @NotNull(message = "品牌名称不能为空")
    private String name;

    @ApiModelProperty(value = "品牌图片", required = true)
    @NotNull(message = "品牌图片不能为空")
    private String bannerUrl;

    @ApiModelProperty(value = "品牌排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "品牌描述", example = "描述")
    private String description;

    @ApiModelProperty(value = "状态", required = true, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
