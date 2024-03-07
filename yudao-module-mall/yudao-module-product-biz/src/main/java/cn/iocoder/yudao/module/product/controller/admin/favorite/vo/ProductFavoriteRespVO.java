package cn.iocoder.yudao.module.product.controller.admin.favorite.vo;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 商品收藏 Response VO")
@Data
@ToString(callSuper = true)
public class ProductFavoriteRespVO  extends ProductSpuRespVO {

    @Schema(description = "userId", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Long userId;

    @Schema(description = "spuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Long spuId;

}
