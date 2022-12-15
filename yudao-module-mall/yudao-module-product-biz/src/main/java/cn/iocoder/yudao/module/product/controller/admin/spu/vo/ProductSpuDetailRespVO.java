package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyViewRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel(value = "管理后台 - 商品 SPU 详细 Response VO", description = "包括关联的 SKU 等信息")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuDetailRespVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    // ========== SKU 相关字段 =========

    @ApiModelProperty(value = "库存", required = true, example = "true")
    private Integer totalStock;

    @ApiModelProperty(value = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @ApiModelProperty(value = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

    @ApiModelProperty(value = "商品销量", example = "1024")
    private Integer salesCount;

    /**
     * SKU 数组
     */
    private List<Sku> skus;

    // ========== 统计相关字段 =========

    @ApiModelProperty(value = "点击量", example = "1024")
    private Integer clickCount;

    @ApiModel(value = "管理后台 - 商品 SKU 详细 Response VO")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Sku extends ProductSkuBaseVO {

        /**
         * 属性数组
         */
        private List<ProductSpuDetailRespVO.Property> properties;

    }

    @ApiModel(value = "管理后台 - 商品属性的详细 Response VO")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Property extends ProductSkuBaseVO.Property {

        @ApiModelProperty(value = "属性项的名字", required = true, example = "颜色")
        private String propertyName;

        @ApiModelProperty(value = "属性值的名称", required = true, example = "蓝色")
        private String valueName;

    }

    @ApiModelProperty(value = "分类 id 数组，一直递归到一级父节点", example = "4")
    private Long categoryId;

    // TODO @芋艿：在瞅瞅~
    @ApiModelProperty(value = "属性修改和详情展示组合", example = "[{\"propertyId\":2,\"name\":\"内存\",\"propertyValues\":[{\"v1\":11,\"v2\":\"64G\"},{\"v1\":10,\"v2\":\"32G\"}]},{\"propertyId\":3,\"name\":\"尺寸\",\"propertyValues\":[{\"v1\":16,\"v2\":\"6.1\"},{\"v1\":15,\"v2\":\"5.7\"}]}]")
    private List<ProductPropertyViewRespVO> productPropertyViews;

}
