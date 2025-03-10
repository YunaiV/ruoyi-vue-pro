package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单新增/修改 Request VO")
@Data
public class ErpPurchaseOrderSaveReqVO {

    @Schema(description = "id")
    @Null(groups = validation.OnCreate.class, message = "创建时，订单id必须为空")
//    @NotNull(groups = validation.OnUpdate.class, message = "更新时，订单id不能为空")
    private Long id;

    @Schema(description = "单据编号")
    @Pattern(regexp = "^[^\\r\\n]*$", message = "单据编号不能包含换行符")
    @Pattern(regexp = "^\\S.*\\S$", message = "单据编号开头和结尾不能是空格")
    private String no;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private LocalDateTime noTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "结算账户编号")
    private Long accountId;

    //收获地址
    @Schema(description = "收获地址")
    private String address;
    //付款条款
    @Schema(description = "付款条款")
    private String paymentTerms;

    @Schema(description = "采购主体编号")
    private Long purchaseEntityId;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "订单清单列表")
    @NotNull(message = "订单项不能为空")
    @Size(min = 1, message = "商品信息至少一个")
    private List<@Valid Item> items;

    @Data
    public static class Item {

        @Schema(description = "订单项编号")
        @Null(groups = validation.OnCreate.class, message = "创建时，订单项id必须为空")
        @NotNull(groups = validation.OnUpdate.class, message = "更新时，订单项id不能为空")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单价")
        @DecimalMin(value = "0.00", message = "产品单价不能小于0")
        private BigDecimal productPrice;

        @Schema(description = "增值税税率，百分比")
        private BigDecimal taxPercent;

        @Schema(description = "税额")
        private BigDecimal taxPrice;

        @Schema(description = "币别id(财务管理-币别维护)")
        private Long currencyId;

        @Schema(description = "含税单价")
        @DecimalMin(value = "0.00", message = "含税单价不能小于0")
        private BigDecimal actTaxPrice;

//        @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED)
//        @NotNull(message = "applyCount下单数量不能为空")
//        @Min(value = 0, message = "产品数量必须大于0")
//        private Integer applyCount;

        @Schema(description = "下单数量")//审核人填写
        @NotNull(message = "count下单数量不能为空")
        @Min(value = 0, message = "产品数量必须大于0")
        private Integer count;

        @Schema(description = "商品行备注")
        private String remark;

        // ========== 采购入库 ==========
        /**
         * 采购入库数量
         */
        @DecimalMin(value = "0.00", message = "采购入库数量不能小于0")
        private BigDecimal inCount;

        @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal discountPercent;

        @Schema(description = "仓库编号")
        private Long warehouseId; // 仓库编号

        @Schema(description = "交货日期")
        private LocalDateTime deliveryTime;
        // ========== 其他 ==========
        @Schema(description = "x码")
        private String xCode;

        @Schema(description = "箱率")
        private String containerRate;

        @Schema(description = "采购申请单的申请项id")
        private Long purchaseApplyItemId;

        @Schema(description = "采购申请单No")
        private String erpPurchaseRequestItemNo;
    }

}