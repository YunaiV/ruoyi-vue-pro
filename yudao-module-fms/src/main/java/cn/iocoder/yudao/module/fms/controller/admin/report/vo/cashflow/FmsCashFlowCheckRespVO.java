package cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow;

import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - FMS 现金流量表检查 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsCashFlowCheckRespVO extends FmsBalanceSheetCheckRespVO {

    @Schema(description = "资产负债表是否满足现金流量表取数条件", example = "true")
    private Boolean balanceSheetReady;

}
