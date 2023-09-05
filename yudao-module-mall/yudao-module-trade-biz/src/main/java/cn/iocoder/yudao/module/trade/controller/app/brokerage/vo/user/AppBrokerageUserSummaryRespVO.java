package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 个人分销统计 Response VO")
@Data
public class AppBrokerageUserSummaryRespVO {

    @Schema(description = "昨天的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer yesterdayBrokeragePrice;

    @Schema(description = "提现的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer withdrawBrokeragePrice;

    @Schema(description = "可用的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "2408")
    private Integer brokeragePrice;

    @Schema(description = "冻结的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "234")
    private Integer frozenBrokeragePrice;

}
