package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.payment;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 付款单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpFinancePaymentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23752")
    private Long id;

    @Schema(description = "付款单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "FKD888")
    private String no;

    @Schema(description = "付款状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "付款时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime paymentTime;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;
    @Schema(description = "财务人员名称", example = "张三")
    private String financeUserName;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29399")
    private Long supplierId;
    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小番茄公司")
    private String supplierName;

    @Schema(description = "付款账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28989")
    private Long accountId;
    @Schema(description = "付款账户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String accountName;

    @Schema(description = "合计价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "13832")
    private BigDecimal totalPrice;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "11600")
    private BigDecimal discountPrice;

    @Schema(description = "实际价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private BigDecimal paymentPrice;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建人", example = "芋道")
    private String creator;
    @Schema(description = "创建人名称", example = "芋道")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "付款项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "付款项编号", example = "11756")
        private Long id;

        @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer bizType;

        @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        private Long bizId;

        @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        private String bizNo;

        @Schema(description = "应付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        private BigDecimal totalPrice;

        @Schema(description = "已付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        private BigDecimal paidPrice;

        @Schema(description = "本次付款，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        @NotNull(message = "本次付款不能为空")
        private BigDecimal paymentPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

    }

}