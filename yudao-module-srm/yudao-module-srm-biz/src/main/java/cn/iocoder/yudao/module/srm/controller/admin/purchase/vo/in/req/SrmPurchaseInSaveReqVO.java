package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库新增/修改 Request VO")
@Data
public class SrmPurchaseInSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = Validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，订单id不能为空")
    private Long id;

    /**
     * 采购订单号 冗余 {@link SrmPurchaseOrderDO#getNo()}
     */
    @Schema(description = "入库单号")
    private String no;

    @Schema(description = "入库时间(不填默认当前时间)")
    private LocalDateTime inTime;

    @Schema(description = "单据日期")
    private LocalDateTime billTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "收货地址")
    private String address;

    //    @Schema(description = "结算日期")
    //    private LocalDateTime settlementDate;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

//    @Schema(description = "产品单价")
//    @DecimalMin(value = "0.00", message = "产品单价不能小于0")
//    @NotNull(message = "产品单价不能为空")
//    private BigDecimal productPrice;

    @Schema(description = "其它金额，单位：元")
    private BigDecimal otherPrice;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "对账状态(false:未对账 ，true:已对账)")
    private Boolean reconciliationStatus;

    @Schema(description = "版本号")
    private Long version;

    @Schema(description = "入库清单列表")
    @Size(min = 1, message = "入库项至少有一个")
    @NotNull(message = "入库项不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @Schema(description = "入库项编号")
        @Null(groups = Validation.OnCreate.class, message = "入库id创建时要为null")
        @Null(groups = Validation.OnUpdate.class, message = "入库id更新时不能为null")
        @Size(min = 1, groups = Validation.OnUpdate.class, message = "更新时至少存在一项入库单")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "型号规格(产品带出)")
        private String model;

        @Schema(description = "采购订单项id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "采购订单项id不能为空")
        private Long orderItemId;

        @Schema(description = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "入库数量不能为空")
        private BigDecimal qty;

        /**
         * 总价，单位：元 totalPrice = productPrice * qty
         */
        @Schema(description = "总价，单位：元。totalPrice = productPrice * qty")
        @NotNull(message = "总价不能为空")
        private BigDecimal totalPrice;

        @Schema(description = "税价合计，单位：元。 taxPrice = totalPrice * taxPercent")
        @NotNull(message = "税价合计不能为空")
        private BigDecimal taxPrice;
        /**
         * 合计产品价格，单位：元
         */
        @Schema(description = "合计产品价格，单位：元")
        @NotNull(message = "合计产品价格不能为空")
        private BigDecimal totalProductPrice;

        @Schema(description = "合计税价，单位：元。 totalTaxPrice = totalPrice * taxPercent")
        @NotNull(message = "合计税价不能为空")
        private BigDecimal totalTaxPrice;

        @Schema(description = "仓库id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "仓库id不能为空")
        private Long warehouseId;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "单据来源描述")
        private String source;

        @Schema(description = "申请人id")
        private Long applicantId;

        @Schema(description = "申请人部门id")
        private Long applicationDeptId;

        @Schema(description = "版本号")
        private Long version;
    }
}