package cn.iocoder.yudao.module.promotion.api.seckill.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 更新秒杀库存 request DTO
 *
 * @author HUIHUI
 */
@Data
public class SeckillActivityUpdateStockReqDTO {

    // TODO @puhui999：可以不用 dto，直接 activityId、skuId、count 即可

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
