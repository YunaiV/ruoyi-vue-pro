package cn.iocoder.yudao.module.product.service.property.bo;

import lombok.Data;

/**
 * 商品属性项的明细 Response BO
 *
 * @author 芋道源码
 */
@Data
public class ProductPropertyValueDetailRespBO {

    /**
     * 属性的编号
     */
    private Long propertyId;

    /**
     * 属性的名称
     */
    private String propertyName;

    /**
     * 属性值的编号
     */
    private Long valueId;

    /**
     * 属性值的名称
     */
    private String valueName;

}
