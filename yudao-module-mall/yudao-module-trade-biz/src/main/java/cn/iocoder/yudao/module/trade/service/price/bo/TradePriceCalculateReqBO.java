package cn.iocoder.yudao.module.trade.service.price.bo;

import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 价格计算 Request BO
 *
 * @author yudao源码
 */
@Data
public class TradePriceCalculateReqBO {

    /**
     * 订单类型
     *
     * 枚举 {@link TradeOrderTypeEnum}
     */
    private Integer type;

    /**
     * 用户编号
     *
     * 对应 MemberUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 优惠劵编号
     *
     * 对应 CouponDO 的 id 编号
     */
    private Long couponId;

    /**
     * 收货地址编号
     *
     * 对应 MemberAddressDO 的 id 编号
     */
    private Long addressId;

    /**
     * 商品 SKU 数组
     */
    @NotNull(message = "商品数组不能为空")
    private List<Item> items;

    /**
     * 商品 SKU
     */
    @Data
    @Valid
    public static class Item {

        /**
         * SKU 编号
         */
        @NotNull(message = "商品 SKU 编号不能为空")
        private Long skuId;

        /**
         * SKU 数量
         */
        @NotNull(message = "商品 SKU 数量不能为空")
        @Min(value = 0L, message = "商品 SKU 数量必须大于等于 0")
        private Integer count;

        /**
         * 购物车项的编号
         */
        private Long cartId;

        /**
         * 是否选中
         */
        @NotNull(message = "是否选中不能为空")
        private Boolean selected;

    }

}
