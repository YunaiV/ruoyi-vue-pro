package cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - FMS 现金流量辅助数据更新 Request VO")
@Data
public class FmsCashFlowAdjustmentUpdateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "辅助数据项", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "辅助数据项不能为空")
    @Valid
    private List<Item> items = new ArrayList<>();

    @Schema(description = "管理后台 - FMS 现金流量辅助数据更新项 Request VO")
    @Data
    public static class Item {

        @Schema(description = "数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "数据编号不能为空")
        private Long id;

        @Schema(description = "本期金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "本期金额不能为空")
        private BigDecimal currentAmount;

        @Schema(description = "本年累计金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "200.00")
        @NotNull(message = "本年累计金额不能为空")
        private BigDecimal yearAmount;

    }

}
