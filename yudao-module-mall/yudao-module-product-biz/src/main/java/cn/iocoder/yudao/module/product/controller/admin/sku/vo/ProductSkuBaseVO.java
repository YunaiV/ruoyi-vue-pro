package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
* 商品 SKU Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductSkuBaseVO {

    @ApiModelProperty(value = "商品 SKU 名字", required = true, example = "芋道")
    @NotEmpty(message = "商品 SKU 名字不能为空")
    private String name;

    @ApiModelProperty(value = "销售价格，单位：分", required = true, example = "1024", notes = "单位：分")
    @NotNull(message = "销售价格，单位：分不能为空")
    private Integer price;

    @ApiModelProperty(value = "市场价", example = "1024", notes = "单位：分")
    private Integer marketPrice;

    @ApiModelProperty(value = "成本价", example = "1024", notes = "单位：分")
    private Integer costPrice;

    @ApiModelProperty(value = "条形码", example = "haha")
    private String barCode;

    @ApiModelProperty(value = "图片地址", required = true, example = "https://www.iocoder.cn/xx.png")
    @NotNull(message = "图片地址不能为空")
    private String picUrl;

    @ApiModelProperty(value = "SKU 状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举")
    @NotNull(message = "SKU 状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @ApiModelProperty(value = "库存", required = true, example = "1")
    @NotNull(message = "库存不能为空")
    private Integer stock;

    @ApiModelProperty(value = "预警预存", example = "1")
    private Integer warnStock;

    @ApiModelProperty(value = "商品重量", example = "1", notes = "单位：kg 千克")
    private Double weight;

    @ApiModelProperty(value = "商品体积", example = "1024", notes = "单位：m^3 平米")
    private Double volume;

    @ApiModel("规格值")
    @Data
    public static class Property {

        @ApiModelProperty(value = "属性编号", required = true, example = "1")
        @NotNull(message = "属性编号不能为空")
        private Long propertyId;

        @ApiModelProperty(value = "属性值编号", required = true, example = "1024")
        @NotNull(message = "属性值编号不能为空")
        private Long valueId;

    }

}
