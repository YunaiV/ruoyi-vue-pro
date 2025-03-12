package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库新增/修改 Request VO")
@Data
public class ErpPurchaseInSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    private Long id;

    @Schema(description = "采购订单id", requiredMode = Schema.RequiredMode.REQUIRED, example = "CGRK-20241012-00197")
    @NotNull(message = "采购订单编号不能为空")
    private Long orderId;
    /**
     * 采购订单号
     * 冗余 {@link ErpPurchaseOrderDO#getNo()}
     */
    @Schema(description = "采购订单号", example = "CGDD-20241012-00197")
    private String orderNo;

    @Schema(description = "单据日期", example = "2024-10-12")
    private LocalDateTime noTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "币别ID,财务管理-币别维护")
    private Long currencyId;

    @Schema(description = "汇率,财务管理-币别维护", example = "5.8")
    private BigDecimal exchangeRate;

    @Schema(description = "审核人ID")
    private String auditorId;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "入库审核状态")
    private Integer auditorStatus;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "入库时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "入库时间不能为空")
    private LocalDateTime inTime;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    private BigDecimal discountPercent;

    @Schema(description = "其它金额，单位：元", example = "100")
    private BigDecimal otherPrice;

    @Schema(description = "附件地址", example = "https://www.abc.cn")
    private String fileUrl;

    @Schema(description = "备注", example = "备注123")
    private String remark;

    @Schema(description = "入库清单列表")
    private List<Item> items;

    @Schema(description = "对账状态(false:未对账 ，true:已对账)", example = "false")
    private Boolean reconciliationStatus;

    @Data
    public static class Item {
        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "型号规格(产品带出)")
        private String model;

        @Schema(description = "产品名称(快照)")
        private String productName;

        @Schema(description = "入库项编号", example = "11756")
        private Long id;

        @Schema(description = "采购订单项id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        @NotNull(message = "采购订单项id不能为空")
        private Long orderItemId;

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "仓库编号不能为空")
        private Long warehouseId;

        @Schema(description = "产品单位(产品带出来)", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品单位单位不能为空")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        @Schema(description = "价税合计")
        private BigDecimal allAmount;

        @Schema(description = "增值税税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "含税单价", example = "100.00")
        @DecimalMin(value = "0.00", message = "含税产品单价不能小于0")
        private BigDecimal actTaxPrice;

        @Schema(description = "备注", example = "商品行备注")
        private String remark;

        @Schema(description = "源单行号")
        private int srcSeq;

        @Schema(description = "源单单号")
        private int srcNo;

        @Schema(description = "箱率")
        private String containerRate;

        @Schema(description = "币别id(财务管理-币别维护)")
        private Long currencyId;

    }
}