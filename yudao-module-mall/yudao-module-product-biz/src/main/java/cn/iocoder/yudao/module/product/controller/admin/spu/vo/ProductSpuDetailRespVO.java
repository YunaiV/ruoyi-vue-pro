package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 商品 SPU 详细 Response VO") // 包括关联的 SKU 等信息
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuDetailRespVO extends ProductSpuRespVO {

    // ========== SKU 相关字段 =========

    /**
     * SKU 数组
     */
    private List<Sku> skus;

    @Schema(description = "管理后台 - 商品 SKU 详细 Response VO")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Sku extends ProductSkuBaseVO {

        /**
         * 属性数组
         */
        private List<ProductPropertyValueDetailRespVO> properties;

    }

}
