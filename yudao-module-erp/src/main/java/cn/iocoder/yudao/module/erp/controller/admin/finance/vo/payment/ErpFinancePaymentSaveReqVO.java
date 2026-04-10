package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 付款单新增/修改 Request VO")
@Data
public class ErpFinancePaymentSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23752")
    private Long id;

    @Schema(description = "付款时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "付款时间不能为空")
    private LocalDateTime paymentTime;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29399")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "付款账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28989")
    @NotNull(message = "付款账户编号不能为空")
    private Long accountId;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "11600")
    @NotNull(message = "优惠金额不能为空")
    private BigDecimal discountPrice;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "付款项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "付款项列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "付款项编号", example = "11756")
        private Long id;

        @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "业务类型不能为空")
        private Integer bizType;

        @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        @NotNull(message = "业务编号不能为空")
        private Long bizId;

        @Schema(description = "已付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        @NotNull(message = "已付金额不能为空")
        private BigDecimal paidPrice;

        @Schema(description = "本次付款，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        @NotNull(message = "本次付款不能为空")
        private BigDecimal paymentPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

    }

}