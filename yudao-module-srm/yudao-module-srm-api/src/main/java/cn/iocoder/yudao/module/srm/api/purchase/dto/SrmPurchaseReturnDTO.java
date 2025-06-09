package cn.iocoder.yudao.module.srm.api.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购退货单 DTO
 */
@Data
public class SrmPurchaseReturnDTO {

    /**
     * 采购退货单编号
     */
    private Long id;

    /**
     * 采购退货单编号
     */
    private String returnNo;

    /**
     * 采购订单编号
     */
    private Long orderId;

    /**
     * 采购订单编号
     */
    private String orderNo;

    /**
     * 供应商编号
     */
    private Long supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 退货日期
     */
    private LocalDateTime returnDate;

    /**
     * 退货状态
     */
    private Integer status;

    /**
     * 退货总金额
     */
    private BigDecimal totalAmount;

    /**
     * 退货总数量
     */
    private BigDecimal totalQuantity;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 退货明细列表
     */
    private List<SrmPurchaseReturnItemDTO> items;

} 