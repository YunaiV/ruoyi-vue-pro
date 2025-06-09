package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购退货新增/修改 Request VO")
@Data
public class SrmPurchaseReturnSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = Validation.OnCreate.class, message = "创建时，退货单id必须为空")
    @NotNull(groups = Validation.OnUpdate.class, message = "更新时，退货单id不能为空")
    private Long id;

    @Schema(description = "退货单编号")
    private String code;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "退货时间")
    @NotNull(message = "退货时间不能为空")
    private LocalDateTime returnTime;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "其它金额，单位：元")
    private BigDecimal otherPrice;

    @Schema(description = "供应商id")
    private Long supplierId;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "退货清单列表")
    @Size(min = 1, message = "至少选择一个到货明细行")
    private List<Item> items;


    @Data
    public static class Item {

        @Schema(description = "退货项编号")
        @Null(groups = Validation.OnCreate.class, message = "创建时，退货单明细行id必须为空")
        private Long id;


        @Schema(description = "到货项id", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "到货项id不能为空")
        private Long arriveItemId;

        @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "退货数量不能为空")
        private BigDecimal qty;

        @Schema(description = "备注")
        private String remark;

//从到货项复制
//        @Schema(description = "退货申请人id")
//        private Long applicantId;
//
//        @Schema(description = "退货申请人部门id")
//        private Long applicationDeptId;
//
//        /**
//         * 实际入库数量,取到货单item的实际入库数量
//         */
//        @Schema(description = "实际入库数量,到货项实际入库数量")
//        private BigDecimal actualQty;
    }
}