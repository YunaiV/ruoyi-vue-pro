package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Schema(description = "用户 App - 购物车更新是否选中 Request VO")
@Data
public class AppTradeCartItemUpdateSelectedReqVO {

    @Schema(description = "商品 SKU 编号列表", required = true, example = "1024,2048")
    @NotNull(message = "商品 SKU 编号列表不能为空")
    private Collection<Long> skuIds;

    @Schema(description = "是否选中", required = true, example = "true")
    @NotNull(message = "是否选中不能为空")
    private Boolean selected;

}
