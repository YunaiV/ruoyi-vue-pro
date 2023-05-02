package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 商品 SPU 详细 Response VO") // 包括关联的 SKU 等信息
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuDetailRespVO extends ProductSpuBaseVO {

    @Schema(description = "商品编号", example = "1")
    private Long id;
    // ========== SKU 相关字段 =========

    @Schema(description = "SKU 数组", example = "1")
    private List<ProductSkuRespVO> skus;

}
