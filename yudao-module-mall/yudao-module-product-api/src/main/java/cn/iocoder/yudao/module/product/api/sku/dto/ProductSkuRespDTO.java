package cn.iocoder.yudao.module.product.api.sku.dto;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * 商品 SKU 信息 Response DTO
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
public class ProductSkuRespDTO {

    /**
     * 商品 SKU 编号，自增
     */
    private Long id;
    /**
     * 商品 SKU 名字
     */
    private String name;
    /**
     * SPU 编号
     */
    private Long spuId;

    /**
     * 规格值数组，JSON 格式
     */
    private List<Property> properties;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 市场价，单位：分
     */
    private Integer marketPrice;
    /**
     * 成本价，单位：分
     */
    private Integer costPrice;
    /**
     * SKU 的条形码
     */
    private String barCode;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * SKU 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 库存
     */
    private Integer stock;
    /**
     * 预警预存
     */
    private Integer warnStock;
    /**
     * 商品重量，单位：kg 千克
     */
    private Double weight;
    /**
     * 商品体积，单位：m^3 平米
     */
    private Double volume;

    /**
     * 商品属性
     */
    @Data
    public static class Property {

        /**
         * 属性编号
         */
        private Long propertyId;
        /**
         * 属性值编号
         */
        private Long valueId;

    }


}
