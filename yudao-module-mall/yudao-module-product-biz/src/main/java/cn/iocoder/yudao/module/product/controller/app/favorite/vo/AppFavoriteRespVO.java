package cn.iocoder.yudao.module.product.controller.app.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户APP - 商品收藏 Response VO")
@Data
public class AppFavoriteRespVO {

    // TODO @jason：required true 哈
    @Schema(description = "编号", example = "1")
    private Long id;

    // TODO @jason：required true 哈
    @Schema(description = "商品 SPU 编号", example = "29502")
    private Long spuId;

    // TODO @jason：required true 哈
    @Schema(description = "类型", example = "1")
    private Integer type;

    // ========== 商品相关字段 ==========

    @Schema(description = "商品 SPU 名称", example = "赵六")
    private String spuName;

    @Schema(description = "商品封面图", example = "https://domain/pic.png")
    private String picUrl;

    @Schema(description = "商品单价", example = "100")
    private Integer price;

}
