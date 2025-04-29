package cn.iocoder.yudao.module.product.controller.app.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 APP - 商品收藏的单个 Request VO") // 用于收藏、取消收藏、获取收藏
@Data
public class AppFavoriteReqVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29502")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

}
