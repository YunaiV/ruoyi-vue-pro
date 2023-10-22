package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员终端统计 Response VO")
@Data
public class MemberTerminalStatisticsRespVO {

    @Schema(description = "终端", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer terminal;

    // TODO @疯狂：要不 orderCreateUserCount 和 orderPayUserCount 貌似更统一一些；
    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

}
