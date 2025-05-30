package com.somle.walmart.model.req;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalmartAllProductsReqVO {
    /**
     * 分页游标，默认值为"*"
     * 当检索超过200个商品时使用的分页方法
     */
    private String nextCursor;

    /**
     * 库存单位，卖家指定的任意字母数字唯一ID
     * 用于标识商品
     */
    private String sku;

    /**
     * 全球贸易项目编号，国际认可的产品唯一标识符
     * 必须是14位数字
     */
    private String gtin;

    /**
     * 起始偏移量，默认值为0
     * 仅当includeDetails设置为true时使用
     */
    private Integer offset;

    /**
     * 返回的实体数量，默认值为20
     * 仅当includeDetails设置为true时使用
     */
    private Integer limit;

    /**
     * 商品在整体生命周期中的状态
     */
    private String lifecycleStatus;

    /**
     * 商品在提交过程中的状态
     */
    private String publishedStatus;

    /**
     * 变体组ID，标识变体组中的所有商品
     * 不同的变体组ID属于不同的组
     */
    private String variantGroupId;

    /**
     * 商品状态
     */
    private String condition;

    /**
     * 商品可用性
     */
    private String availability;

    /**
     * 是否在响应中包含重复商品的信息
     * 设置为true可接收重复商品的信息
     */
    private Boolean showDuplicateItemInfo;
}
