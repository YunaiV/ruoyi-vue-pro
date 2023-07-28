package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

@Schema(description = "管理后台 - 商品 SPU 创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuCreateReqVO extends ProductSpuBaseVO {

    // ========== SKU 相关字段 =========

    @Schema(description = "SKU 数组")
    @Valid
    private List<ProductSkuCreateOrUpdateReqVO> skus;

}
