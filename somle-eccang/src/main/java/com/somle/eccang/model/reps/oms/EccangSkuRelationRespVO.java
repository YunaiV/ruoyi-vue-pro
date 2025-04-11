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
    private String platform_sku;
    /**
     * 店铺别名
     */
    private String user_account;
    /**
     * 仓库ID
     */
    private String warehouse_id;
    /**
     * 对应SKU数据对象
     */
    private List<Relation> relation;


    @Data
    public static class Relation {
        /**
         * 仓库SKU
         */
        private String product_sku;
        /**
         * 仓库sku数量
         */
        private String product_sku_qty;
        /**
         * 采购价
         */
        private String product_sku_pu_price;
        /**
         * 费用比例
         */
        private String product_sku_expense_ratio;
        /**
         * 更新时间
         */
        private String update_time;

    }


}
