package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 商品sku Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductSkuBaseVO {

    // TODO @franky：example 要写哈；

    @ApiModelProperty(value = "spu编号")
    private Long spuId;

    // TODO @franky：类似这种字段，有额外说明的。可以写成；    @ApiModelProperty(value = "规格值数组", required = true, notes = "json格式， [{propertyId: , valueId: }, {propertyId: , valueId: }]")

    @ApiModelProperty(value = "规格值数组-json格式， [{propertyId: , valueId: }, {propertyId: , valueId: }]")
    private List<Property> properties;

    @ApiModelProperty(value = "销售价格，单位：分", required = true)
    @NotNull(message = "销售价格，单位：分不能为空")
    private Integer price;

    @ApiModelProperty(value = "市场价， 单位： 分", required = true)
    @NotNull(message = "市场价， 单位： 分不能为空")
    private Integer marketPrice;

    @ApiModelProperty(value = "成本价，单位： 分", required = true)
    @NotNull(message = "成本价，单位： 分不能为空")
    private Integer costPrice;

    @ApiModelProperty(value = "条形码")
//    @NotNull(message = "条形码不能为空")
    private String barCode;

    @ApiModelProperty(value = "图片地址", required = true)
    @NotNull(message = "图片地址不能为空")
    private String picUrl;

    @ApiModelProperty(value = "状态： 0-正常 1-禁用")
    private Integer status;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "商品重量，单位：kg 千克")
    private Double weight;

    @ApiModelProperty(value = "商品体积，单位：m^3 平米")
    private Double volume;

    @Data
    public static class Property {
        @NotNull(message = "规格属性名id不能为空")
        private Long propertyId;
        @NotNull(message = "规格属性值id不能为空")
        private Long valueId;
    }

}
