package cn.iocoder.yudao.module.promotion.api.combination.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 拼团活动更新活动库存 Request DTO
 *
 * @author HUIHUI
 */
@Data
public class CombinationActivityUpdateStockReqDTO {

    @NotNull(message = "活动编号不能为空")
    private Long activityId;

    @NotNull(message = "购买数量不能为空")
    private Integer count;

    @NotNull(message = "活动商品不能为空")
    private Item item;

    @Data
    @Valid
    public static class Item {

        @NotNull(message = "SPU 编号不能为空")
        private Long spuId;

        @NotNull(message = "SKU 编号活动商品不能为空")
        private Long skuId;

        @NotNull(message = "购买数量不能为空")
        private Integer count;

    }

}
