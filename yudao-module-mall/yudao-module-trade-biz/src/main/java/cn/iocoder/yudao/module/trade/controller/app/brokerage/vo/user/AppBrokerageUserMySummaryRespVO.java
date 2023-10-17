package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 个人分销统计 Response VO")
@Data
public class AppBrokerageUserMySummaryRespVO {

    @Schema(description = "昨天的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer yesterdayPrice;

    @Schema(description = "提现的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer withdrawPrice;

    @Schema(description = "可用的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "2408")
    private Integer brokeragePrice;

    @Schema(description = "冻结的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "234")
    private Integer frozenPrice;

    @Schema(description = "分销用户数量（一级）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long firstBrokerageUserCount;

    @Schema(description = "分销用户数量（二级）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long secondBrokerageUserCount;

}
