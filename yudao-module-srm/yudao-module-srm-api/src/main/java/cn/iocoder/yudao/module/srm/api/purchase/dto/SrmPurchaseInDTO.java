package cn.iocoder.yudao.module.srm.api.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购入库单 DTO
 */
@Data
public class SrmPurchaseInDTO {

    /**
     * 采购入库单编号
     */
    private Long id;

    /**
     * 采购入库单编号
     */
    private String inNo;

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
     * 入库日期
     */
    private LocalDateTime inDate;

    /**
     * 入库状态
     */
    private Integer status;

    /**
     * 入库总金额
     */
    private BigDecimal totalAmount;

    /**
     * 入库总数量
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
     * 入库明细列表
     */
    private List<SrmPurchaseInItemDTO> items;

} 