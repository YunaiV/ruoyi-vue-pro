package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order;

import cn.iocoder.yudao.framework.mybatis.core.vo.BaseVO;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.BooleanEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SrmPurchaseOrderBaseRespVO extends BaseVO {

    // ========== 基础信息 ==========
    @Schema(description = "id")
    private Long id;

    @Schema(description = "采购单编号")
    private String code;

    @Schema(description = "单据日期")
    private LocalDateTime billTime;

    @Schema(description = "采购时间")
    private LocalDateTime orderTime;

    @Schema(description = "交货日期")
    private LocalDateTime deliveryDate;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "订单项列表")
    private List<Item> items;

    // ========== 供应商信息 ==========
    @Schema(description = "供应商编号")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "收获地址")
    private String address;

    // ========== 审核信息 ==========
    @Schema(description = "审核人名称")
    private String auditor;

    @Schema(description = "审核者id")
    private Long auditorId;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    @ContentStyle(shrinkToFit = BooleanEnum.TRUE)
    private LocalDateTime auditTime;

    @Schema(description = "审核意见")
    private String auditAdvice;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    // ========== 状态信息 ==========
    @Schema(description = "开关状态")
    private Integer offStatus;

    @Schema(description = "执行状态")
    private Integer executeStatus;

    @Schema(description = "入库状态")
    private Integer inboundStatus;

    @Schema(description = "付款状态")
    private Integer payStatus;

    // ========== 价格相关信息 ==========
    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元")
    private BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    private BigDecimal depositPrice;

    @Schema(description = "合计数量-项目数量")
    private BigDecimal totalCount;

    @Schema(description = "最终合计价格，单位：元")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元")
    private BigDecimal totalGrossPrice;

    // ========== 入库和退货信息 ==========
    @Schema(description = "订单采购入库数量")
    private BigDecimal totalInboundCount;

    @Schema(description = "订单采购退货数量")
    private BigDecimal totalReturnCount;

    // ========== 其他信息 ==========
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "付款条款")
    private String paymentTerms;

    @Schema(description = "采购主体编号")
    private Long purchaseCompanyId;

    @Schema(description = "币别id(财务管理-币别维护)")
    private Long currencyId;

    @Schema(description = "币别名称")
    @NotBlank(message = "币别名称不能为空")
    private String currencyName;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "装运港")
    private String fromPortName;

    @Schema(description = "目的港")
    private String toPortName;

    // ========== 内部类：订单项 ==========
    @Data
    public static class Item extends BaseVO {
        // ========== 基础信息 ==========
        @Schema(description = "订单项编号")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品sku")
        private String productCode;

        @Schema(description = "产品单位ID")
        private Long productUnitId;

        @Schema(description = "产品单位名称")
        private String productUnitName;

        @Schema(description = "型号规格型号")
        private String model;

        @Schema(description = "x码")
        private String fbaCode;

        @Schema(description = "箱率")
        private String containerRate;

        // ========== 报关信息 ==========
        @Schema(description = "产品报关品名")
        private String declaredType;

        @Schema(description = "报关品名英文")
        private String declaredTypeEn;

        @Schema(description = "报关品名-产品(产品的品牌)")
        private String customsDeclaration;

        // ========== 数量和价格信息 ==========
        @Schema(description = "产品下单数量")
        private BigDecimal qty;

        @Schema(description = "产品单价")
        private BigDecimal productPrice;

        @Schema(description = "含税单价", example = "120.00")
        private BigDecimal grossPrice;

        @Schema(description = "税率，百分比")
        private BigDecimal taxRate;

        @Schema(description = "税额，单位：元")
        private BigDecimal tax;

        @Schema(description = "价税合计")
        private BigDecimal grossTotalPrice;

        @Schema(description = "已付款金额")
        private BigDecimal payPrice;

        // ========== 入库和退货信息 ==========
        @Schema(description = "待入库数量")
        private BigDecimal waitInCount;

        @Schema(description = "采购入库数量")
        private BigDecimal inboundClosedQty;

        @Schema(description = "采购退货数量")
        private BigDecimal returnCount;

        // ========== 仓库信息 ==========
        @Schema(description = "产品存放仓库编号")
        private Long warehouseId;

        @Schema(description = "产品存放仓库名称")
        private String warehouseName;

        @Schema(description = "可用库存")
        private BigDecimal availableStock;

        // ========== 状态信息 ==========
        @Schema(description = "关闭状态")
        private Integer offStatus;

        @Schema(description = "执行状态")
        private Integer executeStatus;

        @Schema(description = "入库状态")
        private Integer inboundStatus;

        @Schema(description = "付款状态")
        private Integer payStatus;

        // ========== 申请相关信息 ==========
        @Schema(description = "采购申请项ID")
        private Long purchaseApplyItemId;

        @Schema(description = "原单单号（采购申请单code）")
        private String purchaseApplyCode;

        @Schema(description = "交货日期")
        private LocalDateTime deliveryTime;

        @Schema(description = "申请人id")
        private Long applicantId;

        @Schema(description = "申请人名称")
        private String applicantName;

        @Schema(description = "部门，由系统中进行选择")
        private Long applicationDeptId;

        @Schema(description = "部门名称")
        private String departmentName;

        // ========== 其他信息 ==========
        @Schema(description = "商品行备注")
        private String remark;

        @Schema(description = "供应商付款条款")
        private String supplierRule;

        @Schema(description = "验货单json")
        private String inspectionJson;

        @Schema(description = "总验货通过数量")
        private Integer totalInspectionPassCount;

        @Schema(description = "完工单json")
        private String completionJson;

        @Schema(description = "总完工单通过数量")
        private Integer totalCompletionPassCount;

        @Schema(description = "版本号")
        private Integer version;
    }
}