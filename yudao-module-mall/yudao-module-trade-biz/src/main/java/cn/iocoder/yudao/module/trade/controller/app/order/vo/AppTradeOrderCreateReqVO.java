package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(value = "用户 App - 交易订单创建 Request VO")
@Data
public class AppTradeOrderCreateReqVO {

    @ApiModelProperty(name = "收件地址编号", required = true, example = "1")
    @NotNull(message = "收件地址不能为空")
    private Integer addressId;

    @ApiModelProperty(name = "优惠劵编号", example = "1024")
    private Long couponId;

    @ApiModelProperty(name = "备注", example = "1024")
    private String remark;

    @ApiModelProperty(name = "是否来自购物车", required = true, example = "true", notes = "true - 来自购物车；false - 立即购买")
    @NotNull(message = "是否来自购物车不能为空")
    private Boolean fromCart;

    /**
     * 订单商品项列表
     */
    @NotNull(message = "必须选择购买的商品")
    private List<Item> items;

    @ApiModel(value = "订单商品项")
    @Data
    public static class Item {

        @ApiModelProperty(name = "商品 SKU 编号", required = true, example = "111")
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long skuId;

        @ApiModelProperty(name = "商品 SKU 购买数量", required = true, example = "1024")
        @NotNull(message = "商品 SKU 购买数量不能为空")
        @Min(value = 1, message = "商品 SKU 购买数量必须大于 0")
        private Integer count;

    }

}
