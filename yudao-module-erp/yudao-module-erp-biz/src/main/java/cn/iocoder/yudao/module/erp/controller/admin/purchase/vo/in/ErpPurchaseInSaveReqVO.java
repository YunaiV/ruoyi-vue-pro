package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库新增/修改 Request VO")
@Data
@Builder
public class ErpPurchaseInSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = validation.OnUpdate.class, message = "更新时，订单id不能为空")
    private Long id;

    /**
     * 采购订单号
     * 冗余 {@link ErpPurchaseOrderDO#getNo()}
     */
    @Schema(description = "入库单号")
    private String no;

    @Schema(description = "入库时间(不填默认当前时间)")
    private LocalDateTime inTime;

    @Schema(description = "单据日期")
    private LocalDateTime noTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "收货地址")
    private String address;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "其它金额，单位：元")
    private BigDecimal otherPrice;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "对账状态(false:未对账 ，true:已对账)")
    private Boolean reconciliationStatus;

    @Schema(description = "入库清单列表")
    @Size(min = 1, message = "入库项至少有一个")
    @NotNull(message = "入库项不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @Schema(description = "入库项编号")
        @Null(groups = validation.OnCreate.class, message = "入库id创建时要为null")
        @Null(groups = validation.OnUpdate.class, message = "入库id更新时不能为null")
        @Size(min = 1, groups = validation.OnUpdate.class, message = "更新时至少存在一项入库单")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "型号规格(产品带出)")
        private String model;

        @Schema(description = "产品名称(快照)")
        private String productName;

        @Schema(description = "采购订单项id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "采购订单项id不能为空")
        private Long orderItemId;

        @Schema(description = "采购订单id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "采购订单编号不能为空")
        private Long orderId;

        @Schema(description = "采购订单编号-展示用(源单单号,采购单)")
        @NotNull(message = "采购订单no不能为空")
        private String orderNo;

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "仓库编号不能为空")
        private Long warehouseId;

        @Schema(description = "产品单位(产品带出来)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品单位单位不能为空")
        private Long productUnitId;

        @Schema(description = "产品单价")
        private BigDecimal productPrice;

        @Schema(description = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "入库数量不能为空")
        private BigDecimal count;

        @Schema(description = "价税合计")
        private BigDecimal allAmount;

        @Schema(description = "增值税税率，百分比")
        private BigDecimal taxPercent;

        @Schema(description = "含税单价")
        @DecimalMin(value = "0.00", message = "含税产品单价不能小于0")
        private BigDecimal actTaxPrice;

        @Schema(description = "币别ID,财务管理-币别维护")
        private Long currencyId;

        @Schema(description = "汇率,财务管理-币别维护")
        private BigDecimal exchangeRate;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "单据来源描述")
        private String source;

//        @Schema(description = "源单单号")
//        private int srcNo;

        @Schema(description = "箱率")
        private String containerRate;
    }
}