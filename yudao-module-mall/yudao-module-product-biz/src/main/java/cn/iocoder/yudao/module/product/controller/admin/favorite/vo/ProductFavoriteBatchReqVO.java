package cn.iocoder.yudao.module.product.controller.admin.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "管理后台 - 商品收藏的批量 Request VO")
@Data
@ToString(callSuper = true)
public class ProductFavoriteBatchReqVO extends ProductFavoriteBaseVO{

    @Schema(description = "商品 SPU 编号数组", requiredMode = REQUIRED, example = "29502")
    @NotNull(message = "商品 SPU 编号数组不能为空")
    private List<Long> spuIds;

}
