package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - ERP采购申请单采购 Request VO")
@Data
public class SrmPurchaseRequestMergeReqVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "期望采购时间")
    private LocalDateTime orderTime;
    // 项目列表
    @NotNull(message = "项目列表不能为空")
    @Schema(description = "项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<requestItems> items;

    @Schema(description = "订单币别名称")
    @NotBlank(message = "订单币别名称不能为空")
    private String currencyName;

    @Schema(description = "付款条款")
    private String paymentTerms;

    @Schema(description = "装运港")
    private String portOfLoading;

    @Schema(description = "目的港")
    private String portOfDischarge;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "定金金额，单位：元")
    @DecimalMin(value = "0.00", message = "定金金额不能小于0")
    private BigDecimal depositPrice;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Data
    public static class requestItems {

        // 项目ID
        @NotNull(message = "申请单项ID不能为空")
        @Schema(description = "申请单项ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Schema(description = "下单数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "下单数量不能为空")
        private Integer orderQuantity;

        //仓库
        @Schema(description = "仓库id")
        private Long warehouseId;

        @Schema(description = "产品单价")
        @DecimalMin(value = "0.00", message = "产品单价不能小于0")
        @NotNull(message = "产品单价不能为空")
        private BigDecimal productPrice;
        //含税单价
        @Schema(description = "含税单价")
        @Positive(message = "含税单价必须为正数")
        private BigDecimal actTaxPrice;

        @Schema(description = "价税合计")
        @DecimalMin(value = "0.0", message = "价税合计必须大于0")
        private BigDecimal allAmount;

        @Schema(description = "参考单价")
        @DecimalMin(value = "0.0", message = "参考单价必须大于0")
        private BigDecimal referenceUnitPrice;

        //是否计算得到？待确认
        @Schema(description = "税额，单位：元")
        @DecimalMin(value = "0.0", message = "税额必须大于0")
        private BigDecimal taxPrice;

        @Schema(description = "增值税税率，百分比")
        private BigDecimal taxPercent;

        @Schema(description = "交货日期(交期)")
        @NotNull(message = "交货日期不能为空")
        private LocalDateTime deliveryTime;
    }
}
