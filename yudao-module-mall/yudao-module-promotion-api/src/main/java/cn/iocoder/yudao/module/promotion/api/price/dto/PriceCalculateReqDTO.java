package cn.iocoder.yudao.module.promotion.api.price.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 价格计算 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class PriceCalculateReqDTO {

    /**
     * 用户编号
     *
     * 对应 MemberUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 优惠劵编号
     */
    private Long couponId;

    /**
     * 商品 SKU 数组
     */
    @NotNull(message = "商品数组不能为空")
    private List<Item> items;

    /**
     * 商品 SKU
     */
    @Data
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
        @Min(value = 0L, message = "商品 SKU 数量必须大于等于 0") // 可传递 0 数量，用于购物车未选中的情况
        private Integer count;

    }

}
