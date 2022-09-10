package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("App - 商品spu分页 Request VO")
@Data
public class AppSpuPageRespVO  {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    private Long id;

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
    private Integer status;

}
