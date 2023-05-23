package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
* 商品 SKU Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductSkuBaseVO {

    @Schema(description = "商品 SKU 名字", required = true, example = "芋道")
    @NotEmpty(message = "商品 SKU 名字不能为空")
    private String name;

    @Schema(description = "销售价格，单位：分", required = true, example = "1024")
    @NotNull(message = "销售价格，单位：分不能为空")
    private Integer price;

    @Schema(description = "市场价", example = "1024")
    private Integer marketPrice;

    @Schema(description = "成本价", example = "1024")
    private Integer costPrice;

    @Schema(description = "条形码", example = "haha")
    private String barCode;

    @Schema(description = "图片地址", required = true, example = "https://www.iocoder.cn/xx.png")
    @NotNull(message = "图片地址不能为空")
    private String picUrl;

    @Schema(description = "库存", required = true, example = "1")
    @NotNull(message = "库存不能为空")
    private Integer stock;

    @Schema(description = "预警预存", example = "1")
    private Integer warnStock;

    @Schema(description = "商品重量,单位：kg 千克", example = "1")
    private Double weight;

    @Schema(description = "商品体积,单位：m^3 平米", example = "1024")
    private Double volume;

    @Schema(description = "一级分销的佣金，单位：分", example = "1024")
    private Integer subCommissionFirstPrice;

    @Schema(description = "二级分销的佣金，单位：分", example = "1024")
    private Integer subCommissionSecondPrice;

    /**
     * 商品属性
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {

        /**
         * 属性编号
         * 关联 {@link ProductPropertyDO#getId()}
         */
        private Long propertyId;
        /**
         * 属性名字
         * 冗余 {@link ProductPropertyDO#getName()}
         *
         * 注意：每次属性名字发生变化时，需要更新该冗余
         */
        private String propertyName;

        /**
         * 属性值编号
         * 关联 {@link ProductPropertyValueDO#getId()}
         */
        private Long valueId;
        /**
         * 属性值名字
         * 冗余 {@link ProductPropertyValueDO#getName()}
         *
         * 注意：每次属性值名字发生变化时，需要更新该冗余
         */
        private String valueName;

    }
}
