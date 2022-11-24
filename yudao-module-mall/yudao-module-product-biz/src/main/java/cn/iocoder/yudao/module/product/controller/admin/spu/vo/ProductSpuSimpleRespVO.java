package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 商品 SPU 精简 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuSimpleRespVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "商品名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @ApiModelProperty(value = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

}
