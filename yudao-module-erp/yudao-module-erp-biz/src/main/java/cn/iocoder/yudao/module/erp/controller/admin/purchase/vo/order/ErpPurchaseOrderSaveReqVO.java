package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单新增/修改 Request VO")
@Data
public class ErpPurchaseOrderSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "单据日期")
    private LocalDateTime noTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "采购时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采购时间不能为空")
    private LocalDateTime orderTime;

//    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
//    private BigDecimal discountPercent;

    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "订单清单列表")
    private List<Item> items;
    // ========== 部门和主体信息 ==========

    @Schema(description = "部门编号")
    private Long departmentId;

    @Schema(description = "采购主体编号")
    private Long purchaseEntityId;

    @Data
    public static class Item {

        @Schema(description = "订单项编号")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单位", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品单位不能为空")
        private Long productUnitId;

        @Schema(description = "产品单价")
        @DecimalMin(value = "0.00", message = "产品单价不能小于0")
        private BigDecimal productPrice;

        @Schema(description = "币别id(财务管理-币别维护)")
        private Long currencyId;

        @Schema(description = "含税单价")
        @DecimalMin(value = "0.00", message = "含税单价不能小于0")
        private BigDecimal actTaxPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品数量不能为空")
        @DecimalMin(value = "1", message = "产品数量必须大于0")
        private BigDecimal count;

        @Schema(description = "增值税税率，百分比")
        private BigDecimal taxPercent;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "供应商产品编号")
        @Pattern(regexp = "\\d+$", message = "供应商产品编号格式不正确")
        @NotNull(message = "供应商产品编号不能为空")
        private String supplierProductId;

        // ========== 采购入库 ==========
        /**
         * 采购入库数量
         */
        @DecimalMin(value = "0.00", message = "采购入库数量不能小于0")
        private BigDecimal inCount;

        @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal discountPercent;
        // ========== 仓库相关 ==========
        @Schema(description = "仓库编号")
        private String warehouseId; // 仓库编号

        @Schema(description = "交货日期")
        private LocalDateTime deliveryTime;
        // ========== 其他 ==========
        @Schema(description = "x码")
        private String xCode;

        @Schema(description = "箱率")
        private String containerRate;
    }

}