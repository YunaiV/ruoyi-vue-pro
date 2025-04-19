package com.somle.eccang.model.req.oms;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/2/25 10:50
 * @description:
 */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangModifySkuRelationReqVO {

    private List<ModifyData> data;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ModifyData {
        /**
         * 平台sku
         */
        private String platformSku;
        /**
         * 仓库代码
         */
        private String warehouseCode;
        /**
         * 店铺账号数组
         */
        private List<String> userAccount;
        /**
         * 仓库sku数据数组
         */
        private List<PCR> pcr;
        /**
         * 原仓库SKU数据数组
         */
        private List<Origin> origin;
    }


    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PCR {
        /**
         * 平台sku
         */
        private String productSku;
        /**
         * 仓库代码
         */
        private String productSkuQty;
        /**
         * 店铺账号数组
         */
        private String productSkuNameCn;
        /**
         * 仓库sku数据数组
         */
        private String productSkuPuPrice;
        /**
         * 原仓库SKU数据数组
         */
        private String origin;
    }


    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Origin {
        /**
         * 原仓库代码
         */
        private String warehouseCode;
        /**
         * 原店铺账号数组
         */
        private String userAccount;
        /**
         * 原仓库sku
         */
        private String productSku;

    }

}
