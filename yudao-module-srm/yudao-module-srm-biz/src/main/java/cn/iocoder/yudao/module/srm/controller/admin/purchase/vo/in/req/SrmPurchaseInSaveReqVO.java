package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购到货新增/修改 Request VO")
@Data
public class SrmPurchaseInSaveReqVO {

    // ========== 主表字段 ==========

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = Validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，订单id不能为空")
    @DiffLogField(name = "到货单编号")
    private Long id;

    @Schema(description = "到货单号")
    @DiffLogField(name = "到货单号")
    private String code;

    @Schema(description = "到货时间(不填默认当前时间)")
    @DiffLogField(name = "到货时间")
    private LocalDateTime arriveTime;

    @Schema(description = "单据日期")
    @DiffLogField(name = "单据日期")
    private LocalDateTime billTime;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    @DiffLogField(name = "供应商编号")
    private Long supplierId;

    @Schema(description = "收货地址")
    @DiffLogField(name = "收货地址")
    private String address;

    @Schema(description = "结算账户编号")
    @DiffLogField(name = "结算账户编号")
    private Long accountId;

    @Schema(description = "优惠率，百分比")
    @DiffLogField(name = "优惠率")
    private BigDecimal discountPercent;

    @Schema(description = "其它金额，单位：元")
    @DiffLogField(name = "其它金额")
    private BigDecimal otherPrice;

    @Schema(description = "附件地址")
    @DiffLogField(name = "附件地址")
    private String fileUrl;

    @Schema(description = "币别ID")
    private Long currencyId;

    @Schema(description = "总毛重")
    @DiffLogField(name = "总毛重")
    private BigDecimal totalWeight;


    @Schema(description = "总体积")
    @DiffLogField(name = "总体积")
    private BigDecimal totalVolume;

    @Schema(description = "备注")
    @DiffLogField(name = "备注")
    private String remark;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "到货清单列表")
    @Size(min = 1, message = "到货项至少有一个")
    @NotNull(message = "到货项不能为空")
    @DiffLogField(name = "到货清单列表")
    private List<Item> items;

    // ========== 子表字段 ==========

    @Data
    public static class Item {

        // ========== 基本信息 ==========

        @Schema(description = "到货项编号")
        @DiffLogField(name = "到货项编号")
        @Null(groups = Validation.OnCreate.class, message = "到货id创建时要为null")
        @Size(min = 1, groups = Validation.OnUpdate.class, message = "更新时至少存在一项到货单")
        private Long id;

        @Schema(description = "采购到货编号")
        private Long arriveId;

        @Schema(description = "仓库Id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "仓库不能为空")
        private Long warehouseId;

        @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品ID不能为空")
        private Long productId;

        @Schema(description = "产品单位ID")
        @NotNull(message = "产品单位ID不能为空")
        private Long productUnitId;

        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "产品单位名称不能为空")
        private String productUnitName;

        // ========== 金额数量 ==========

        @Schema(description = "产品单价", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品单价不能为空")
        @DiffLogField(name = "产品单价")
        @DecimalMin(value = "0.00", message = "产品单价不能小于0")
        private BigDecimal productPrice;

        @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "到货数量不能为空")
        @DiffLogField(name = "到货数量")
        private BigDecimal qty;

//        @Schema(description = "实际到货数量(到货回填)")
//        private BigDecimal actualQty;

        @Schema(description = "总价，单位：元。totalPrice = productPrice * qty", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "总价不能为空")
        @DiffLogField(name = "总价")
        private BigDecimal totalPrice;

        @Schema(description = "税率，百分比")
        @DiffLogField(name = "税率")
        private BigDecimal taxRate;

        @Schema(description = "税价合计，单位：元。 tax = totalPrice * taxRate", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "税价合计不能为空")
        @DiffLogField(name = "税价合计")
        private BigDecimal tax;

        @Schema(description = "含税单价")
        @DiffLogField(name = "含税单价")
        private BigDecimal grossPrice;

        @Schema(description = "价税合计")
        @DiffLogField(name = "价税合计")
        private BigDecimal grossTotalPrice;

        @Schema(description = "合计产品价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "合计产品价格不能为空")
        @DiffLogField(name = "合计产品价格")
        private BigDecimal totalProductPrice;

        @Schema(description = "合计税价，单位：元。 totalGrossPrice = totalPrice * taxRate", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "合计税价不能为空")
        @DiffLogField(name = "合计税价")
        private BigDecimal totalGrossPrice;

        @Schema(description = "已付款金额")
        @DiffLogField(name = "已付款金额")
        private BigDecimal payPrice;

        @Schema(description = "付款状态")
        @DiffLogField(name = "付款状态")
        private Integer payStatus;

        // ========== 来源关联 ==========

        @Schema(description = "采购订单项ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "采购订单项ID不能为空")
        @DiffLogField(name = "采购订单项编号")
        private Long orderItemId;

        @Schema(description = "型号规格(产品带出)")
        @DiffLogField(name = "型号规格")
        private String model;

        @Schema(description = "单据来源描述(前端不传)")
        @DiffLogField(name = "单据来源")
        private String source;

        // ========== 人员组织 ==========

        @Schema(description = "申请人id")
        @DiffLogField(name = "申请人编号")
        private Long applicantId;

        @Schema(description = "申请人部门id")
        private Long applicationDeptId;

        // ========== 产品相关 ==========

        @Schema(description = "产品报关品名")
        @NotBlank(message = "产品报关品名不能为空")
        private String declaredType;

        @Schema(description = "产品报关品名英文")
        @NotBlank(message = "产品报关品名英文不能为空")
        private String declaredTypeEn;

        @Schema(description = "产品sku")
        @NotBlank(message = "产品sku不能为空")
        private String productCode;

        @Schema(description = "产品名称")
        @NotBlank(message = "产品名称不能为空")
        private String productName;

        // ========== 其他字段 ==========

        @Schema(description = "x编码")
        private String fbaCode;

        @Schema(description = "箱率")
        private String containerRate;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "版本号")
        private Integer version;
    }
}
