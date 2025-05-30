package com.somle.walmart.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: LaoSan
 * @create: 2025-03-19 09:43
 **/
@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class WalmartAllProductsRepsVO {


    private List<ItemResponseDTO> itemResponse;
    private Integer totalItems;
    private String nextCursor;

    @NoArgsConstructor
    @Data
    public static class ItemResponseDTO {
        //市场名称。例如:Walmart_US
        private String mart;

        private String sku;

        //产品状况
        private String condition;

        //产品的可用性。可能的值是In_stock， Out_of_stock和Preorder
        private String availability;

        //在Walmart.com上列出商品时，由沃尔玛分配给该商品的沃尔玛产品ID
        private String wpid;

        //在美国广泛用于零售包装的12位条形码
        private String upc;

        //gtin兼容的产品ID（即UPC或EAN）。upc的长度必须是12或14位数字。ean的长度必须为13位。
        private String gtin;

        // 卖方指定的唯一标识产品名称的字母数字字符串。示例：“纯银蓝钻心形吊坠配18英寸链条”
        private String productName;

        //沃尔玛为商品指定了货架名称
        private String shelf;

        //卖方指定的唯一标识产品类型的字母数字字符串。例如:“钻石”
        private String productType;

        //指定项目购买价格信息，包括货币和金额。
        private PriceDTO price;

        //当项目处于提交过程中时，项目的状态。状态可以是：PUBLISHED、READY_TO_PUBLISH、IN_PROGRESS、UNPUBLISHED、STAGE或SYSTEM_PROBLEM。
        private String publishedStatus;

        //项目的生命周期状态描述了项目列表在整个生命周期中的位置。允许的值包括ACTIVE、ARCHIVED、RETIRED。
        private String lifecycleStatus;

        //默认情况下，无论是否为显示重复信息设置查询参数，都将包含该查询参数
        private Boolean isDuplicate;

        // 如果项目是Variant类型，则为Variant Id
        private String variantGroupId;

        //如果项目是variant类型，则附加的变体组信息
        private VariantGroupInfoDTO variantGroupInfo;

        //它概述了项目未发布的原因，即当‘ publishhedstatus ’被设置为‘ unpublished ’时。
        private UnpublishedReasonsDTO unpublishedReasons;

        //库存数量
        private Integer sellableQty;

        @NoArgsConstructor
        @Data

        public static class PriceDTO {
            //货币类型。例如：USD表示美元
            private String currency;
            //价格的数量。例如:9.99
            private Double amount;
        }

        @NoArgsConstructor
        @Data
        public static class VariantGroupInfoDTO {
            //如果是主变量，则返回true
            private Boolean isPrimary;
            //用于创建变量项的变量属性列表
            private List<GroupingAttributesDTO> groupingAttributes;

            @NoArgsConstructor
            @Data
            public static class GroupingAttributesDTO {
                private String name;
                private String value;
            }
        }

        @NoArgsConstructor
        @Data
        public static class UnpublishedReasonsDTO {
            private List<String> reason;
        }
    }
}
