package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购退货新增/修改 Request VO")
@Data
public class ErpPurchaseReturnSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = Validation.OnCreate.class, message = "创建时，退货单id必须为空")
    private Long id;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "退货时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退货时间不能为空")
    private LocalDateTime returnTime;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "其它金额，单位：元")
    private BigDecimal otherPrice;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "退货清单列表")
    private List<Item> items;

    @Schema(description = "币别ID,财务管理-币别维护")
    private Long currencyId;

    @Data
    public static class Item {

        @Schema(description = "退货项编号")
        private Long id;

        @Schema(description = "入库项id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "入库项id不能为空")
        private Long inItemId;

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "仓库编号不能为空")
        private Long warehouseId;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单位单位", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品单位单位不能为空")
        private Long productUnitId;

        @Schema(description = "产品单价")
        private BigDecimal productPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        @Schema(description = "含税单价")
        @DecimalMin(value = "0.00", message = "含税单价不能小于0")
        private BigDecimal actTaxPrice;

        @Schema(description = "增值税税率，百分比")
        private BigDecimal taxPercent;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "箱率")
        private String containerRate;

        @Schema(description = "申请人id")
        private Long applicantId;

        @Schema(description = "申请人部门id")
        private Long applicationDeptId;
    }
}