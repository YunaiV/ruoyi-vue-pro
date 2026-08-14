package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 凭证科目余额 Response VO")
@Data
@Accessors(chain = true)
public class FmsVoucherSubjectBalanceRespVO {

    @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long subjectId;

    @Schema(description = "余额方向", example = "借")
    private String balanceDirection;

    @Schema(description = "余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal balance;

}
