package com.somle.eccang.model.req.oms;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

/**
 * @author: LeeFJ
 * @date: 2025/2/25 10:50
 * @description:
 */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangSkuRelationReqVO {

    // 当前页
    private Integer page;

    // 每页数量（最大500）
    private Integer pageSize;

    // 查询条件：biz_content.condition
    private EccangSkuRelationReqVO.Condition condition;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Condition {
        /**
         * 平台账号
         */
        private String userAccount;
        /**
         * 平台sku
         */
        private String platformSku;
        /**
         * 仓库ID
         */
        private String warehouseId;
        /**
         * 对应SKU
         */
        private String productSku;
        /**
         * 创建开始时间
         */
        private String createDateStart;
        /**
         * 创建结束时间
         */
        private String createDateEnd;
        /**
         * 更新开始时间
         */
        private String updateDateStart;
        /**
         * 更新结束时间
         */
        private String updateDateEnd;
    }

}
