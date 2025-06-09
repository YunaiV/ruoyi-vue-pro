package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 采购退货 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SrmPurchaseReturnBaseRespVO {

    // ========== 主键信息 ==========
    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "乐观锁")
    private Integer version;

    // ========== 基础信息 ==========
    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("退货单编号")
    private String code;

    @Schema(description = "退货时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("退货时间")
    private LocalDateTime returnTime;

    private String creator;

    private LocalDateTime createTime;

    private String updater;

    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "附件地址")
    private String fileUrl;

    // ========== 审批信息 ==========
    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("审批状态")
    private Integer auditStatus;

    @Schema(description = "审核者编号")
    private Long auditorId;

    @Schema(description = "审核者姓名")
    private String auditorName;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核意见")
    private String auditAdvice;

    // ========== 供应商信息 ==========
    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

//    @Schema(description = "供应商编码")
//    private String supplierCode;

    // ========== 结算信息 ==========
    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "结算账户名称")
    private String accountName;

    @Schema(description = "币种编号")
    private Long currencyId;

    @Schema(description = "币种名称")
    private String currencyName;

    // ========== 金额信息 ==========
    @Schema(description = "合计数量")
    private BigDecimal totalCount;

    @Schema(description = "合计产品价格，单位：元")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元")
    private BigDecimal totalGrossPrice;

    @Schema(description = "价税合计，单位：元")
    private BigDecimal grossTotalPrice;

    @Schema(description = "最终合计价格，单位：元")
    private BigDecimal totalPrice;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元")
    private BigDecimal discountPrice;

    @Schema(description = "其他费用，单位：元")
    private BigDecimal otherPrice;

    // ========== 退款信息 ==========
    @Schema(description = "退款状态")
    private Integer refundStatus;

    @Schema(description = "已退款金额，单位：元")
    private BigDecimal refundPrice;

    @Schema(description = "出库状态")
    private Integer outboundStatus;

    // ========== 物理信息 ==========
    @Schema(description = "总毛重，单位：kg")
    private BigDecimal totalWeight;

    @Schema(description = "总体积，单位：mm³")
    private BigDecimal totalVolume;

    // ========== 关联信息 ==========
    @Schema(description = "退货项列表")
    private List<Item> items;

    @Schema(description = "采购退货项")
    @Data
    public static class Item {

        // ========== 主键信息 ==========
        @Schema(description = "退货项编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Schema(description = "乐观锁")
        private Integer version;

        @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long returnId;

        // ========== 关联信息 ==========
        @Schema(description = "入库项编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long arriveItemId;

        @Schema(description = "入库单编号")
        private String arriveCode;

        // ========== 产品信息 ==========
        @Schema(description = "产品编号")
        private Long productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品SKU")
        private String productCode;

        @Schema(description = "报关品名")
        private String declaredType;

        @Schema(description = "报关品名英文")
        private String declaredTypeEn;

        @Schema(description = "产品单位编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品单位不能为空")
        private Long productUnitId;

        @Schema(description = "产品单位名称")
        private String productUnitName;

        // ========== 仓库信息 ==========
        @Schema(description = "仓库编号")
        private Long warehouseId;

        @Schema(description = "仓库名称")
        private String warehouseName;

        // ========== 数量价格信息 ==========
        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品数量不能为空")
        private BigDecimal qty;

        //产品可售库存
        @Schema(description = "仓库产品可售库存")
        private BigDecimal sellableQty;

        @Schema(description = "实际入库数量,到货项实际入库数量")
        private BigDecimal actualQty;

        @Schema(description = "产品单价，单位：元")
        private BigDecimal productPrice;

        @Schema(description = "含税单价，单位：元")
        private BigDecimal grossPrice;

        @Schema(description = "税率，百分比")
        private BigDecimal taxRate;

        @Schema(description = "税额，单位：元")
        private BigDecimal tax;

        @Schema(description = "总价，单位：元")
        private BigDecimal totalPrice;

        @Schema(description = "价税合计，单位：元")
        private BigDecimal grossTotalPrice;

        // ========== 申请人信息 ==========
        @Schema(description = "申请人编号")
        private Long applicantId;

        @Schema(description = "申请人姓名")
        private String applicantName;

        @Schema(description = "申请部门编号")
        private Long applicationDeptId;

        @Schema(description = "申请部门名称")
        private String applicationDeptName;

        // ========== 其他信息 ==========
        @Schema(description = "出库状态")
        private Integer outboundStatus;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "箱率")
        private String containerRate;

        private String creator;

        private LocalDateTime createTime;

        private String updater;

        private LocalDateTime updateTime;
    }
}