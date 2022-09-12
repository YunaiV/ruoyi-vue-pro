package cn.iocoder.yudao.module.trade.controller.app.cart.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@ApiModel(value = "用户 App - 购物车更新是否选中 Request VO")
@Data
public class AppTradeCartItemUpdateSelectedReqVO {

    @ApiModelProperty(value = "商品 SKU 编号列表", required = true, example = "1024,2048")
    @NotNull(message = "商品 SKU 编号列表不能为空")
    private Collection<Long> skuIds;

    @ApiModelProperty(value = "是否选中", required = true, example = "true")
    @NotNull(message = "是否选中不能为空")
    private Boolean selected;

}
