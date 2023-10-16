package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员分析 Response VO")
@Data
public class MemberAnalyseRespVO {

    @Schema(description = "访客数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer visitUserCount;

    @Schema(description = "下单用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer orderUserCount;

    @Schema(description = "成交用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer payUserCount;

    @Schema(description = "客单价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer atv;

    @Schema(description = "对照数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private DataComparisonRespVO<MemberAnalyseDataRespVO> comparison;

}
