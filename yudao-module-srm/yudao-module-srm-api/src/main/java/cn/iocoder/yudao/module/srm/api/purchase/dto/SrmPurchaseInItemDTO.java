package cn.iocoder.yudao.module.srm.api.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购入库项 DTO
 */
@Data
public class SrmPurchaseInItemDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 采购入库编号
     */
    private Long inId;

    /**
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

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
     * 产品单位编号
     */
    private Long productUnitId;

    /**
     * 产品单位名称
     */
    private String unit;

    /**
     * 产品单价，单位：元
     */
    private BigDecimal price;

    /**
     * 已付款金额
     */
    private BigDecimal payPrice;

    /**
     * 到货数量
     */
    private BigDecimal quantity;

    /**
     * 实际入库数量
     */
    private BigDecimal actualQuantity;

    /**
     * 实际入库状态
     */
    private Integer inStatus;

    /**
     * 总价，单位：元
     */
    private BigDecimal amount;

    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;

    /**
     * 合计税额
     */
    private BigDecimal totalGrossPrice;

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
     * 备注
     */
    private String remark;

    /**
     * 采购订单项编号
     */
    private Long orderItemId;

    /**
     * 付款状态
     */
    private Integer payStatus;

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