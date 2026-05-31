package cn.iocoder.yudao.module.im.controller.admin.manager.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IM 数据看板概览 Response VO")
@Data
public class ImStatisticsManagerOverviewRespVO {

    @Schema(description = "用户总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private Long totalUser;

    @Schema(description = "今日新增用户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "23")
    private Long newUserToday;

    @Schema(description = "群总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "678")
    private Long totalGroup;

    @Schema(description = "今日新建群数", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long newGroupToday;

    @Schema(description = "日活用户（今日发过消息的去重用户数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1023")
    private Long activeUserDaily;

    @Schema(description = "周活用户（最近 7 天发过消息的去重用户数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "4567")
    private Long activeUserWeekly;

    @Schema(description = "月活用户（最近 30 天发过消息的去重用户数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "8901")
    private Long activeUserMonthly;

    @Schema(description = "今日私聊消息数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8765")
    private Long privateMessageToday;

    @Schema(description = "今日群聊消息数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3210")
    private Long groupMessageToday;

    @Schema(description = "昨日私聊消息数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7890")
    private Long privateMessageYesterday;

    @Schema(description = "昨日群聊消息数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3000")
    private Long groupMessageYesterday;

}
