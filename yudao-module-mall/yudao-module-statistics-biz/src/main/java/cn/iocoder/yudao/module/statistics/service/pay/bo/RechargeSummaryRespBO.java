package cn.iocoder.yudao.module.statistics.service.pay.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO @疯狂：BO 不用写 swagger 注解哈，写注释就好啦；
@Schema(description = "管理后台 - 充值统计 Response VO")
@Data
public class RechargeSummaryRespBO {

    @Schema(description = "充值会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "221")
    private Integer rechargeUserCount;

    @Schema(description = "充值金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer rechargePrice;

}
