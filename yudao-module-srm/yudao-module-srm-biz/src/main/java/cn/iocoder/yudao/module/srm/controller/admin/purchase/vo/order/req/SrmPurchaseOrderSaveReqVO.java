package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单新增/修改 Request VO")
@Data
@SuppressWarnings("all")
public class SrmPurchaseOrderSaveReqVO {

    @Schema(description = "id")
    @Null(groups = Validation.OnCreate.class, message = "创建时，订单id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时订单id不能为null")
    @DiffLogField(name = "订单编号")
    private Long id;

    /**
     * code限制条件放给用户
     */
//    @Pattern(regexp = "^" + PURCHASE_ORDER_NO_PREFIX + "-\\d{8}-\\d{6}$",
//             message = "单据编号格式不正确，正确格式如：" + PURCHASE_ORDER_NO_PREFIX + "-20250108-000001")
//    @Pattern(regexp = "^" + PURCHASE_ORDER_NO_PREFIX + "-\\d{8}-[0-8]\\d{5}$",
//             message = "单据编号格式不正确，注意后6位序号中不能以9开头,正确格式:" + PURCHASE_ORDER_NO_PREFIX + "-20250108-000001")
    @Schema(description = "单据编号", example = "CGDD-20250108-000027")
    @DiffLogField(name = "单据编号")
    private String code;

    @Schema(description = "单据日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @DiffLogField(name = "单据日期")
    private LocalDateTime billTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    @DiffLogField(name = "供应商编号")
    private Long supplierId;

    @Schema(description = "结算账户编号")
    @DiffLogField(name = "结算账户编号")
    private Long accountId;

    @Schema(description = "结算日期")
    @DiffLogField(name = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "币别id(财务管理-币别维护)")
    @DiffLogField(name = "币别编号")
    @NotNull(message = "币别编号不能为空")
    private Long currencyId;

    @Schema(description = "币别名称")
    @NotBlank(message = "币别名称不能为空")
    @DiffLogField(name = "币别名称")
    private String currencyName;
    //收获地址
    @Schema(description = "收获地址")
    @DiffLogField(name = "收货地址")
    private String address;
    //付款条款
    @Schema(description = "付款条款")
    @DiffLogField(name = "付款条款")
    @NotNull(message = "付款条款不能为空")
    private String paymentTerms;

    @Schema(description = "采购主体编号")
    @DiffLogField(name = "采购主体编号")
    @NotNull(message = "采购主体编号不能为空")
    private Long purchaseCompanyId;

    @Schema(description = "装运港")
    @DiffLogField(name = "装运港")
    private String fromPortName;

    @Schema(description = "目的港")
    @DiffLogField(name = "目的港")
    private String toPortName;

    @Schema(description = "优惠率，百分比")
    @DiffLogField(name = "优惠率")
    private BigDecimal discountPercent;

    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    @DiffLogField(name = "定金金额")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址")
    @DiffLogField(name = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "订单清单列表")
    @NotNull(message = "订单项不能为空")
    @Size(min = 1, message = "商品信息至少一个")
    private List<@Valid Item> items;

    @Schema(description = "版本号")
    private Integer version;

    @Data
    public static class Item {

        @Schema(description = "订单项编号")
        @Null(groups = Validation.OnCreate.class, message = "创建时，订单项id必须为空")
        @DiffLogField(name = "订单项编号")
        private Long id;

        @Schema(description = "产品报关品名")
        @NotBlank(message = "产品报关品名不能为空")
        @DiffLogField(name = "产品报关品名")
        private String declaredType;

        @Schema(description = "产品报关品名英文")
        @NotBlank(message = "产品报关品名英文不能为空")
        @DiffLogField(name = "产品报关品名英文")
        private String declaredTypeEn;

        @Schema(description = "产品sku")
        @NotBlank(message = "产品sku不能为空")
        @DiffLogField(name = "产品SKU")
        private String productCode;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品编号不能为空")
        @DiffLogField(name = "产品编号")
        private Long productId;
        //产品名称
        @Schema(description = "产品名称")
        @NotBlank(message = "产品名称不能为空")
        @DiffLogField(name = "产品名称")
        private String productName;

        @Schema(description = "产品单位名称")
        @NotBlank(message = "产品单位名称不能为空")
        @DiffLogField(name = "产品单位名称")
        private String productUnitName;

        @Schema(description = "产品单价")
        @DecimalMin(value = "0.00", message = "产品单价不能小于0")
        @NotNull(message = "产品单价不能为空")
        @DiffLogField(name = "产品单价")
        private BigDecimal productPrice;

        @Schema(description = "产品单位ID")
//        @NotNull(message = "产品单位ID不能为空")
        @DiffLogField(name = "产品单位编号")
        private Long productUnitId;

        @Schema(description = "增值税税率，百分比")
        @DiffLogField(name = "增值税税率")
        private BigDecimal taxRate;

        @Schema(description = "税额")
        @DiffLogField(name = "税额")
        private BigDecimal tax;

        @Schema(description = "币别编号")
        private Long currencyId;

        @Schema(description = "币别名称")
        @DiffLogField(name = "币别名称")
        private String currencyName;

        @Schema(description = "含税单价")
        @DecimalMin(value = "0.00", message = "含税单价不能小于0")
        @DiffLogField(name = "含税单价")
        private BigDecimal grossPrice;

        @Schema(description = "价税合计")
        @DecimalMin(value = "0.0", message = "价税合计必须大于0")
        @DiffLogField(name = "价税合计")
        private BigDecimal grossTotalPrice;

        @Schema(description = "参考单价")
        @DecimalMin(value = "0.0", message = "参考单价必须大于0")
        @DiffLogField(name = "参考单价")
        private BigDecimal referenceUnitPrice;
        //应付款余额
        @Schema(description = "应付款余额(查询+修改)")
        @DiffLogField(name = "应付款余额")
        private BigDecimal payableBalance;

        @Schema(description = "下单数量")//审核人填写
        @NotNull(message = "count下单数量不能为空")
        @Min(value = 0, message = "产品数量必须大于0")
        @DiffLogField(name = "下单数量")
        private Integer qty;

        @Schema(description = "商品行备注")
        @DiffLogField(name = "商品行备注")
        private String remark;

        @Schema(description = "单据来源描述")
        @DiffLogField(name = "单据来源")
        private String source;
        // ========== 采购入库 ==========
        //        /**
        //         * 采购入库数量
        //         */
        //        @DecimalMin(value = "0.00", message = "采购入库数量不能小于0")
        //        private BigDecimal inboundClosedQty;

        @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
        @DiffLogField(name = "优惠率")
        private BigDecimal discountPercent;

        @Schema(description = "仓库编号")
        @DiffLogField(name = "仓库编号")
        private Long warehouseId; // 仓库编号

        @Schema(description = "交货日期")
        @DiffLogField(name = "交货日期")
        private LocalDateTime deliveryTime;
        // ========== 其他 ==========
        @Schema(description = "x码")
        @DiffLogField(name = "X码")
        private String fbaCode;

        @Schema(description = "箱率")
        @DiffLogField(name = "箱率")
        private String containerRate;

        @Schema(description = "采购申请单的申请项id")
        @DiffLogField(name = "采购申请项编号")
        private Long purchaseApplyItemId;

        @Schema(description = "部门id")
        @DiffLogField(name = "部门编号")
        private Long applicationDeptId;

        @Schema(description = "申请人id")
        @DiffLogField(name = "申请人编号")
        private Long applicantId;

        /**
         * 验货单，JSON 格式
         */
        @Schema(description = "验货单json")
        @DiffLogField(name = "验货单")
        private String inspectionJson;
        //总验货通过数量
        @Schema(description = "总验货通过数量")
        @DiffLogField(name = "总验货通过数量")
        private Integer totalInspectionPassCount;
        /**
         * 完工单，JSON 格式
         */
        @Schema(description = "完工单json")
        @DiffLogField(name = "完工单")
        private String completionJson;

        @Schema(description = "总完工单通过数量")
        @DiffLogField(name = "总完工单通过数量")
        private Integer totalCompletionPassCount;

        @Schema(description = "版本号")
        private Integer version;
    }

}