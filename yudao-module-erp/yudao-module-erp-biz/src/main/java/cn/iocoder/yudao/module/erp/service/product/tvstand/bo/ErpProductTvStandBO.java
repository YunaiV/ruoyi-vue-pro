package cn.iocoder.yudao.module.erp.service.product.tvstand.bo;

import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Wqh
 * @date: 2024/12/3 16:13
 */
@Data
public class ErpProductTvStandBO {
    /**
     * 产品主信息
     */
    private ErpProductBO product;
    /**
     * 层板承重
     */
    private String shelfLoadCapacity;
    /**
     * 层板数量
     */
    private Integer shelvesCount;
    /**
     * 电视调节方式
     */
    private String tvAdjustmentMethod;
    /**
     * 层板调节方式
     */
    private String shelfAdjustmentMethod;
    /**
     * 设计说明
     */
    private String description;
    /**
     * 宽度最大值
     */
    private BigDecimal widthMax;
    /**
     * 宽度最小值
     */
    private BigDecimal widthMin;
    /**
     * 长度最大值
     */
    private BigDecimal lengthMax;
    /**
     * 长度最小值
     */
    private BigDecimal lengthMin;
}
