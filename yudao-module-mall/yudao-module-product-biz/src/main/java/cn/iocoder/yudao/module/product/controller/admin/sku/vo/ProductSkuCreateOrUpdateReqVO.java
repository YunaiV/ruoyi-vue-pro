package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ApiModel("管理后台 - 商品 SKU 创建/更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSkuCreateOrUpdateReqVO extends ProductSkuBaseVO {

    // TODO @Luowenfeng：可以不用哈，如果基于规格匹配
    @ApiModelProperty(value = "商品 id 更新时须有", example = "1")
    private Long id;

    /**
     * 规格值数组
     */
    private List<Property> properties;

}
