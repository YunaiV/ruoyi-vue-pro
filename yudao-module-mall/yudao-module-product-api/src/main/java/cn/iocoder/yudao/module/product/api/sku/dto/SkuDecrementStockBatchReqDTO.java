package cn.iocoder.yudao.module.product.api.sku.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TODO @LeeYan9: 1) 类注释; 2) Product 开头哈;
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuDecrementStockBatchReqDTO {

    // TODO @LeeYan9: 参数校验
    private List<Item> items;

    @Data
    public static class Item {

        /**
         * 商品 SPU 编号，自增
         */
        // TODO @LeeYan9: 是不是不用传递哈
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

    // TODO @LeeYan9: 构造方法, 是不是可以满足啦
    public static SkuDecrementStockBatchReqDTO of(List<Item> items) {
        return new SkuDecrementStockBatchReqDTO(items);
    }

}
