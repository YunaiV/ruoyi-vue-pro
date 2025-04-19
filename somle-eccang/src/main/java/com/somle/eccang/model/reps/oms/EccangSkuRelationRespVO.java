package com.somle.eccang.model.reps.oms;

import lombok.Data;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/2/25 11:01
 * @description:
 */
@Data
public class EccangSkuRelationRespVO {

    /**
     * 平台SKU
     */
    private String platformSku;
    /**
     * 店铺别名
     */
    private String userAccount;
    /**
     * 仓库ID
     */
    private String warehouseId;
    /**
     * 对应SKU数据对象
     */
    private List<Relation> relation;


    @Data
    public static class Relation {
        /**
         * 仓库SKU
         */
        private String productSku;
        /**
         * 仓库sku数量
         */
        private String productSkuQty;
        /**
         * 采购价
         */
        private String productSkuPuPrice;
        /**
         * 费用比例
         */
        private String productSkuExpenseRatio;
        /**
         * 更新时间
         */
        private String updateTime;

    }


}
