package com.somle.esb.model.oms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/2/26 11:35
 * @description:
 */
@Data
@Builder
public class SkuRelationDTO {
    private String platformSku;
    //店铺别名
    private String shopName;
    private List<Relation> relations = new ArrayList<>();

    public SkuRelationDTO addRelation(String productSku, Long productSkuQty) {
        relations.add(Relation.builder().productSku(productSku).productSkuQty(productSkuQty).build());
        return this;
    }

    @Data
    @Builder
    public static class Relation {
        private String productSku;
        private Long productSkuQty;
    }

}
