package cn.iocoder.yudao.module.srm.api.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购订单项 DTO
 */
@Data
public class SrmPurchaseOrderItemDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 产品编号
     */
    private Long materialId;

    /**
     * 产品编码
     */
    private String materialCode;

    /**
     * 产品名称
     */
    private String materialName;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 单位
     */
    private String unit;

    /**
     * 备注
     */
    private String remark;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 含税单价
     */
    private BigDecimal grossPrice;

    /**
     * 价税合计
     */
    private BigDecimal grossTotalPrice;

    /**
     * 交货日期
     */
    private String deliveryDate;

    /**
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 报关品名
     */
    private String declaredType;

    /**
     * 英文报关品名
     */
    private String declaredTypeEn;

    /**
     * 产品条码
     */
    private String productCode;
} 