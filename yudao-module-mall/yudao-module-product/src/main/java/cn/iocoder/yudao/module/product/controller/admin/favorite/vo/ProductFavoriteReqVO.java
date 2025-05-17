package cn.iocoder.yudao.module.product.controller.admin.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商品收藏的单个 Response VO")
@Data
@ToString(callSuper = true)
public class ProductFavoriteReqVO extends  ProductFavoriteBaseVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32734")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;
}
