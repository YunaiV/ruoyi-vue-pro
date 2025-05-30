package com.somle.cdiscount.model.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductReqVO {
    // 分类参考（示例值：010201）
    private String categoryReference;

    // 是否可销售（true/false）
    private Boolean isMarketable;


    private String cursor;

    // 每页大小（示例值：25）
    private Integer limit;

    // 返回字段（逗号分隔，示例："label,groupReference"）
    private String fields;

    // 排序字段（示例："title,description"）
    private String sort;

    // 排序方向（示例："title,description"）
    private String desc;

    // 商品国际编码（示例：6003036138437）
    private String gtin = "6003036138437";
}
