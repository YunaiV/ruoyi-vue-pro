package cn.iocoder.yudao.module.trade.service.wholesale.bo;

import lombok.Data;

import java.util.List;

/**
 * 批发库存检查结果 BO
 *
 * @author deepay
 */
@Data
public class WholesaleStockCheckBO {

    /** 所有商品均有货 */
    private boolean available;

    /** 缺货 SKU 编号列表 */
    private List<Long> unavailableSkuIds;

    /** 推荐替代品列表（available=false 时填充） */
    private List<AlternativeItem> alternatives;

    @Data
    public static class AlternativeItem {
        /** 原缺货 SKU 编号 */
        private Long originalSkuId;
        /** 推荐替代 SPU 编号 */
        private Long alternativeSpuId;
        /** 推荐替代 SKU 编号 */
        private Long alternativeSkuId;
        private String spuName;
        private String picUrl;
        /** 价格（分） */
        private Integer price;
        private Integer stock;
        /** 替换原因说明 */
        private String reason;
    }

}
