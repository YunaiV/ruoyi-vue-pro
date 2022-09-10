package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@ApiModel(value = "管理后台 - 商品 SPU 详细 Response VO", description = "包括关联的 SKU 等信息")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuDetailRespVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * SKU 数组
     */
    private List<Sku> skus;

    @ApiModel(value = "管理后台 - 商品 SKU 详细 Response VO")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Sku extends ProductSkuBaseVO {

        /**
         * 规格的数组
         */
        private List<ProductSpuDetailRespVO.Property> properties;

    }

    @ApiModel(value = "管理后台 - 商品规格的详细 Response VO")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Property extends ProductSkuBaseVO.Property {

        @ApiModelProperty(value = "规格的名字", required = true, example = "颜色")
        private String propertyName;

        @ApiModelProperty(value = "规格值的名字", required = true, example = "蓝色")
        private String valueName;

    }

}
