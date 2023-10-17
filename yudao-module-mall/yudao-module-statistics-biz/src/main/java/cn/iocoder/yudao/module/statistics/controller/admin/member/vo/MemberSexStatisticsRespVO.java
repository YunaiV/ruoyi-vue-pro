package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员性别统计 Response VO")
@Data
public class MemberSexStatisticsRespVO {

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sex;

    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

}
