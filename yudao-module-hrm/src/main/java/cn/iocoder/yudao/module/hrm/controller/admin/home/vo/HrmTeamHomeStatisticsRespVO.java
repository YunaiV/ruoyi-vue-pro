package cn.iocoder.yudao.module.hrm.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 团队工作台统计 Response VO")
@Data
public class HrmTeamHomeStatisticsRespVO {

    @Schema(description = "当前主管员工编号", example = "1")
    private Long leaderEmployeeId;

    @Schema(description = "我的团队")
    private TeamOverview teamOverview;

    @Schema(description = "团队概况")
    private TeamSurvey teamSurvey;

    @Schema(description = "管理后台 - HRM 团队工作台我的团队 Response VO")
    @Data
    public static class TeamOverview {

        @Schema(description = "团队人数", example = "12")
        private Long employeeCount;

        @Schema(description = "本月入职人数", example = "2")
        private Long entryThisMonthCount;

        @Schema(description = "本月离职人数", example = "1")
        private Long leaveThisMonthCount;

        @Schema(description = "本月转正人数", example = "3")
        private Long regularThisMonthCount;

    }

    @Schema(description = "管理后台 - HRM 团队工作台团队概况 Response VO")
    @Data
    public static class TeamSurvey {

        @Schema(description = "员工状态占比")
        private List<AnalysisItem> statusAnalysis;

        @Schema(description = "男女性别占比")
        private List<AnalysisItem> sexAnalysis;

        @Schema(description = "成员年龄占比")
        private List<AnalysisItem> ageAnalysis;

        @Schema(description = "成员司龄占比")
        private List<AnalysisItem> companyAgeAnalysis;

    }

    @Schema(description = "管理后台 - HRM 团队工作台统计分析项 Response VO")
    @Data
    public static class AnalysisItem {

        @Schema(description = "分类类型；null 表示未填写", example = "1")
        private Integer type;

        @Schema(description = "数量", example = "8")
        private Long count;

    }

}
