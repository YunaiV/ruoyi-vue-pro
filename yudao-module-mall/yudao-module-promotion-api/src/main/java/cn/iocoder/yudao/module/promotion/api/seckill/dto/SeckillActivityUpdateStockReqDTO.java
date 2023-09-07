package cn.iocoder.yudao.module.promotion.api.seckill.dto;

import lombok.Data;

import java.util.List;

/**
 * 更新秒杀库存 request DTO
 *
 * @author HUIHUI
 */
@Data
public class SeckillActivityUpdateStockReqDTO {

    /**
     * 活动编号
     */
    private Long activityId;
    /**
     * 总购买数量
     */
    private Integer count;
    /**
     * 活动商品
     */
    private List<Item> items;

    @Data
    public static class Item {

        /**
         * SPU 编号
         */
        private Long spuId;
        /**
         * SKU 编号
         */
        private Long skuId;
        /**
         * 购买数量
         */
        private Integer count;

    }

}
