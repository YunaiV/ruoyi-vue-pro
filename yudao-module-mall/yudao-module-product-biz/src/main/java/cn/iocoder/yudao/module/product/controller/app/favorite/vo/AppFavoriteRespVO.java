package cn.iocoder.yudao.module.product.controller.app.favorite.vo;

import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author jason
 */
@Schema(description = "用户APP - 喜爱商品 Response VO")
@Data
public class AppFavoriteRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "商品SPU编号", example = "29502")
    private Long spuId;

    @Schema(description = "商品SPU名称", example = "赵六")
    private String spuName;

    @Schema(description = "商品封面图", example = "https://domain/pic.png")
    private String picUrl;

    @Schema(description = "商品单价", example = "100")
    private Integer price;

    @Schema(description = "类型 1:收藏 2：点赞", example = "1")
    private Integer type;
}
