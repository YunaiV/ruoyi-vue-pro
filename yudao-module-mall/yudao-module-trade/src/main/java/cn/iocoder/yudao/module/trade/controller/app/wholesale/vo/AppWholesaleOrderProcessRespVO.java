package cn.iocoder.yudao.module.trade.controller.app.wholesale.vo;

import cn.iocoder.yudao.module.trade.service.wholesale.bo.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 批发订单处理 Response VO")
@Data
public class AppWholesaleOrderProcessRespVO {

    @Schema(description = "处理状态：success / need_alternatives")
    private String status;

    @Schema(description = "缺货替代品列表（status=need_alternatives 时返回）")
    private List<WholesaleStockCheckBO.AlternativeItem> alternatives;

    @Schema(description = "订单价格摘要")
    private WholesaleOrderSummaryBO summary;

    @Schema(description = "智能合同")
    private SmartContractBO contract;

    @Schema(description = "信用评估结果")
    private WholesalerCreditBO credit;

    @Schema(description = "下一步操作建议")
    private List<WholesaleNextStepBO> nextSteps;

    @Schema(description = "区块链存证任务 ID")
    private String blockchainTaskId;

}
