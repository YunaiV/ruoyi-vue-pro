package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员统计 Response VO")
@Data
public class MemberSummaryRespVO {

    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

    @Schema(description = "充值会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "221")
    private Integer rechargeUserCount;

    @Schema(description = "充值金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer rechargePrice;

    @Schema(description = "支出金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer expensePrice;

}
