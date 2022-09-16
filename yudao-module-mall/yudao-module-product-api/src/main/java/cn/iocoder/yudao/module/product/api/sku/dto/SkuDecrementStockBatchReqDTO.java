package cn.iocoder.yudao.module.product.api.sku.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuDecrementStockBatchReqDTO {


    private List<Item> items;

    @Data
    public static class Item {

        /**
         * 商品 SPU 编号，自增
         */
        private Long productId;

        /**
         * 商品 SKU 编号，自增
         */
        private Long skuId;

        /**
         * 数量
         */
        private Integer count;

    }

    public static SkuDecrementStockBatchReqDTO of(List<Item> items) {
        return new SkuDecrementStockBatchReqDTO(items);
    }

}
