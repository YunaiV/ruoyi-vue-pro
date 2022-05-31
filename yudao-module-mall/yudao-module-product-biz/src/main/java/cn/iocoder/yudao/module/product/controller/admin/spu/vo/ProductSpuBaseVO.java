package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 商品spu Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductSpuBaseVO {

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "卖点", required = true)
    @NotNull(message = "卖点不能为空")
    private String sellPoint;

    @ApiModelProperty(value = "描述", required = true)
    @NotNull(message = "描述不能为空")
    private String description;

    @ApiModelProperty(value = "分类id", required = true)
    @NotNull(message = "分类id不能为空")
    private Long categoryId;

    @ApiModelProperty(value = "商品主图地址,* 数组，以逗号分隔,最多上传15张", required = true)
    @NotNull(message = "商品主图地址,* 数组，以逗号分隔,最多上传15张不能为空")
    private List<String> picUrls;

    @ApiModelProperty(value = "排序字段", required = true)
    @NotNull(message = "排序字段不能为空")
    private Integer sort;

    @ApiModelProperty(value = "点赞初始人数")
    private Integer likeCount;

    @ApiModelProperty(value = "价格 单位使用：分")
    private Integer price;

    @ApiModelProperty(value = "库存数量")
    private Integer quantity;

    @ApiModelProperty(value = "上下架状态： 0 上架（开启） 1 下架（禁用）")
    private Boolean status;

}
