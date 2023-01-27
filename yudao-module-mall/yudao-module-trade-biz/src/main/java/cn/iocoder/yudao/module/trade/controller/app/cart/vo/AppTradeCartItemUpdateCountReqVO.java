package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel(value = "用户 App - 购物车更新数量 Request VO")
@Data
public class AppTradeCartItemUpdateCountReqVO {

    @ApiModelProperty(value = "商品 SKU 编号", required = true, example = "1024")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @ApiModelProperty(value = "商品数量", required = true, example = "1")
    @NotNull(message = "数量不能为空")
    @Min(message = "数量必须大于 0", value = 1L)
    private Integer count;

}
