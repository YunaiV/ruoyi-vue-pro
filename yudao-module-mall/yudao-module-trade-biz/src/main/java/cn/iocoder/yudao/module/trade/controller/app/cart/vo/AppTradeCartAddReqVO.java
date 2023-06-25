package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 购物车添加购物项 Request VO")
@Data
public class AppTradeCartAddReqVO {

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED,example = "1024")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "新增商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数量不能为空")
    private Integer count;

    @Schema(description = "是否添加到购物车", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否添加购物车不能为空")
    private Boolean addStatus;

}
