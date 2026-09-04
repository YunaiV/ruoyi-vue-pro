package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - PMS 项目概况 Response VO")
@Data
public class PmsProjectOverviewRespVO {

    @Schema(description = "工作项总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long totalCount;

    @Schema(description = "未开始工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Long pendingCount;

    @Schema(description = "进行中工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Long processingCount;

    @Schema(description = "已完成工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Long completedCount;

    @Schema(description = "按工作项类型统计")
    private Map<Integer, Long> typeCountMap;

    @Schema(description = "近十四日完成趋势")
    private List<TrendPoint> completedTrends;

    @Schema(description = "分配给当前用户的未完成工作项")
    private List<AssignedWorkItem> assignedWorkItems;

    @Schema(description = "PMS 项目概况 - 趋势点")
    @Data
    public static class TrendPoint {

        @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
        private String date;

        @Schema(description = "当日完成数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Long count;

    }

    @Schema(description = "PMS 项目概况 - 分配给我的工作项")
    @Data
    public static class AssignedWorkItem {

        @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "项目内序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
        private Integer serialNumber;

        @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer type;

        @Schema(description = "工作项标题", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "语义状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Integer status;

        @Schema(description = "截止时间")
        private LocalDateTime endTime;

        @Schema(description = "完成进度", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        private Integer progress;

    }

}
