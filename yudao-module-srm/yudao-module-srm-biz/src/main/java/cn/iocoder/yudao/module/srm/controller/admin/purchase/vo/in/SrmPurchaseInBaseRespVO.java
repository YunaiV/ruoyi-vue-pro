package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 采购到货单基础 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SrmPurchaseInBaseRespVO {

    // ========== 基本信息 ==========
    @Schema(description = "到货单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货单编号")
    private Long id;

    @Schema(description = "到货单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货单号")
    private String code;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据日期")
    private LocalDateTime billTime;

    @Schema(description = "到货时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货时间")
    private LocalDateTime arriveTime;

    @Schema(description = "备注")
    private String remark;

    // ========== 供应商信息 ==========
    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long supplierId;

    @Schema(description = "供应商名称")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "收货地址")
    private String address;

    // ========== 结算信息 ==========
    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "结算账户名称")
    private String accountName;

    @Schema(description = "币种编号")
    private Long currencyId;

    @Schema(description = "币种名称")
    private String currencyName;

    @Schema(description = "汇率")
    private BigDecimal exchangeRate;

    // ========== 状态信息 ==========
    @Schema(description = "入库状态")
    private Integer inboundStatus;

    @Schema(description = "付款状态")
    private Integer payStatus;

    @Schema(description = "对账状态")
    private Boolean reconciliationStatus;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "审核人编号")
    private Long auditorId;

    @Schema(description = "审核人名称")
    @ExcelProperty("审核人名称")
    private String auditorName;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核意见")
    private String auditAdvice;

    // ========== 数量金额信息 ==========
    @Schema(description = "商品总体积，单位：m³")
    private BigDecimal totalVolume;

    @Schema(description = "商品总重量，单位：kg")
    private BigDecimal totalWeight;

    @Schema(description = "合计数量")
    private BigDecimal totalCount;

    @Schema(description = "最终合计价格")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额")
    private BigDecimal totalGrossPrice;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额")
    private BigDecimal discountPrice;

    @Schema(description = "其他金额")
    private BigDecimal otherPrice;

    @Schema(description = "已支付金额")
    private BigDecimal paymentPrice;

    // ========== 审计信息 ==========
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "创建人编号")
    private String creator;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新人编号")
    private String updater;

    @Schema(description = "更新人名称")
    private String updaterName;

    @Schema(description = "版本号")
    private Integer version;

    // ========== 入库项列表 ==========
    @Schema(description = "入库项列表")
    private List<Item> items;

    @Data
    public static class Item {

        // ========== 基本信息 ==========
        @Schema(description = "入库项编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Schema(description = "到货单编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long arriveId;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "版本号")
        private Integer version;

        // ========== 仓库信息 ==========
        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long warehouseId;

        @Schema(description = "仓库名称")
        @ExcelProperty("仓库名称")
        private String warehouseName;

        // ========== 产品信息 ==========
        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;

        @Schema(description = "产品名称")
        @ExcelProperty("产品名称")
        private String productName;

        @Schema(description = "产品单位编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productUnitId;

        @Schema(description = "产品单位名称")
        @ExcelProperty("产品单位")
        private String productUnitName;

        @Schema(description = "型号规格")
        private String model;

        @Schema(description = "报关品名")
        private String declaredType;

        @Schema(description = "报关品名英文")
        private String declaredTypeEn;

        @Schema(description = "条码")
        private String productCode;

        @Schema(description = "x码")
        private String fbaCode;

        @Schema(description = "箱率")
        private String containerRate;

        // ========== 数量金额信息 ==========
        @Schema(description = "产品单价")
        private BigDecimal productPrice;

        @Schema(description = "库存数量")
        private BigDecimal stockCount;

        @Schema(description = "到货数量")
        private BigDecimal qty;

        @Schema(description = "实际入库数量")
        private BigDecimal actualQty;

        @Schema(description = "关联订单行的采购数")
        private BigDecimal orderQty;

        @Schema(description = "总价")
        private BigDecimal totalPrice;

        @Schema(description = "税率，百分比")
        private BigDecimal taxRate;

        @Schema(description = "税额")
        private BigDecimal tax;

        @Schema(description = "含税单价")
        private BigDecimal grossPrice;

        @Schema(description = "价税合计")
        private BigDecimal grossTotalPrice;

        @Schema(description = "合计产品价格")
        private BigDecimal totalProductPrice;

        @Schema(description = "合计税价")
        private BigDecimal totalGrossPrice;

        @Schema(description = "已付款金额")
        private BigDecimal payPrice;

        // ========== 状态信息 ==========
        @Schema(description = "入库状态")
        private Integer inboundStatus;

        @Schema(description = "付款状态")
        private Integer payStatus;

        @Schema(description = "付款状态名称")
        private String payStatusName;

        // ========== 来源信息 ==========
        @Schema(description = "采购订单项编号")
        private Long orderItemId;

        @Schema(description = "采购订单编号")
        private String orderCode;

        @Schema(description = "单据来源")
        private String source;

        // ========== 申请人信息 ==========
        @Schema(description = "申请人编号")
        private Long applicantId;

        @Schema(description = "申请人名称")
        private String applicantName;

        @Schema(description = "申请部门编号")
        private Long applicationDeptId;

        @Schema(description = "申请部门名称")
        private String applicationDeptName;
    }
}
