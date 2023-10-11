package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员分析对照数据 Response VO")
@Data
public class MemberAnalyseComparisonRespVO {

    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

    @Schema(description = "活跃用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer activeUserCount;

    @Schema(description = "充值会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "221")
    private Integer rechargeUserCount;

}
