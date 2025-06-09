package cn.iocoder.yudao.module.srm.api.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购退货项 DTO
 */
@Data
public class SrmPurchaseReturnItemDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 物料编号
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
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
     * 税率，百分比
     */
    private BigDecimal taxRate;

    /**
     * 税额，单位：元
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
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 产品条码
     */
    private String productCode;

    /**
     * 报关品名
     */
    private String declaredType;

    /**
     * 英文报关品名
     */
    private String declaredTypeEn;

    /**
     * 箱率
     */
    private String containerRate;

    /**
     * 申请人编号
     */
    private Long applicantId;

    /**
     * 申请部门编号
     */
    private Long applicationDeptId;
} 