package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 商品 SPU 更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuUpdateReqVO extends ProductSpuBaseVO {

    @Schema(description = "商品编号", required = true, example = "1")
    @NotNull(message = "商品编号不能为空")
    private Long id;

    /**
     * SKU 数组
     */
    @Valid
    private List<ProductSkuCreateOrUpdateReqVO> skus;

}
