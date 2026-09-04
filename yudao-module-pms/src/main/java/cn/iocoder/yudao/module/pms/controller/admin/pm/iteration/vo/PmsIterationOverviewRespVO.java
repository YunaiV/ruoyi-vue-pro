package cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - PMS 迭代概览 Response VO")
@Data
public class PmsIterationOverviewRespVO {

    @Schema(description = "工作项总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalCount;

    @Schema(description = "未开始工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer pendingCount;

    @Schema(description = "进行中工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer processingCount;

    @Schema(description = "已完成工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer completedCount;

    @Schema(description = "完成进度百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer progress;

    @Schema(description = "工作项类型数量统计", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"1\": 60, \"2\": 40}")
    private Map<Integer, Integer> typeCountMap;

    @Schema(description = "工作项类型和状态数量统计", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<Integer, Map<Integer, Integer>> typeStatusCountMap;

    @Schema(description = "状态趋势", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TrendItem> statusTrends;

    @Schema(description = "燃尽图数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<BurnDownItem> burnDowns;

    @Schema(description = "最近动态列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ActivityItem> recentActivities;

    @Schema(description = "管理后台 - PMS 迭代概览状态趋势项 VO")
    @Data
    public static class TrendItem {

        @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-01")
        private String date;

        @Schema(description = "当日未开始工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
        private Integer pendingCount;

        @Schema(description = "当日进行中工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer processingCount;

        @Schema(description = "当日已完成工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Integer completedCount;

    }

    @Schema(description = "管理后台 - PMS 迭代概览燃尽图项 VO")
    @Data
    public static class BurnDownItem {

        @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-01")
        private String date;

        @Schema(description = "理想剩余工时，单位：小时", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
        private Integer idealRemaining;

        @Schema(description = "实际剩余工时，单位：小时", requiredMode = Schema.RequiredMode.REQUIRED, example = "38")
        private Integer actualRemaining;

    }

    @Schema(description = "管理后台 - PMS 迭代概览动态项 VO")
    @Data
    public static class ActivityItem {

        @Schema(description = "动态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long workItemId;

        @Schema(description = "工作项序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer workItemSerialNumber;

        @Schema(description = "工作项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "优化登录页")
        private String workItemName;

        @Schema(description = "操作人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long operatorUserId;

        @Schema(description = "操作人昵称", example = "芋道")
        private String operatorUserName;

        @Schema(description = "动态内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "将状态更新为「已完成」")
        private String content;

        @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime createTime;

    }

}
