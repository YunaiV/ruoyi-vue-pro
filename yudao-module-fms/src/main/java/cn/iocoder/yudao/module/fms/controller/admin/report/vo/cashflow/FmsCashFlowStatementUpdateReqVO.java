package cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - FMS 现金流量表修改 Request VO")
@Data
public class FmsCashFlowStatementUpdateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "开始会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotNull(message = "开始会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "开始会计期间格式不正确")
    private String startMonth;

    @Schema(description = "结束会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotNull(message = "结束会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "结束会计期间格式不正确")
    private String endMonth;

    @Schema(description = "现金流量表项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "现金流量表项目不能为空")
    @Valid
    private List<Item> items = new ArrayList<>();

    @Schema(description = "管理后台 - FMS 现金流量表修改项 Request VO")
    @Data
    public static class Item {

        @Schema(description = "报表项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        @NotNull(message = "报表项目编号不能为空")
        private Long id;

        @Schema(description = "本期金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "本期金额不能为空")
        private BigDecimal currentAmount;

        @Schema(description = "本年累计金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "200.00")
        @NotNull(message = "本年累计金额不能为空")
        private BigDecimal yearAmount;

    }

}
