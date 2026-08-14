package cn.iocoder.yudao.module.fms.controller.admin.closing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 结账概况 Response VO")
@Data
public class FmsClosingOverviewRespVO {

    @Schema(description = "会计期间", example = "2026-08")
    private String month;

    @Schema(description = "是否已结账")
    private Boolean closed;

    @Schema(description = "是否要求凭证审核")
    private Boolean voucherReviewRequired;

    @Schema(description = "未审核凭证数量", example = "2")
    private Long pendingVoucherCount;

    @Schema(description = "凭证数量", example = "12")
    private Long voucherCount;

    @Schema(description = "损益类科目余额", example = "9100.00")
    private BigDecimal profitLossBalance;

    @Schema(description = "资产负债表差额", example = "0.00")
    private BigDecimal balanceSheetDifference;

    @Schema(description = "结转损益凭证编号", example = "1024")
    private Long profitLossVoucherId;

    @Schema(description = "初始余额是否试算平衡")
    private Boolean initialBalanceBalanced;

    @Schema(description = "凭证编号是否连续")
    private Boolean voucherNumberContinuous;

    @Schema(description = "需要结转损益时是否已生成凭证")
    private Boolean profitLossVoucherGenerated;

    @Schema(description = "利润表勾稽是否平衡")
    private Boolean incomeStatementBalanced;

    @Schema(description = "利润表未纳入公式的科目数量", example = "0")
    private Integer incomeStatementUnmappedSubjectCount;

    @Schema(description = "资产负债表损益是否已结转")
    private Boolean balanceSheetProfitLossTransferred;

    @Schema(description = "资产负债表是否平衡")
    private Boolean balanceSheetBalanced;

    @Schema(description = "资产负债表未纳入公式的科目数量", example = "0")
    private Integer balanceSheetUnmappedSubjectCount;

    @Schema(description = "是否满足全部结账条件")
    private Boolean canClose;

}
