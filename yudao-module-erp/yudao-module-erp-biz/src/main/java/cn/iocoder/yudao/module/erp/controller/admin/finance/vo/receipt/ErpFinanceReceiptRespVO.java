package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 收款单 Response VO")
@Data
public class ErpFinanceReceiptRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23752")
    private Long id;

    @Schema(description = "收款单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "FKD888")
    private String no;

    @Schema(description = "收款状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "收款时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime receiptTime;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;
    @Schema(description = "财务人员名称", example = "张三")
    private String financeUserName;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29399")
    private Long customerId;
    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小番茄公司")
    private String customerName;

    @Schema(description = "收款账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28989")
    private Long accountId;
    @Schema(description = "收款账户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String accountName;

    @Schema(description = "合计价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "13832")
    private BigDecimal totalPrice;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "11600")
    private BigDecimal discountPrice;

    @Schema(description = "实际价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private BigDecimal receiptPrice;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建人", example = "芋道")
    private String creator;
    @Schema(description = "创建人名称", example = "芋道")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "收款项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "收款项编号", example = "11756")
        private Long id;

        @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer bizType;

        @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        private Long bizId;

        @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        private String bizNo;

        @Schema(description = "应收金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        private BigDecimal totalPrice;

        @Schema(description = "已收金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        private BigDecimal receiptedPrice;

        @Schema(description = "本次收款，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        @NotNull(message = "本次收款不能为空")
        private BigDecimal receiptPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

    }

}