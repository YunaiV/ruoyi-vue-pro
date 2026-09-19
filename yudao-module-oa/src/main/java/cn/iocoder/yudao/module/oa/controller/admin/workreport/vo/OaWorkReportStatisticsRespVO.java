package cn.iocoder.yudao.module.oa.controller.admin.workreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - OA 工作汇报统计 Response VO")
@Data
public class OaWorkReportStatisticsRespVO {

    @Schema(description = "统计人数", example = "1")
    private Integer userCount;

    @Schema(description = "应填数量", example = "1")
    private Integer expectedCount;

    @Schema(description = "已填数量", example = "1")
    private Integer submittedCount;

    @Schema(description = "未填数量", example = "1")
    private Integer missingCount;

    @Schema(description = "员工统计列表")
    private List<UserStatistics> users;

    @Schema(description = "管理后台 - 员工工作汇报统计 Response VO")
    @Data
    public static class UserStatistics {

        @Schema(description = "员工用户编号", example = "1024")
        private Long userId;

        @Schema(description = "员工昵称", example = "张三")
        private String userName;

        @Schema(description = "部门编号", example = "1024")
        private Long deptId;

        @Schema(description = "部门名称", example = "行政部")
        private String deptName;

        @Schema(description = "应填数量", example = "1")
        private Integer expectedCount;

        @Schema(description = "已填数量", example = "1")
        private Integer submittedCount;

        @Schema(description = "未填数量", example = "1")
        private Integer missingCount;

        @Schema(description = "已提交汇报")
        private List<Report> submittedReports;

        @Schema(description = "未填周期", example = "[\"2026-09-14\"]")
        private List<String> missingPeriodKeys;

    }

    @Schema(description = "管理后台 - 已提交汇报摘要 Response VO")
    @Data
    public static class Report {

        @Schema(description = "汇报编号", example = "1024")
        private Long id;

        @Schema(description = "汇报单号", example = "HB202609140001")
        private String no;

        @Schema(description = "汇报标题", example = "2026-09-14 工作日报")
        private String title;

        @Schema(description = "汇报状态", example = "2")
        private Integer status;

        @Schema(description = "开始时间", type = "integer", format = "int64", example = "1789347600000")
        private LocalDateTime startTime;

        @Schema(description = "创建时间", type = "integer", format = "int64", example = "1789347600000")
        private LocalDateTime createTime;

        @Schema(description = "汇报周期", example = "2026-09-14")
        private String periodKey;

    }

}
