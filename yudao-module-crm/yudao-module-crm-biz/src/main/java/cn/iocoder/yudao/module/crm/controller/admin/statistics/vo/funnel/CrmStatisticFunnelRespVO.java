package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @puhui999：改成 CrmStatisticFunnelSummaryRespVO 更有统计的味道
@Schema(description = "管理后台 - CRM 销售漏斗 Response VO")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CrmStatisticFunnelRespVO {

    @Schema(description = "客户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long customerCount;

    @Schema(description = "商机数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long businessCount;

    // TODO @puhui999：这个改成 businessWinCount 可能会更合适点哈；
    @Schema(description = "赢单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long winCount;

}
