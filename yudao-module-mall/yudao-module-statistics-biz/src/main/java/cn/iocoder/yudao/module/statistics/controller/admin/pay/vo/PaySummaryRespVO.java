package cn.iocoder.yudao.module.statistics.controller.admin.pay.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 支付统计 Response VO")
@Data
public class PaySummaryRespVO {

    @Schema(description = "充值金额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer rechargePrice;

}
