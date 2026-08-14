package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - FMS 凭证分录保存 Request VO")
@Data
public class FmsVoucherEntrySaveReqVO {

    @Schema(description = "凭证分录编号", example = "1024")
    private Long id;

    @Schema(description = "摘要内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "支付办公用品款")
    @NotBlank(message = "摘要不能为空")
    @Size(max = 500, message = "摘要长度不能超过 500 个字符")
    private String digest;

    @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "科目不能为空")
    private Long subjectId;

    @Schema(description = "数量", example = "10")
    @DecimalMin(value = "0", message = "数量不能小于 0")
    @Digits(integer = 14, fraction = 4, message = "数量最多保留 4 位小数")
    private BigDecimal quantity;

    @Schema(description = "单价", example = "20.50")
    @DecimalMin(value = "0", message = "单价不能小于 0")
    @Digits(integer = 12, fraction = 6, message = "单价最多保留 6 位小数")
    private BigDecimal unitPrice;

    @Schema(description = "借方金额", example = "205.00")
    @Digits(integer = 16, fraction = 2, message = "借方金额最多保留 2 位小数")
    private BigDecimal debitAmount;

    @Schema(description = "贷方金额", example = "205.00")
    @Digits(integer = 16, fraction = 2, message = "贷方金额最多保留 2 位小数")
    private BigDecimal creditAmount;

    @Schema(description = "辅助核算项目数组")
    @Valid
    private List<AuxiliaryItem> auxiliaries;

    @Schema(description = "管理后台 - FMS 凭证分录辅助核算项目")
    @Data
    public static class AuxiliaryItem {

        @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
        @NotNull(message = "辅助核算类别不能为空")
        private Long typeId;

        @Schema(description = "辅助核算项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "辅助核算项目不能为空")
        private Long itemId;
    }

}
